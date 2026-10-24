"""Nhắc việc định kỳ. Một worker giữ khóa Postgres để khỏi chạy trùng."""
import os

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection, giao_dich
from dich_vu.dat_phong_service import huy_don_qua_han
from dich_vu.email_service import gui_email, smtp_da_cau_hinh
from dich_vu.thong_bao_service import tao_thong_bao

_scheduler = None
_KHOA = 82420124


def _da_nhac_hom_nay(cursor, nguoi_id, tieu_de):
    cursor.execute(
        """SELECT 1 FROM thong_bao
           WHERE nguoi_dung_id = %s AND tieu_de = %s AND ngay_tao::date = CURRENT_DATE
           LIMIT 1""",
        (nguoi_id, tieu_de),
    )
    return cursor.fetchone() is not None


def nhac_buoi_sang(conn):
    with giao_dich(conn):
        cursor = get_cursor(conn)
        cursor.execute(
            """SELECT lh.id, lh.nguoi_dung_id, lh.thoi_gian_hen, p.tieu_de, p.nguoi_dung_id AS chu_id
               FROM lich_hen_xem_phong lh
               JOIN phong p ON p.id = lh.phong_id
               WHERE lh.thoi_gian_hen::date = CURRENT_DATE
                 AND lh.trang_thai IN ('cho_xac_nhan', 'chap_nhan')"""
        )
        for lich in cursor.fetchall():
            tieu_de = 'Lịch xem phòng hôm nay'
            for nguoi_id in (lich['nguoi_dung_id'], lich['chu_id']):
                if _da_nhac_hom_nay(cursor, nguoi_id, tieu_de):
                    continue
                tao_thong_bao(
                    cursor, nguoi_id, tieu_de,
                    f"{lich['tieu_de']} lúc {lich['thoi_gian_hen']}",
                    '/lich-hen-cua-toi',
                )
        cursor.execute(
            """SELECT hd.id, hd.tong_tien, hd.thang_nam, hop.nguoi_dung_id AS khach_id,
                      p.nguoi_dung_id AS chu_id, p.tieu_de, nd.email
               FROM hoa_don_thang hd
               JOIN hop_dong_thue hop ON hop.id = hd.hop_dong_id
               JOIN phong p ON p.id = hop.phong_id
               JOIN nguoi_dung nd ON nd.id = hop.nguoi_dung_id
               WHERE hd.trang_thai = 'chua_dong'
                 AND (
                     hd.thang_nam < to_char(CURRENT_DATE, 'YYYY-MM')
                     OR (hd.thang_nam = to_char(CURRENT_DATE, 'YYYY-MM') AND EXTRACT(DAY FROM CURRENT_DATE) > 5)
                 )"""
        )
        for hoa_don in cursor.fetchall():
            tieu_de = 'Hóa đơn tiền nhà chưa đóng'
            noi_dung = f"{hoa_don['tieu_de']} tháng {hoa_don['thang_nam']}"
            if not _da_nhac_hom_nay(cursor, hoa_don['khach_id'], tieu_de):
                tao_thong_bao(cursor, hoa_don['khach_id'], tieu_de, noi_dung, '/my-bookings')
                if smtp_da_cau_hinh() and hoa_don.get('email'):
                    gui_email(hoa_don['email'], tieu_de, f'<p>{noi_dung}</p>')
            if not _da_nhac_hom_nay(cursor, hoa_don['chu_id'], tieu_de):
                tao_thong_bao(cursor, hoa_don['chu_id'], tieu_de, noi_dung, '/chu-nha/hoa-don')


def _viec_co_khoa(ham):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    try:
        cursor.execute('SELECT pg_try_advisory_lock(%s)', (_KHOA,))
        if not cursor.fetchone()['pg_try_advisory_lock']:
            return
        ham(conn)
    finally:
        try:
            cursor.execute('SELECT pg_advisory_unlock(%s)', (_KHOA,))
        except Exception:
            pass
        conn.close()


def khoi_dong_lich():
    global _scheduler
    if _scheduler is not None:
        return
    if os.environ.get('TESTING') == '1' or os.environ.get('FLASK_ENV') == 'testing':
        return
    from apscheduler.schedulers.background import BackgroundScheduler

    lich = BackgroundScheduler(timezone='Asia/Ho_Chi_Minh')
    lich.add_job(lambda: _viec_co_khoa(huy_don_qua_han), 'interval', minutes=1, id='het_han', max_instances=1)
    lich.add_job(lambda: _viec_co_khoa(nhac_buoi_sang), 'cron', hour=8, minute=0, id='sang', max_instances=1)
    lich.start()
    _scheduler = lich
