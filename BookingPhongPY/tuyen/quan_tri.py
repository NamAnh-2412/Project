"""Danh sách của admin. Phòng của chủ nhà nằm ở chu_nha_phong.py."""
from urllib.parse import urlencode

from flask import Blueprint, current_app, flash, redirect, render_template, request, session, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.thong_ke_service import thong_ke_admin
from dich_vu.upload_service import xoa_phong_va_tep
from phu_tro.decorators_xac_thuc import get_current_admin, require_admin, require_login
from phu_tro.tro_giup import get_anh_chinh_subquery, get_anh_dau_tien_subquery, thong_tin_trang

admin_bp = Blueprint('admin', __name__)


@admin_bp.route('/')
@require_login
def index():
    if 'admin_id' in session:
        return redirect(url_for('admin.dashboard'))
    return redirect(url_for('admin.chu_nha_index'))


@admin_bp.route('/dashboard')
@require_admin
def dashboard():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    stats, bieu_do = thong_ke_admin(cursor)
    admin = get_current_admin(conn)
    return render_template('admin/dashboard.html', stats=stats, admin=admin, bieu_do=bieu_do)


@admin_bp.route('/manage-phong')
@require_admin
def manage_phong():
    conn = get_db_connection()
    cursor = get_cursor(conn)

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        try:
            xoa_phong_va_tep(cursor, delete_id)
            conn.commit()
            flash('Đã xóa phòng thành công!', 'success')
        except Exception:
            current_app.logger.exception('Không xóa được phòng')
            flash('Không xóa được phòng.', 'error')
        return redirect(url_for('admin.manage_phong'))

    cursor.execute('SELECT COUNT(*) AS total FROM phong')
    trang = thong_tin_trang(request.args.get('page', 1), cursor.fetchone()['total'], per_page=10)
    cursor.execute(f"""
        SELECT p.*, nd.ho_ten as chu_nha_ten, nd.email as chu_nha_email,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        ORDER BY p.id DESC
        LIMIT %s OFFSET %s
    """, (trang['per_page'], trang['offset']))
    phong_list = cursor.fetchall()
    return render_template(
        'admin/manage_phong.html',
        phong_list=phong_list,
        phan_trang=trang,
        tham_so=urlencode([(k, v) for k, v in request.args.items(multi=True) if k != 'page']),
    )


@admin_bp.route('/manage-chu-nha')
@require_admin
def manage_chu_nha():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute("""
        SELECT nd.*, COUNT(p.id) as so_phong,
               COUNT(CASE WHEN p.trang_thai = 'con_trong' THEN 1 END) as phong_con_trong,
               COUNT(CASE WHEN p.trang_thai = 'da_thue' THEN 1 END) as phong_da_thue
        FROM nguoi_dung nd
        LEFT JOIN phong p ON nd.id = p.nguoi_dung_id
        WHERE nd.vai_tro = 'chu_nha'
        GROUP BY nd.id
        ORDER BY nd.id DESC
    """)
    chu_nha_list = cursor.fetchall()

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        cursor.execute("SELECT COUNT(*) as count FROM phong WHERE nguoi_dung_id = %s", (delete_id,))
        if cursor.fetchone()['count'] > 0:
            flash('Không thể xóa chủ nhà này vì đang có phòng trọ.', 'error')
        else:
            cursor.execute("DELETE FROM nguoi_dung WHERE id = %s AND vai_tro = 'chu_nha'", (delete_id,))
            conn.commit()
            flash('Đã xóa chủ nhà thành công!', 'success')
        return redirect(url_for('admin.manage_chu_nha'))

    return render_template('admin/manage_chu_nha.html', chu_nha_list=chu_nha_list)


@admin_bp.route('/view-chu-nha/<int:chu_nha_id>')
@require_admin
def view_chu_nha(chu_nha_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute("SELECT * FROM nguoi_dung WHERE id = %s AND vai_tro = 'chu_nha'", (chu_nha_id,))
    chu_nha = cursor.fetchone()
    if not chu_nha:
        return redirect(url_for('admin.manage_chu_nha'))

    cursor.execute(f"""
        SELECT p.*, {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
        FROM phong p
        WHERE p.nguoi_dung_id = %s
        ORDER BY p.id DESC
    """, (chu_nha_id,))
    phong_list = cursor.fetchall()

    stats = {'total_rooms': len(phong_list), 'available_rooms': 0, 'rented_rooms': 0, 'total_bookings': 0}
    for phong in phong_list:
        if phong['trang_thai'] == 'con_trong':
            stats['available_rooms'] += 1
        else:
            stats['rented_rooms'] += 1

    cursor.execute("""
        SELECT COUNT(*) as total FROM dat_phong dp
        INNER JOIN phong p ON dp.phong_id = p.id
        WHERE p.nguoi_dung_id = %s
    """, (chu_nha_id,))
    stats['total_bookings'] = cursor.fetchone()['total'] or 0

    return render_template('admin/view_chu_nha.html', chu_nha=chu_nha, phong_list=phong_list, stats=stats)


@admin_bp.route('/manage-nguoi-thue')
@require_admin
def manage_nguoi_thue():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute("""
        SELECT nd.*, COUNT(dp.id) as so_booking,
               COUNT(CASE WHEN dp.trang_thai = 'cho_xac_nhan' THEN 1 END) as booking_cho_xac_nhan,
               COUNT(CASE WHEN dp.trang_thai = 'da_xac_nhan' THEN 1 END) as booking_da_xac_nhan
        FROM nguoi_dung nd
        LEFT JOIN dat_phong dp ON nd.id = dp.nguoi_dung_id
        WHERE nd.vai_tro = 'nguoi_thue'
        GROUP BY nd.id
        ORDER BY nd.id DESC
    """)
    nguoi_thue_list = cursor.fetchall()

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        cursor.execute("SELECT COUNT(*) as count FROM dat_phong WHERE nguoi_dung_id = %s", (delete_id,))
        if cursor.fetchone()['count'] > 0:
            flash('Không thể xóa người thuê này vì đang có đơn đặt phòng.', 'error')
        else:
            cursor.execute("DELETE FROM yeu_thich WHERE nguoi_dung_id = %s", (delete_id,))
            cursor.execute("DELETE FROM nguoi_dung WHERE id = %s AND vai_tro = 'nguoi_thue'", (delete_id,))
            conn.commit()
            flash('Đã xóa người thuê thành công!', 'success')
        return redirect(url_for('admin.manage_nguoi_thue'))

    return render_template('admin/manage_nguoi_thue.html', nguoi_thue_list=nguoi_thue_list)


@admin_bp.route('/view-nguoi-thue/<int:nguoi_thue_id>')
@require_admin
def view_nguoi_thue(nguoi_thue_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute("SELECT * FROM nguoi_dung WHERE id = %s AND vai_tro = 'nguoi_thue'", (nguoi_thue_id,))
    nguoi_thue = cursor.fetchone()
    if not nguoi_thue:
        return redirect(url_for('admin.manage_nguoi_thue'))

    cursor.execute("""
        SELECT dp.*, p.tieu_de as phong_tieu_de, p.dia_chi as phong_dia_chi, p.gia as phong_gia, p.id as phong_id,
               chu_nha.ho_ten as chu_nha_ten, chu_nha.email as chu_nha_email
        FROM dat_phong dp
        JOIN phong p ON dp.phong_id = p.id
        LEFT JOIN nguoi_dung chu_nha ON p.nguoi_dung_id = chu_nha.id
        WHERE dp.nguoi_dung_id = %s
        ORDER BY dp.id DESC
    """, (nguoi_thue_id,))
    bookings = cursor.fetchall()

    stats = {'total_bookings': len(bookings), 'cho_xac_nhan': 0, 'da_xac_nhan': 0, 'da_huy': 0}
    for b in bookings:
        stats[b['trang_thai']] = stats.get(b['trang_thai'], 0) + 1

    return render_template('admin/view_nguoi_thue.html', nguoi_thue=nguoi_thue, bookings=bookings, stats=stats)


@admin_bp.route('/manage-bookings')
@require_admin
def manage_bookings():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(f"""
        SELECT dp.*, p.tieu_de as phong_tieu_de, p.dia_chi as phong_dia_chi,
               nd.ho_ten as khach_hang_ten, nd.email as khach_hang_email, nd.so_dien_thoai as khach_hang_sdt
        FROM dat_phong dp
        JOIN phong p ON dp.phong_id = p.id
        JOIN nguoi_dung nd ON dp.nguoi_dung_id = nd.id
        ORDER BY dp.ngay_tao DESC
    """)
    bookings = cursor.fetchall()
    return render_template('admin/manage_bookings.html', bookings=bookings)

from tuyen import chu_nha_phong  # đăng ký route chủ nhà
