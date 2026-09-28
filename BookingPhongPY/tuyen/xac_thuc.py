"""Tuyến xác thực: đăng nhập, đăng ký, đăng xuất"""
import re
try:
    import bcrypt
except ImportError:
    bcrypt = None
from flask import Blueprint, render_template, request, redirect, url_for, session
from werkzeug.security import check_password_hash, generate_password_hash
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor
from phu_tro.decorators_xac_thuc import is_logged_in, is_admin, get_current_user, get_current_admin

auth_bp = Blueprint('auth', __name__)


def _verify_password(stored_hash: str | None, password: str) -> bool:
    if not stored_hash or not password:
        return False
    stored_hash = str(stored_hash).strip()
    if stored_hash.startswith('$2') and stored_hash[1:3] in ('2y', '2a', '2b'):
        if bcrypt is None:
            raise ImportError("Cần cài bcrypt: pip install bcrypt")
        try:
            return bcrypt.checkpw(password.encode('utf-8'), stored_hash.encode('utf-8'))
        except Exception:
            return False
    try:
        return check_password_hash(stored_hash, password)
    except (ValueError, TypeError):
        return False


@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if is_logged_in():
        if is_admin():
            return redirect(url_for('admin.dashboard'))
        conn = get_db_connection()
        user = get_current_user(conn)
        conn.close()
        if user and user.get('vai_tro') == 'chu_nha':
            return redirect(url_for('admin.chu_nha_index'))
        return redirect(url_for('main.index'))

    error = ''
    if request.method == 'POST':
        email = (request.form.get('email') or '').strip()
        password = request.form.get('password') or ''
        login_type = request.form.get('login_type', 'user')

        if not email or not password:
            error = 'Vui lòng nhập thông tin đăng nhập'
        else:
            conn = get_db_connection()
            cursor = get_cursor(conn)
            try:
                if login_type == 'admin':
                    cursor.execute(
                        "SELECT * FROM admin WHERE ten_dang_nhap = %s AND trang_thai = 'active'",
                        (email,)
                    )
                    admin = cursor.fetchone()
                    if admin:
                        try:
                            pw_ok = admin['mat_khau'] == password or _verify_password(admin['mat_khau'], password)
                        except ImportError as e:
                            error = str(e) + " - Khởi động lại web sau khi cài."
                            pw_ok = False
                        if pw_ok:
                            if admin['mat_khau'] == password:
                                hashed = generate_password_hash(password)
                                cursor.execute("UPDATE admin SET mat_khau = %s WHERE id = %s", (hashed, admin['id']))
                                conn.commit()
                            session['admin_id'] = admin['id']
                            cursor.execute("UPDATE admin SET lan_dang_nhap_cuoi = NOW() WHERE id = %s", (admin['id'],))
                            conn.commit()
                            return redirect(url_for('admin.dashboard'))
                    if not error:
                        error = 'Tên đăng nhập hoặc mật khẩu không đúng'
                else:
                    cursor.execute("SELECT * FROM nguoi_dung WHERE email = %s", (email,))
                    user = cursor.fetchone()
                    try:
                        user_ok = user and _verify_password(user['mat_khau'], password)
                    except ImportError as e:
                        error = str(e) + " - Khởi động lại web sau khi cài."
                        user_ok = False
                    if user_ok:
                        session['user_id'] = user['id']
                        session['vai_tro'] = user['vai_tro']
                        if user['vai_tro'] == 'chu_nha':
                            return redirect(url_for('admin.chu_nha_index'))
                        return redirect(url_for('main.index'))
                    if not error:
                        error = 'Email hoặc mật khẩu không đúng'
            finally:
                conn.close()

    return render_template('auth/login.html', error=error)


@auth_bp.route('/register', methods=['GET', 'POST'])
def register():
    error = ''
    success = ''
    if request.method == 'POST':
        email = (request.form.get('email') or '').strip()
        password = request.form.get('password') or ''
        confirm_password = request.form.get('confirm_password') or ''
        ho_ten = (request.form.get('ho_ten') or '').strip()
        so_dien_thoai = (request.form.get('so_dien_thoai') or '').strip()
        vai_tro = request.form.get('vai_tro', 'nguoi_thue')

        if not email or not password or not ho_ten:
            error = 'Vui lòng điền đầy đủ thông tin bắt buộc'
        elif password != confirm_password:
            error = 'Mật khẩu xác nhận không khớp'
        elif len(password) < 6:
            error = 'Mật khẩu phải có ít nhất 6 ký tự'
        elif not re.match(r'^[\w\.-]+@[\w\.-]+\.\w+$', email):
            error = 'Email không hợp lệ'
        else:
            conn = get_db_connection()
            cursor = get_cursor(conn)
            try:
                cursor.execute("SELECT id FROM nguoi_dung WHERE email = %s", (email,))
                if cursor.fetchone():
                    error = 'Email đã tồn tại'
                else:
                    hashed = generate_password_hash(password)
                    cursor.execute(
                        "INSERT INTO nguoi_dung (email, mat_khau, ho_ten, so_dien_thoai, vai_tro) VALUES (%s, %s, %s, %s, %s)",
                        (email, hashed, ho_ten, so_dien_thoai or None, vai_tro)
                    )
                    conn.commit()
                    success = 'Đăng ký thành công! <a href="' + url_for('auth.login') + '">Đăng nhập ngay</a>'
            finally:
                conn.close()

    return render_template('auth/register.html', error=error, success=success)


@auth_bp.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('main.index'))
