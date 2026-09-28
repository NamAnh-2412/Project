"""Kết nối database PostgreSQL"""
import os
import psycopg2
from psycopg2.extras import RealDictCursor

# Cấu hình database PostgreSQL (có thể override bằng biến môi trường)
DB_CONFIG = {
    'host': os.environ.get('DB_HOST', 'localhost'),
    'port': int(os.environ.get('DB_PORT', 5432)),
    'user': os.environ.get('DB_USER', 'postgres'),
    'password': os.environ.get('DB_PASSWORD', '24120510'),  # ⚠️ ĐỔI thành mật khẩu PostgreSQL của bạn
    'dbname': os.environ.get('DB_NAME', 'bookingphong'),
    'options': '-c client_encoding=UTF8',
}


def get_db_connection():
    """Tạo kết nối database PostgreSQL"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = True
        return conn
    except psycopg2.Error as e:
        raise Exception(f"Kết nối database thất bại: {e}")


def get_cursor(conn):
    """Lấy cursor với kết quả dạng dict (RealDictCursor)"""
    return conn.cursor(cursor_factory=RealDictCursor)
