"""Decorators kiểm tra quyền - tương đương requireLogin, requireAdmin, etc."""
from functools import wraps
from flask import session, redirect, url_for, flash, request

from cau_hinh.co_so_du_lieu import get_cursor


def get_current_user(conn):
    """Lấy thông tin user hiện tại từ session"""
    user_id = session.get('user_id')
    if not user_id:
        return None
    cursor = get_cursor(conn)
    cursor.execute("SELECT * FROM nguoi_dung WHERE id = %s", (user_id,))
    return cursor.fetchone()


def get_current_admin(conn):
    """Lấy thông tin admin hiện tại từ session"""
    admin_id = session.get('admin_id')
    if not admin_id:
        return None
    cursor = get_cursor(conn)
    cursor.execute("SELECT * FROM admin WHERE id = %s", (admin_id,))
    return cursor.fetchone()


def is_logged_in():
    """Kiểm tra đã đăng nhập chưa"""
    return 'user_id' in session or 'admin_id' in session


def is_admin():
    """Kiểm tra có phải Admin không"""
    return 'admin_id' in session


def is_chu_nha(conn):
    """Kiểm tra có phải Chủ nhà không"""
    user = get_current_user(conn)
    return user and user.get('vai_tro') == 'chu_nha'


def is_nguoi_thue(conn):
    """Kiểm tra có phải Người thuê không"""
    user = get_current_user(conn)
    return user and user.get('vai_tro') == 'nguoi_thue'


def require_login(f):
    """Decorator: yêu cầu đăng nhập"""
    @wraps(f)
    def decorated(*args, **kwargs):
        if not is_logged_in():
            return redirect(url_for('auth.login'))
        return f(*args, **kwargs)
    return decorated


def require_admin(f):
    """Decorator: chỉ Admin"""
    @wraps(f)
    def decorated(*args, **kwargs):
        if not is_admin():
            flash('Bạn không có quyền truy cập trang Quản trị!', 'error')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated


def require_chu_nha(f):
    """Decorator: chỉ Chủ nhà"""
    @wraps(f)
    def decorated(*args, **kwargs):
        from cau_hinh.co_so_du_lieu import get_db_connection
        conn = get_db_connection()
        try:
            if not is_logged_in():
                return redirect(url_for('auth.login'))
            if not is_chu_nha(conn):
                flash('Trang này chỉ dành cho Chủ nhà!', 'error')
                return redirect(url_for('main.index'))
            return f(*args, **kwargs)
        finally:
            conn.close()
    return decorated


def require_nguoi_thue(f):
    """Decorator: chỉ Người thuê"""
    @wraps(f)
    def decorated(*args, **kwargs):
        from cau_hinh.co_so_du_lieu import get_db_connection
        conn = get_db_connection()
        try:
            if not is_logged_in():
                return redirect(url_for('auth.login'))
            if not is_nguoi_thue(conn):
                return redirect(url_for('main.index'))
            return f(*args, **kwargs)
        finally:
            conn.close()
    return decorated
