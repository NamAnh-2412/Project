"""
Booking Phòng Trọ - Flask Application
Chuyển đổi từ PHP (PhanMemBookingPhong)
"""
import os
from datetime import datetime
from flask import Flask, url_for
from cau_hinh.cai_dat import BASE_DIR

app = Flask(__name__, template_folder='mau', static_folder='tinh')
app.secret_key = os.environ.get('SECRET_KEY', 'booking-phong-secret-key-change-in-production')

# Đăng ký blueprints
from tuyen.trang_chu import main_bp
from tuyen.xac_thuc import auth_bp
from tuyen.phong import rooms_bp
from tuyen.quan_tri import admin_bp
from tuyen.api import api_bp

app.register_blueprint(main_bp)
app.register_blueprint(auth_bp, url_prefix='/auth')
app.register_blueprint(rooms_bp)
app.register_blueprint(admin_bp, url_prefix='/admin')
app.register_blueprint(api_bp, url_prefix='/api')

# Context processor - đưa helpers vào tất cả templates
@app.context_processor
def inject_helpers():
    from phu_tro.decorators_xac_thuc import get_current_user, get_current_admin, is_logged_in, is_admin, is_chu_nha, is_nguoi_thue
    from cau_hinh.co_so_du_lieu import get_db_connection
    from phu_tro.tro_giup import get_trang_thai_phong_text, get_trang_thai_booking_info

    def _get_user():
        conn = get_db_connection()
        try:
            return get_current_user(conn)
        finally:
            conn.close()

    def _get_admin():
        conn = get_db_connection()
        try:
            return get_current_admin(conn)
        finally:
            conn.close()

    def _is_chu_nha():
        conn = get_db_connection()
        try:
            return is_chu_nha(conn)
        finally:
            conn.close()

    def _is_nguoi_thue():
        conn = get_db_connection()
        try:
            return is_nguoi_thue(conn)
        finally:
            conn.close()

    return dict(
        get_current_user=_get_user,
        get_current_admin=_get_admin,
        is_logged_in=is_logged_in,
        is_admin=is_admin,
        is_chu_nha=_is_chu_nha,
        is_nguoi_thue=_is_nguoi_thue,
        get_trang_thai_phong_text=get_trang_thai_phong_text,
        get_trang_thai_booking_info=get_trang_thai_booking_info,
        current_year=datetime.now().year,
        image_url=lambda path: url_for('serve_upload', filename=path.replace('uploads/', '', 1)) if path and path.startswith('uploads/') else (url_for('static', filename=path.replace('assets/', '', 1)) if path and path.startswith('assets/') else (url_for('static', filename=path) if path else url_for('static', filename='images/no-image.svg'))),
    )


# Serve static uploads
from flask import send_from_directory

UPLOAD_FOLDER = os.path.join(BASE_DIR, 'uploads')
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

@app.route('/uploads/<path:filename>')
def serve_upload(filename):
    return send_from_directory(UPLOAD_FOLDER, filename)


if __name__ == '__main__':
    app.run(debug=True, port=5000)
