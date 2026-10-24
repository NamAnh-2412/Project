"""Một chỗ ghi đơn, hợp đồng và tiền cọc. Database là trọng tài."""
import os
from calendar import monthrange
from datetime import date, datetime, timedelta

from psycopg2 import IntegrityError

from cau_hinh.co_so_du_lieu import get_cursor, giao_dich
from dich_vu.thong_bao_service import tao_thong_bao


class LoiDon(Exception):
    def __init__(self, message, don_cu_id=None):
        super().__init__(message)
        self.message = message
        self.don_cu_id = don_cu_id


def gio_giu_cho():
    try:
        return max(int(os.environ.get('GIU_CHO_GIO', '24')), 1)
    except ValueError:
        return 24


def cong_thang(ngay, so_thang):
    if isinstance(ngay, datetime):
        ngay = ngay.date()
    if isinstance(ngay, str):
        ngay = date.fromisoformat(ngay[:10])
    so_thang = max(int(so_thang or 1), 1)
    thang = ngay.month - 1 + so_thang
    nam = ngay.year + thang // 12
    thang = thang % 12 + 1
    ngay_trong_thang = min(ngay.day, monthrange(nam, thang)[1])
    ket = date(nam, thang, ngay_trong_thang)
    if ket <= ngay:
        ket = ngay + timedelta(days=1)
    return ket


def cap_nhat_co_phong(cursor, phong_id):
    cursor.execute(
        """UPDATE phong SET trang_thai = CASE
               WHEN EXISTS (
                   SELECT 1 FROM hop_dong_thue
                   WHERE phong_id = %s AND trang_thai = 'dang_hieu_luc'
               ) OR EXISTS (
                   SELECT 1 FROM dat_phong
                   WHERE phong_id = %s AND trang_thai = 'da_xac_nhan'
               ) THEN 'da_thue' ELSE 'con_trong' END
           WHERE id = %s""",
        (phong_id, phong_id, phong_id),
    )


def _khoa_phong(cursor, phong_id):
    cursor.execute('SELECT * FROM phong WHERE id = %s FOR UPDATE', (phong_id,))
    return cursor.fetchone()


def _ap_ma_giam(cursor, ma, gia_thue):
    if not ma or not str(ma).strip():
        return None, 0
    cursor.execute(
        'SELECT * FROM ma_giam WHERE UPPER(ma) = UPPER(%s) AND dang_dung IS TRUE FOR UPDATE',
        (str(ma).strip(),),
    )
    row = cursor.fetchone()
    if not row:
        raise LoiDon('Mã giảm không hợp lệ.')
    if row.get('het_han') and row['het_han'] < date.today():
        raise LoiDon('Mã giảm đã hết hạn.')
    if row.get('so_lan_toi_da') is not None and int(row['so_lan_da_dung'] or 0) >= int(row['so_lan_toi_da']):
        raise LoiDon('Mã giảm đã hết lượt.')
    gia = float(gia_thue or 0)
    if row['loai'] == 'phan_tram':
        giam = gia * float(row['gia_tri']) / 100.0
    else:
        giam = float(row['gia_tri'])
    if row.get('tran_giam') is not None:
        giam = min(giam, float(row['tran_giam']))
    giam = min(max(giam, 0), gia)
    cursor.execute(
        'UPDATE ma_giam SET so_lan_da_dung = so_lan_da_dung + 1 WHERE id = %s',
        (row['id'],),
    )
    return row['id'], round(giam, 2)


def _dam_bao_khoan(cursor, don, loai, so_tien, phuong_thuc):
    cursor.execute(
        """SELECT * FROM giao_dich_thanh_toan
           WHERE dat_phong_id = %s AND loai = %s AND trang_thai <> 'da_huy'
           ORDER BY id DESC LIMIT 1""",
        (don['id'], loai),
    )
    row = cursor.fetchone()
    if row:
        return row
    from dich_vu.payment_service import ma_coc
    ma = ma_coc(don['id']) if loai == 'coc' else f"THUE{don['id']}"
    cursor.execute(
        """INSERT INTO giao_dich_thanh_toan
           (ma_giao_dich, dat_phong_id, nguoi_dung_id, so_tien, phuong_thuc, trang_thai, thong_tin_nhan, loai)
           VALUES (%s, %s, %s, %s, %s, 'dang_cho', %s, %s)
           RETURNING *""",
        (ma, don['id'], don['nguoi_dung_id'], so_tien, phuong_thuc, ma if loai == 'coc' else None, loai),
    )
    return cursor.fetchone()


def tao_don(conn, phong_id, nguoi_dung_id, tin_nhan, sdt, ngay_vao, thoi_han, so_nguoi, ma_giam=None):
    if isinstance(ngay_vao, str):
        ngay_vao = date.fromisoformat(ngay_vao[:10]) if ngay_vao else date.today()
    if ngay_vao is None:
        ngay_vao = date.today()
    thoi_han = max(int(thoi_han or 1), 1)
    ket_thuc = cong_thang(ngay_vao, thoi_han)
    try:
        with giao_dich(conn):
            cursor = get_cursor(conn)
            phong = _khoa_phong(cursor, phong_id)
            if not phong:
                raise LoiDon('Không tìm thấy phòng.')
            if phong['nguoi_dung_id'] == nguoi_dung_id:
                raise LoiDon('Bạn không thể đặt phòng của chính mình.')
            if (phong.get('trang_thai_duyet') or 'da_duyet') != 'da_duyet' or phong.get('an_tin'):
                raise LoiDon('Tin này chưa được đặt.')
            cursor.execute(
                'SELECT email_da_xac_thuc FROM nguoi_dung WHERE id = %s',
                (nguoi_dung_id,),
            )
            nguoi = cursor.fetchone()
            if not nguoi or not nguoi.get('email_da_xac_thuc'):
                raise LoiDon('Hãy xác thực email trước khi đặt phòng.')
            cursor.execute(
                """SELECT id FROM hop_dong_thue
                   WHERE phong_id = %s AND trang_thai = 'dang_hieu_luc' LIMIT 1""",
                (phong_id,),
            )
            if cursor.fetchone():
                raise LoiDon('Phòng đang có hợp đồng hiệu lực.')
            cursor.execute(
                """SELECT id FROM dat_phong
                   WHERE phong_id = %s AND nguoi_dung_id = %s AND trang_thai = 'cho_xac_nhan'
                   LIMIT 1""",
                (phong_id, nguoi_dung_id),
            )
            don_cu = cursor.fetchone()
            if don_cu:
                raise LoiDon('Bạn đang có một đơn chờ cho phòng này.', don_cu_id=don_cu['id'])
            gia = float(phong.get('gia') or 0)
            coc = float(phong.get('tien_coc') or 0)
            ma_id, giam = _ap_ma_giam(cursor, ma_giam, gia)
            het_han = datetime.now() + timedelta(hours=gio_giu_cho())
            try:
                cursor.execute(
                    """INSERT INTO dat_phong
                       (phong_id, nguoi_dung_id, tin_nhan, so_dien_thoai_lien_he, trang_thai,
                        ngay_chuyen_vao, thoi_han_thue_thang, so_nguoi_o, tien_coc_phai_tra,
                        trang_thai_thanh_toan, gia_thue_chot, het_han_giu, ngay_ket_thuc_du_kien,
                        ma_giam_id, giam_thang_dau)
                       VALUES (%s, %s, %s, %s, 'cho_xac_nhan', %s, %s, %s, %s, 'chua_thanh_toan',
                               %s, %s, %s, %s, %s)
                       RETURNING *""",
                    (
                        phong_id, nguoi_dung_id, tin_nhan or None, sdt, ngay_vao, thoi_han,
                        max(int(so_nguoi or 1), 1), coc, gia, het_han, ket_thuc, ma_id, giam,
                    ),
                )
            except IntegrityError as exc:
                raise LoiDon('Phòng đang được giữ.') from exc
            don = cursor.fetchone()
            tao_thong_bao(
                cursor,
                phong['nguoi_dung_id'],
                'Có đơn đặt phòng mới',
                f"Đơn #{don['id']} giữ đến {het_han.strftime('%H:%M %d/%m')}.",
                '/admin/bookings',
            )
            return don
    except LoiDon:
        raise


def xac_nhan_don(conn, dat_id, chu_nha_id):
    with giao_dich(conn):
        cursor = get_cursor(conn)
        cursor.execute(
            """SELECT dp.*, p.nguoi_dung_id AS chu_id, p.tieu_de
               FROM dat_phong dp JOIN phong p ON p.id = dp.phong_id
               WHERE dp.id = %s FOR UPDATE OF dp""",
            (dat_id,),
        )
        don = cursor.fetchone()
        if not don or don['chu_id'] != chu_nha_id:
            raise LoiDon('Không tìm thấy đơn của bạn.')
        if don['trang_thai'] != 'cho_xac_nhan':
            raise LoiDon('Đơn không còn ở trạng thái chờ xác nhận.')
        _khoa_phong(cursor, don['phong_id'])
        cursor.execute(
            "UPDATE dat_phong SET trang_thai = 'da_xac_nhan' WHERE id = %s RETURNING *",
            (dat_id,),
        )
        don = cursor.fetchone()
        gia = float(don.get('gia_thue_chot') or 0) - float(don.get('giam_thang_dau') or 0)
        _dam_bao_khoan(cursor, don, 'tien_thue', max(gia, 0), 'chua_thu')
        cap_nhat_co_phong(cursor, don['phong_id'])
        tao_thong_bao(
            cursor, don['nguoi_dung_id'], 'Đơn đặt phòng đã được xác nhận',
            'Chủ nhà đã chấp nhận đơn của bạn.', '/my-bookings',
        )
        return don


def huy_don(conn, dat_id, nguoi_id, la_chu_nha=False, ly_do=None):
    with giao_dich(conn):
        cursor = get_cursor(conn)
        cursor.execute(
            """SELECT dp.*, p.nguoi_dung_id AS chu_id, p.tieu_de
               FROM dat_phong dp JOIN phong p ON p.id = dp.phong_id
               WHERE dp.id = %s FOR UPDATE OF dp""",
            (dat_id,),
        )
        don = cursor.fetchone()
        if not don:
            raise LoiDon('Không tìm thấy đơn.')
        if la_chu_nha and don['chu_id'] != nguoi_id:
            raise LoiDon('Không tìm thấy đơn của bạn.')
        if not la_chu_nha and don['nguoi_dung_id'] != nguoi_id:
            raise LoiDon('Không tìm thấy đơn của bạn.')
        if don['trang_thai'] not in ('cho_xac_nhan', 'da_xac_nhan'):
            raise LoiDon('Đơn này không hủy được.')
        _khoa_phong(cursor, don['phong_id'])
        cursor.execute(
            "UPDATE dat_phong SET trang_thai = 'da_huy', ly_do_huy = %s WHERE id = %s",
            (ly_do or None, dat_id),
        )
        cursor.execute(
            """UPDATE giao_dich_thanh_toan
               SET trang_thai = 'can_xem_lai'
               WHERE dat_phong_id = %s AND loai = 'coc' AND trang_thai = 'thanh_cong'""",
            (dat_id,),
        )
        cap_nhat_co_phong(cursor, don['phong_id'])
        nguoi_nhan = don['chu_id'] if not la_chu_nha else don['nguoi_dung_id']
        tieu_de = 'Đơn đặt phòng bị từ chối' if la_chu_nha else 'Đơn đặt phòng bị hủy'
        tao_thong_bao(cursor, nguoi_nhan, tieu_de, don['tieu_de'], '/my-bookings' if la_chu_nha else '/admin/bookings')
        return don


def tao_hop_dong(conn, chu_nha_id, phong_id, khach_id, ngay_bat_dau, ngay_ket_thuc, gia, coc):
    with giao_dich(conn):
        cursor = get_cursor(conn)
        phong = _khoa_phong(cursor, phong_id)
        if not phong or phong['nguoi_dung_id'] != chu_nha_id:
            raise LoiDon('Chọn phòng của bạn.')
        cursor.execute(
            """SELECT id FROM hop_dong_thue
               WHERE phong_id = %s AND trang_thai = 'dang_hieu_luc' LIMIT 1""",
            (phong_id,),
        )
        if cursor.fetchone():
            raise LoiDon('Phòng đang có hợp đồng hiệu lực.')
        cursor.execute(
            """SELECT id FROM dat_phong
               WHERE phong_id = %s AND nguoi_dung_id <> %s
                 AND trang_thai IN ('cho_xac_nhan', 'da_xac_nhan')
               LIMIT 1""",
            (phong_id, khach_id),
        )
        if cursor.fetchone():
            raise LoiDon('Phòng đang có đơn của người thuê khác.')
        cursor.execute(
            """INSERT INTO hop_dong_thue
               (phong_id, nguoi_dung_id, ngay_bat_dau, ngay_ket_thuc, gia_thue_hang_thang, tien_dat_coc, trang_thai)
               VALUES (%s, %s, %s, %s, %s, %s, 'dang_hieu_luc')
               RETURNING *""",
            (phong_id, khach_id, ngay_bat_dau, ngay_ket_thuc, gia, coc),
        )
        hop = cursor.fetchone()
        cap_nhat_co_phong(cursor, phong_id)
        tao_thong_bao(cursor, khach_id, 'Bạn có hợp đồng thuê mới', phong['tieu_de'], '/my-bookings')
        return hop


def ghi_nhan_coc(conn, dat_id):
    """Xác nhận cọc một lần. Lần sau trả lại trạng thái đã ghi."""
    with giao_dich(conn):
        cursor = get_cursor(conn)
        cursor.execute('SELECT * FROM dat_phong WHERE id = %s FOR UPDATE', (dat_id,))
        don = cursor.fetchone()
        if not don:
            raise LoiDon('Không tìm thấy đơn.')
        gd = _dam_bao_khoan(cursor, don, 'coc', float(don.get('tien_coc_phai_tra') or 0), 'vietqr')
        if gd['trang_thai'] == 'thanh_cong':
            return don, False
        cursor.execute(
            "UPDATE giao_dich_thanh_toan SET trang_thai = 'thanh_cong' WHERE id = %s",
            (gd['id'],),
        )
        cursor.execute(
            "UPDATE dat_phong SET trang_thai_thanh_toan = 'da_dat_coc' WHERE id = %s RETURNING *",
            (dat_id,),
        )
        don = cursor.fetchone()
        tao_thong_bao(
            cursor, don['nguoi_dung_id'], 'Đã ghi nhận tiền cọc',
            f"Đơn #{don['id']} đã được xác nhận đặt cọc.", '/my-bookings',
        )
        return don, True


def huy_don_qua_han(conn):
    """Hủy đơn chờ quá hạn nếu chưa nhận cọc."""
    with giao_dich(conn):
        cursor = get_cursor(conn)
        cursor.execute(
            """SELECT dp.id, dp.nguoi_dung_id, dp.phong_id, p.nguoi_dung_id AS chu_id, p.tieu_de
               FROM dat_phong dp
               JOIN phong p ON p.id = dp.phong_id
               WHERE dp.trang_thai = 'cho_xac_nhan'
                 AND dp.het_han_giu IS NOT NULL
                 AND dp.het_han_giu < NOW()
                 AND NOT EXISTS (
                     SELECT 1 FROM giao_dich_thanh_toan gd
                     WHERE gd.dat_phong_id = dp.id AND gd.loai = 'coc' AND gd.trang_thai = 'thanh_cong'
                 )
               FOR UPDATE OF dp"""
        )
        ds = cursor.fetchall()
        for don in ds:
            cursor.execute(
                "UPDATE dat_phong SET trang_thai = 'da_huy', ly_do_huy = 'het_han' WHERE id = %s",
                (don['id'],),
            )
            cap_nhat_co_phong(cursor, don['phong_id'])
            tao_thong_bao(
                cursor, don['nguoi_dung_id'], 'Đơn giữ chỗ đã hết hạn',
                don['tieu_de'], '/my-bookings',
            )
            tao_thong_bao(
                cursor, don['chu_id'], 'Đơn giữ chỗ đã hết hạn',
                don['tieu_de'], '/admin/bookings',
            )
        return len(ds)
