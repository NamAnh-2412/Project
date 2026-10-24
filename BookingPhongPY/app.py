"""Booking Phòng Trọ - ứng dụng Flask."""
import os
import time
from datetime import datetime, timedelta

from flask import Flask, flash, jsonify, redirect, request, send_from_directory, session, url_for
from werkzeug.middleware.proxy_fix import ProxyFix

from cau_hinh.cai_dat import (
    FLASK_ENV,
    GOOGLE_CLIENT_ID,
    GOOGLE_CLIENT_SECRET,
    SECRET_KEY,
    UPLOAD_DIR,
    WEBHOOK_SECRET,
)
from cau_hinh.co_so_du_lieu import giai_phong_ket_noi, get_cursor, get_db_connection
from cau_hinh.van_hanh import kiem_tra_van_hanh
from phu_tro.mo_rong import csrf, init_oauth, limiter


def create_app():
    app = Flask(__name__, template_folder='mau', static_folder='tinh')
    app.secret_key = SECRET_KEY
    app.config['MAX_CONTENT_LENGTH'] = 8 * 1024 * 1024
    app.config['WTF_CSRF_TIME_LIMIT'] = None
    app.config['GOOGLE_CLIENT_ID'] = GOOGLE_CLIENT_ID
    app.config['GOOGLE_CLIENT_SECRET'] = GOOGLE_CLIENT_SECRET
    app.config['WEBHOOK_SECRET'] = WEBHOOK_SECRET
    app.config['SESSION_COOKIE_HTTPONLY'] = True
    app.config['SESSION_COOKIE_SAMESITE'] = 'Lax'
    app.config['SESSION_COOKIE_SECURE'] = os.environ.get('COOKIE_SECURE') == '1'
    app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(days=7)
    app.config['RATELIMIT_STORAGE_URI'] = os.environ.get('RATELIMIT_STORAGE_URI', 'memory://')
    app.config['RATELIMIT_SWALLOW_ERRORS'] = True
    app.config['RATELIMIT_IN_MEMORY_FALLBACK_ENABLED'] = True
    if os.environ.get('TESTING') == '1' or FLASK_ENV == 'testing':
        app.config['RATELIMIT_ENABLED'] = False
    if os.environ.get('BEHIND_PROXY') == '1':
        app.wsgi_app = ProxyFix(app.wsgi_app, x_for=1, x_proto=1, x_host=1)

    csrf.init_app(app)
    limiter.init_app(app)
    init_oauth(app)

    from tuyen.trang_chu import main_bp
    from tuyen.xac_thuc import auth_bp
    from tuyen.phong import rooms_bp
    from tuyen.quan_tri import admin_bp
    from tuyen.api import api_bp
    from tuyen.tai_khoan import tai_khoan_bp
    from tuyen.dat_cho import dat_cho_bp
    from tuyen.tuong_tac import tuong_tac_bp
    from tuyen.chu_nha_pms import pms_bp
    from tuyen.quan_tri_mo_rong import admin_ext_bp

    app.register_blueprint(main_bp)
    app.register_blueprint(auth_bp, url_prefix='/auth')
    app.register_blueprint(rooms_bp)
    app.register_blueprint(admin_bp, url_prefix='/admin')
    app.register_blueprint(api_bp, url_prefix='/api')
    app.register_blueprint(tai_khoan_bp)
    app.register_blueprint(dat_cho_bp)
    app.register_blueprint(tuong_tac_bp)
    app.register_blueprint(pms_bp, url_prefix='/chu-nha')
    app.register_blueprint(admin_ext_bp, url_prefix='/admin')

    @app.context_processor
    def inject_helpers():
        from phu_tro.decorators_xac_thuc import (
            get_current_admin,
            get_current_user,
            is_admin,
            is_chu_nha,
            is_logged_in,
            is_nguoi_thue,
        )
        from phu_tro.tro_giup import (
            dinh_dang_tien,
            get_trang_thai_booking_info,
            get_trang_thai_phong_text,
            nhan_loai_phong,
            url_anh,
        )

        def _get_user():
            conn = get_db_connection()
            return get_current_user(conn)

        def _get_admin():
            conn = get_db_connection()
            return get_current_admin(conn)

        def _is_chu_nha():
            conn = get_db_connection()
            return is_chu_nha(conn)

        def _is_nguoi_thue():
            conn = get_db_connection()
            return is_nguoi_thue(conn)

        def _dem_trong_phien(khoa, cau_sql, params):
            now = time.time()
            hop = session.get(khoa)
            if isinstance(hop, list) and len(hop) == 2 and now - float(hop[0]) < 20:
                return int(hop[1])
            conn = get_db_connection()
            cur = get_cursor(conn)
            cur.execute(cau_sql, params)
            so = int(cur.fetchone()['c'] or 0)
            session[khoa] = [now, so]
            return so

        def _so_thong_bao():
            if 'user_id' not in session:
                return 0
            try:
                return _dem_trong_phien(
                    '_tb',
                    "SELECT COUNT(*) AS c FROM thong_bao WHERE nguoi_dung_id = %s AND da_doc = FALSE",
                    (session['user_id'],),
                )
            except Exception:
                return 0

        def _so_cho_duyet():
            if 'admin_id' not in session:
                return 0
            try:
                return _dem_trong_phien(
                    '_duyet',
                    "SELECT COUNT(*) AS c FROM phong WHERE trang_thai_duyet = 'cho_duyet'",
                    (),
                )
            except Exception:
                return 0

        return dict(
            get_current_user=_get_user,
            get_current_admin=_get_admin,
            is_logged_in=is_logged_in,
            is_admin=is_admin,
            is_chu_nha=_is_chu_nha,
            is_nguoi_thue=_is_nguoi_thue,
            get_trang_thai_phong_text=get_trang_thai_phong_text,
            get_trang_thai_booking_info=get_trang_thai_booking_info,
            nhan_loai_phong=nhan_loai_phong,
            dinh_dang_tien=dinh_dang_tien,
            so_thong_bao=_so_thong_bao,
            so_cho_duyet=_so_cho_duyet,
            current_year=datetime.now().year,
            google_oauth_san_sang=bool(GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET),
            image_url=url_anh,
        )

    @app.route('/suc-khoe')
    def suc_khoe():
        try:
            conn = get_db_connection()
            cur = get_cursor(conn)
            cur.execute('SELECT 1 AS ok')
            cur.fetchone()
            return jsonify({'ok': True})
        except Exception:
            return jsonify({'ok': False}), 503

    @app.route('/uploads/<path:filename>')
    def serve_upload(filename):
        os.makedirs(UPLOAD_DIR, exist_ok=True)
        return send_from_directory(UPLOAD_DIR, filename)

    @app.before_request
    def _chan_tai_khoan_khoa():
        if request.endpoint in (
            'auth.login', 'auth.logout', 'auth.register', 'auth.xac_thuc_email',
            'auth.xac_nhan_email', 'static', 'serve_upload',
        ):
            return None
        if 'user_id' in session and 'admin_id' not in session and request.endpoint:
            try:
                conn = get_db_connection()
                cur = get_cursor(conn)
                cur.execute("SELECT bi_khoa FROM nguoi_dung WHERE id = %s", (session['user_id'],))
                row = cur.fetchone()
                if row and row.get('bi_khoa'):
                    session.clear()
                    flash('Tài khoản đã bị khóa.', 'error')
                    return redirect(url_for('auth.login'))
            except Exception:
                return None
        return None

    app.teardown_appcontext(giai_phong_ket_noi)
    if os.environ.get('TESTING') != '1' and FLASK_ENV != 'testing':
        kiem_tra_van_hanh()
    return app


app = create_app()


if __name__ == '__main__':
    if os.environ.get('WERKZEUG_RUN_MAIN') == 'true':
        from dich_vu.lich_trinh import khoi_dong_lich
        khoi_dong_lich()
    app.run(debug=True, port=5000)
