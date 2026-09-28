"""
Script tạo/cập nhật tài khoản Admin mặc định
Chạy: python kich_ban/tao_admin.py
"""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from werkzeug.security import generate_password_hash
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor

def main():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    
    ten_dang_nhap = 'admin'
    mat_khau = 'admin123'
    ho_ten = 'Administrator'
    email = 'admin@booking.local'
    
    hashed = generate_password_hash(mat_khau)
    
    cursor.execute("SELECT id FROM admin WHERE ten_dang_nhap = %s", (ten_dang_nhap,))
    existing = cursor.fetchone()
    
    if existing:
        cursor.execute(
            "UPDATE admin SET mat_khau = %s, ho_ten = %s, email = %s, vai_tro = 'super_admin', trang_thai = 'active' WHERE ten_dang_nhap = %s",
            (hashed, ho_ten, email, ten_dang_nhap)
        )
        conn.commit()
        print(f"Đã cập nhật mật khẩu Admin: {ten_dang_nhap} / {mat_khau}")
    else:
        cursor.execute(
            "INSERT INTO admin (ten_dang_nhap, mat_khau, ho_ten, email, vai_tro, trang_thai) VALUES (%s, %s, %s, %s, 'super_admin', 'active')",
            (ten_dang_nhap, hashed, ho_ten, email)
        )
        conn.commit()
        print(f"Đã tạo tài khoản Admin: {ten_dang_nhap} / {mat_khau}")
    
    print("⚠️ Hãy đổi mật khẩu sau khi đăng nhập!")
    conn.close()

if __name__ == '__main__':
    main()
