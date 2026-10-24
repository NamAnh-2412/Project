"""Hồ sơ cá nhân."""
from flask import Blueprint, flash, redirect, render_template, request, url_for
from werkzeug.security import check_password_hash, generate_password_hash

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.upload_service import luu_anh_dai_dien, xoa_tep
from phu_tro.bao_mat import lam_sach
from phu_tro.decorators_xac_thuc import get_current_user, require_login

tai_khoan_bp = Blueprint('tai_khoan', __name__)


@tai_khoan_bp.route('/tai-khoan', methods=['GET', 'POST'])
@require_login
def ho_so():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        flash('Tài khoản quản trị đổi mật khẩu bằng script kich_ban/tao_admin.py.', 'error')
        return redirect(url_for('admin.dashboard'))

    error = ''
    if request.method == 'POST':
        ho_ten = lam_sach(request.form.get('ho_ten'), 100)
        sdt = lam_sach(request.form.get('so_dien_thoai'), 20)
        cccd = lam_sach(request.form.get('cccd_so'), 20)
        mat_khau_cu = request.form.get('mat_khau_cu') or ''
        mat_khau_moi = request.form.get('mat_khau_moi') or ''
        if not ho_ten:
            error = 'Họ tên không được để trống'
        else:
            cursor.execute(
                """UPDATE nguoi_dung
                   SET ho_ten = %s, so_dien_thoai = %s, cccd_so = %s
                   WHERE id = %s""",
                (ho_ten, sdt or None, cccd or None, user['id']),
            )
            anh = request.files.get('anh_dai_dien')
            if anh and anh.filename:
                path = luu_anh_dai_dien(anh, user['id'])
                if path:
                    xoa_tep(user.get('anh_dai_dien'))
                    cursor.execute('UPDATE nguoi_dung SET anh_dai_dien = %s WHERE id = %s', (path, user['id']))
            if mat_khau_moi:
                if len(mat_khau_moi) < 6:
                    error = 'Mật khẩu mới phải có ít nhất 6 ký tự'
                else:
                    try:
                        cu_ok = check_password_hash(user['mat_khau'], mat_khau_cu)
                    except Exception:
                        cu_ok = False
                    if not cu_ok:
                        from tuyen.xac_thuc import _verify_password
                        cu_ok = _verify_password(user['mat_khau'], mat_khau_cu)
                    if not cu_ok:
                        error = 'Mật khẩu hiện tại không đúng'
                    else:
                        cursor.execute(
                            'UPDATE nguoi_dung SET mat_khau = %s WHERE id = %s',
                            (generate_password_hash(mat_khau_moi), user['id']),
                        )
            if not error:
                from flask import g
                g._user_cached = False
                conn.commit()
                flash('Đã cập nhật hồ sơ.', 'success')
                return redirect(url_for('tai_khoan.ho_so'))
        cursor.execute('SELECT * FROM nguoi_dung WHERE id = %s', (user['id'],))
        user = cursor.fetchone()

    return render_template('tai_khoan/ho_so.html', user=user, error=error)
