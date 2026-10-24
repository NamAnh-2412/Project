"""Kiểm tra trước khi chạy nhiều người cùng lúc."""
import os

from cau_hinh.cai_dat import FLASK_ENV, SECRET_KEY

KHOA_YEU = {
    '',
    'dev-only-doi-trong-env',
    'doi-khi-trien-khai',
    'doi-thanh-chuoi-ngau-nhien-dai',
}
MAT_KHAU_ADMIN_MAC_DINH = 'admin123'


def khoa_bi_mat_yeu(secret):
    chuoi = (secret or '').strip()
    return chuoi in KHOA_YEU or len(chuoi) < 24


def _co_admin_mat_khau_mac_dinh():
    from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
    from tuyen.xac_thuc import _verify_password

    conn = get_db_connection()
    try:
        cur = get_cursor(conn)
        cur.execute("SELECT mat_khau FROM admin WHERE trang_thai = 'active'")
        for row in cur.fetchall():
            if _verify_password(row.get('mat_khau'), MAT_KHAU_ADMIN_MAC_DINH):
                return True
        return False
    finally:
        conn.close()


def kiem_tra_van_hanh():
    """Production thì dừng hẳn. Bản dev chỉ cảnh báo mật khẩu admin mặc định."""
    if FLASK_ENV == 'production':
        if khoa_bi_mat_yeu(SECRET_KEY):
            raise RuntimeError(
                'SECRET_KEY còn là giá trị mẫu hoặc ngắn hơn 24 ký tự. Đặt chuỗi ngẫu nhiên trong .env trước khi chạy production.'
            )
        if _co_admin_mat_khau_mac_dinh():
            raise RuntimeError(
                'Vẫn còn admin dùng mật khẩu admin123. Đặt ADMIN_PASSWORD rồi chạy python kich_ban/tao_admin.py.'
            )
        return
    try:
        if _co_admin_mat_khau_mac_dinh():
            print('Cảnh báo: admin vẫn dùng mật khẩu admin123. Chỉ để vậy trên máy dev.')
    except Exception:
        return
