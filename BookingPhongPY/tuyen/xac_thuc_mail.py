"""Quên mật khẩu, Google và xác thực email. Route gắn vào blueprint auth."""
import hashlib
import secrets
from datetime import datetime, timedelta

from flask import flash, redirect, render_template, request, session, url_for
from werkzeug.security import generate_password_hash

from cau_hinh.cai_dat import FLASK_ENV, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.email_service import gui_email, loi_gui_gan_nhat, smtp_da_cau_hinh
from phu_tro.mo_rong import limiter, oauth
from tuyen.xac_thuc import auth_bp


def _bam_token(token):
    return hashlib.sha256(token.encode('utf-8')).hexdigest()


def _phat_link_xac_thuc(cursor, user_id, email):
    token = secrets.token_urlsafe(32)
    han = datetime.now() + timedelta(hours=24)
    cursor.execute(
        '''UPDATE nguoi_dung
           SET email_xac_thuc_token = %s, email_xac_thuc_het_han = %s
           WHERE id = %s''',
        (_bam_token(token), han, user_id),
    )
    link = url_for('auth.xac_nhan_email', token=token, _external=True)
    html = (
        '<p>Bấm liên kết này để xác thực email Booking Phòng Trọ. Liên kết hết hạn sau 24 giờ.</p>'
        f'<p><a href="{link}">{link}</a></p>'
    )
    da_gui = gui_email(email, 'Xác thực email Booking Phòng Trọ', html)
    session['email_da_gui'] = da_gui
    session['email_loi_gui'] = '' if da_gui else loi_gui_gan_nhat()
    session.pop('email_xac_thuc_link', None)
    if not da_gui and FLASK_ENV != 'production':
        session['email_xac_thuc_link'] = token
    return da_gui


@auth_bp.route('/quen-mat-khau', methods=['GET', 'POST'])
@limiter.limit('8 per minute')
def quen_mat_khau():
    error = ''
    thong_bao = ''
    link_dev = ''
    if request.method == 'POST':
        email = (request.form.get('email') or '').strip()
        conn = get_db_connection()
        cursor = get_cursor(conn)
        cursor.execute('SELECT id, email FROM nguoi_dung WHERE email = %s', (email,))
        user = cursor.fetchone()
        if not user:
            thong_bao = 'Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi.'
        else:
            token = secrets.token_urlsafe(32)
            han = datetime.now() + timedelta(minutes=30)
            cursor.execute(
                'UPDATE nguoi_dung SET reset_password_token = %s, reset_password_expire = %s WHERE id = %s',
                (_bam_token(token), han, user['id']),
            )
            conn.commit()
            link = url_for('auth.dat_lai_mat_khau', token=token, _external=True)
            da_gui = gui_email(user['email'], 'Đặt lại mật khẩu', f'<p>Bấm vào liên kết (30 phút): <a href="{link}">{link}</a></p>')
            thong_bao = 'Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi.'
            if not da_gui and FLASK_ENV != 'production':
                link_dev = link
    return render_template('auth/quen_mat_khau.html', error=error, thong_bao=thong_bao, link_dev=link_dev)


@auth_bp.route('/dat-lai-mat-khau/<token>', methods=['GET', 'POST'])
def dat_lai_mat_khau(token):
    error = ''
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(
        """SELECT id FROM nguoi_dung
           WHERE reset_password_token = %s AND reset_password_expire > NOW()""",
        (_bam_token(token),),
    )
    user = cursor.fetchone()
    if not user:
        flash('Liên kết đặt lại mật khẩu không còn hiệu lực.', 'error')
        return redirect(url_for('auth.quen_mat_khau'))
    if request.method == 'POST':
        password = request.form.get('password') or ''
        confirm = request.form.get('confirm_password') or ''
        if len(password) < 6:
            error = 'Mật khẩu phải có ít nhất 6 ký tự'
        elif password != confirm:
            error = 'Mật khẩu xác nhận không khớp'
        else:
            cursor.execute(
                """UPDATE nguoi_dung
                   SET mat_khau = %s, reset_password_token = NULL, reset_password_expire = NULL
                   WHERE id = %s""",
                (generate_password_hash(password), user['id']),
            )
            conn.commit()
            flash('Đã đổi mật khẩu. Hãy đăng nhập.', 'success')
            return redirect(url_for('auth.login'))
    return render_template('auth/dat_lai_mat_khau.html', error=error)


@auth_bp.route('/google')
def google_login():
    if not (GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET):
        flash('Chưa cấu hình GOOGLE_CLIENT_ID và GOOGLE_CLIENT_SECRET trong file .env.', 'error')
        return redirect(url_for('auth.login'))
    return oauth.google.authorize_redirect(url_for('auth.google_callback', _external=True))


@auth_bp.route('/google/callback')
def google_callback():
    if not (GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET):
        return redirect(url_for('auth.login'))
    try:
        token = oauth.google.authorize_access_token()
        profile = token.get('userinfo') or {}
    except Exception:
        flash('Đăng nhập Google không thành công.', 'error')
        return redirect(url_for('auth.login'))
    email = (profile.get('email') or '').strip()
    google_id = profile.get('sub')
    if not email:
        flash('Google không trả về email.', 'error')
        return redirect(url_for('auth.login'))
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute('SELECT * FROM nguoi_dung WHERE email = %s OR google_id = %s', (email, google_id))
    user = cursor.fetchone()
    if user and user.get('bi_khoa'):
        flash('Tài khoản đã bị khóa.', 'error')
        return redirect(url_for('auth.login'))
    if not user:
        cursor.execute(
            """INSERT INTO nguoi_dung (email, mat_khau, ho_ten, vai_tro, google_id, email_da_xac_thuc)
               VALUES (%s, %s, %s, 'nguoi_thue', %s, TRUE) RETURNING *""",
            (email, generate_password_hash(secrets.token_urlsafe(16)), profile.get('name') or email, google_id),
        )
        user = cursor.fetchone()
    else:
        cursor.execute(
            'UPDATE nguoi_dung SET google_id = %s, email_da_xac_thuc = TRUE WHERE id = %s',
            (google_id, user['id']),
        )
    session['user_id'] = user['id']
    session['vai_tro'] = user['vai_tro']
    conn.commit()
    if user['vai_tro'] == 'chu_nha':
        return redirect(url_for('admin.chu_nha_index'))
    return redirect(url_for('main.index'))


@auth_bp.route('/xac-thuc-email', methods=['GET', 'POST'])
@limiter.limit('8 per minute', methods=['POST'])
def xac_thuc_email():
    if 'user_id' not in session:
        return redirect(url_for('auth.login'))
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute('SELECT email, email_da_xac_thuc FROM nguoi_dung WHERE id = %s', (session['user_id'],))
    user = cursor.fetchone()
    if request.method == 'POST' and user and not user['email_da_xac_thuc']:
        _phat_link_xac_thuc(cursor, session['user_id'], user['email'])
        conn.commit()
        flash('Đã gửi lại email xác thực.' if session.get('email_da_gui') else 'Chưa gửi được email.', 'success' if session.get('email_da_gui') else 'error')
        return redirect(url_for('auth.xac_thuc_email'))
    link_dev = ''
    token = session.pop('email_xac_thuc_link', None)
    if token and FLASK_ENV != 'production':
        link_dev = url_for('auth.xac_nhan_email', token=token, _external=True)
    return render_template(
        'auth/xac_thuc_email.html',
        user=user,
        link_dev=link_dev,
        da_gui=session.get('email_da_gui', False),
        loi_gui=session.get('email_loi_gui', ''),
        smtp_san_sang=smtp_da_cau_hinh(),
    )


@auth_bp.route('/xac-nhan-email/<token>')
def xac_nhan_email(token):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(
        """UPDATE nguoi_dung
           SET email_da_xac_thuc = TRUE, email_xac_thuc_token = NULL, email_xac_thuc_het_han = NULL
           WHERE email_xac_thuc_token = %s AND email_xac_thuc_het_han > NOW()
           RETURNING id""",
        (_bam_token(token),),
    )
    row = cursor.fetchone()
    conn.commit()
    if not row:
        flash('Liên kết xác thực không còn hiệu lực.', 'error')
        return redirect(url_for('auth.login'))
    flash('Đã xác thực email. Bạn có thể đặt phòng.', 'success')
    return redirect(url_for('main.index'))
