"""
Tạo tài khoản admin lần đầu, hoặc đổi mật khẩu khi có ADMIN_PASSWORD.
Chạy: python kich_ban/tao_admin.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from werkzeug.security import generate_password_hash

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection


def main():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    ten_dang_nhap = 'admin'
    ho_ten = 'Administrator'
    email = 'admin@booking.local'
    production = os.environ.get('FLASK_ENV') == 'production'
    mat_khau = (os.environ.get('ADMIN_PASSWORD') or '').strip()

    cursor.execute("SELECT id FROM admin WHERE ten_dang_nhap = %s", (ten_dang_nhap,))
    existing = cursor.fetchone()

    if not mat_khau:
        if production:
            print('Thieu ADMIN_PASSWORD. Khong tao admin bang mat khau mac dinh tren production.', file=sys.stderr)
            conn.close()
            sys.exit(1)
        if existing:
            print('Admin da ton tai. Dat ADMIN_PASSWORD trong moi truong roi chay lai neu muon doi mat khau.')
            conn.close()
            return
        mat_khau = 'admin123'

    if production and (mat_khau == 'admin123' or len(mat_khau) < 10):
        print('ADMIN_PASSWORD qua yeu. Can it nhat 10 ky tu va khac admin123.', file=sys.stderr)
        conn.close()
        sys.exit(1)

    hashed = generate_password_hash(mat_khau)
    if existing:
        cursor.execute(
            """UPDATE admin
               SET mat_khau = %s, ho_ten = %s, email = %s, vai_tro = 'super_admin', trang_thai = 'active'
               WHERE ten_dang_nhap = %s""",
            (hashed, ho_ten, email, ten_dang_nhap),
        )
        print('Da cap nhat tai khoan admin.')
    else:
        cursor.execute(
            """INSERT INTO admin (ten_dang_nhap, mat_khau, ho_ten, email, vai_tro, trang_thai)
               VALUES (%s, %s, %s, %s, 'super_admin', 'active')""",
            (ten_dang_nhap, hashed, ho_ten, email),
        )
        if mat_khau == 'admin123':
            print('Da tao admin / admin123 cho may dev. Doi mat khau truoc khi dua ra ngoai.')
        else:
            print('Da tao tai khoan admin.')
    conn.commit()
    conn.close()


if __name__ == '__main__':
    main()
