"""Tuyến Admin và Chủ nhà"""
import os
from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from werkzeug.utils import secure_filename
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor
from cau_hinh.cai_dat import UPLOAD_DIR, MAX_IMAGE_SIZE, ALLOWED_IMAGE_TYPES
from phu_tro.decorators_xac_thuc import require_login, require_admin, require_chu_nha, get_current_user, get_current_admin
from phu_tro.tro_giup import get_anh_chinh_subquery, get_anh_dau_tien_subquery, get_trang_thai_phong_text, get_trang_thai_booking_info

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
    stats = {}
    for key, sql in [
        ('total_rooms', "SELECT COUNT(*) as total FROM phong"),
        ('available_rooms', "SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'con_trong'"),
        ('rented_rooms', "SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'da_thue'"),
        ('total_bookings', "SELECT COUNT(*) as total FROM dat_phong"),
        ('pending_bookings', "SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'cho_xac_nhan'"),
        ('confirmed_bookings', "SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'da_xac_nhan'"),
        ('total_nguoi_thue', "SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'nguoi_thue'"),
        ('total_chu_nha', "SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'chu_nha'"),
        ('total_admin', "SELECT COUNT(*) as total FROM admin WHERE trang_thai = 'active'"),
    ]:
        cursor.execute(sql)
        stats[key] = cursor.fetchone()['total'] or 0
    admin = get_current_admin(conn)
    conn.close()
    return render_template('admin/dashboard.html', stats=stats, admin=admin)


@admin_bp.route('/manage-phong')
@require_admin
def manage_phong():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(f"""
        SELECT p.*, nd.ho_ten as chu_nha_ten, nd.email as chu_nha_email,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        ORDER BY p.id DESC
    """)
    phong_list = cursor.fetchall()

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        try:
            cursor.execute("DELETE FROM phong_tien_ich WHERE phong_id = %s", (delete_id,))
            cursor.execute("DELETE FROM anh_phong WHERE phong_id = %s", (delete_id,))
            cursor.execute("DELETE FROM dat_phong WHERE phong_id = %s", (delete_id,))
            cursor.execute("DELETE FROM yeu_thich WHERE phong_id = %s", (delete_id,))
            cursor.execute("DELETE FROM phong WHERE id = %s", (delete_id,))
            conn.commit()
            flash('Đã xóa phòng thành công!', 'success')
        except Exception as e:
            flash(f'Lỗi khi xóa: {e}', 'error')
        conn.close()
        return redirect(url_for('admin.manage_phong'))

    conn.close()
    return render_template('admin/manage_phong.html', phong_list=phong_list)


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
        conn.close()
        return redirect(url_for('admin.manage_chu_nha'))

    conn.close()
    return render_template('admin/manage_chu_nha.html', chu_nha_list=chu_nha_list)


@admin_bp.route('/view-chu-nha/<int:chu_nha_id>')
@require_admin
def view_chu_nha(chu_nha_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute("SELECT * FROM nguoi_dung WHERE id = %s AND vai_tro = 'chu_nha'", (chu_nha_id,))
    chu_nha = cursor.fetchone()
    if not chu_nha:
        conn.close()
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

    conn.close()
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
        conn.close()
        return redirect(url_for('admin.manage_nguoi_thue'))

    conn.close()
    return render_template('admin/manage_nguoi_thue.html', nguoi_thue_list=nguoi_thue_list)


@admin_bp.route('/view-nguoi-thue/<int:nguoi_thue_id>')
@require_admin
def view_nguoi_thue(nguoi_thue_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute("SELECT * FROM nguoi_dung WHERE id = %s AND vai_tro = 'nguoi_thue'", (nguoi_thue_id,))
    nguoi_thue = cursor.fetchone()
    if not nguoi_thue:
        conn.close()
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

    conn.close()
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
    conn.close()
    return render_template('admin/manage_bookings.html', bookings=bookings)


@admin_bp.route('/chu-nha')
@require_login
@require_chu_nha
def chu_nha_index():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    stats = {}
    for key, sql, params in [
        ('total_rooms', "SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s", (user['id'],)),
        ('available_rooms', "SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s AND trang_thai = 'con_trong'", (user['id'],)),
        ('rented_rooms', "SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s AND trang_thai = 'da_thue'", (user['id'],)),
    ]:
        cursor.execute(sql, params)
        stats[key] = cursor.fetchone()['total'] or 0
    conn.close()
    return render_template('admin/chu_nha_index.html', stats=stats, user=user)


@admin_bp.route('/manage-rooms', methods=['GET', 'POST'])
@require_login
@require_chu_nha
def manage_rooms():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    error = ''
    success_message = ''

    if request.method == 'POST' and request.form.get('tieu_de'):
        tieu_de = (request.form.get('tieu_de') or '').strip()
        mo_ta = (request.form.get('mo_ta') or '').strip()
        dia_chi = (request.form.get('dia_chi') or '').strip()
        gia = request.form.get('gia', '0')
        quan_huyen = (request.form.get('quan_huyen') or '').strip()
        dien_tich = request.form.get('dien_tich') or None
        trang_thai = request.form.get('trang_thai', 'con_trong')
        tien_ich_ids = request.form.getlist('tien_ich')

        if not tieu_de or not dia_chi or not gia:
            error = 'Vui lòng điền đầy đủ thông tin bắt buộc (tiêu đề, địa chỉ, giá)'
        elif not str(gia).replace('.', '').isdigit() or float(gia) <= 0:
            error = 'Giá phòng phải là số dương'
        else:
            try:
                cursor.execute(
                    "INSERT INTO phong (tieu_de, mo_ta, dia_chi, quan_huyen, gia, dien_tich, trang_thai, nguoi_dung_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s) RETURNING id",
                    (tieu_de, mo_ta or None, dia_chi, quan_huyen or None, float(gia), float(dien_tich) if dien_tich else None, trang_thai, user['id'])
                )
                phong_id = cursor.fetchone()['id']

                import time
                if 'images_upload' in request.files:
                    files = request.files.getlist('images_upload')
                    for i, f in enumerate(files):
                        if f and f.filename:
                            data = f.read()
                            f.seek(0)
                            ctype = f.content_type or ''
                            if ctype in ALLOWED_IMAGE_TYPES and len(data) <= MAX_IMAGE_SIZE:
                                ext = secure_filename(f.filename).rsplit('.', 1)[-1] if '.' in f.filename else 'jpg'
                                filename = f'room_{phong_id}_{int(time.time())}_{i}.{ext}'
                                filepath = os.path.join(UPLOAD_DIR, filename)
                                os.makedirs(UPLOAD_DIR, exist_ok=True)
                                with open(filepath, 'wb') as out:
                                    out.write(data)
                                cursor.execute("INSERT INTO anh_phong (phong_id, duong_dan_anh, la_anh_chinh) VALUES (%s, %s, %s)",
                                             (phong_id, f'uploads/{filename}', 1 if i == 0 else 0))
                images_input = (request.form.get('images') or '').strip()
                if images_input:
                    for i, path in enumerate([x.strip() for x in images_input.split(',') if x.strip()]):
                        cursor.execute("INSERT INTO anh_phong (phong_id, duong_dan_anh, la_anh_chinh) VALUES (%s, %s, %s)",
                                     (phong_id, path, 1 if i == 0 else 0))

                for tid in tien_ich_ids:
                    if tid.isdigit():
                        cursor.execute("INSERT INTO phong_tien_ich (phong_id, tien_ich_id) VALUES (%s, %s)", (phong_id, int(tid)))
                conn.commit()
                return redirect(url_for('admin.manage_rooms', success='added'))
            except Exception as e:
                error = f'Có lỗi xảy ra: {e}'

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        cursor.execute("DELETE FROM phong WHERE id = %s AND nguoi_dung_id = %s", (delete_id, user['id']))
        conn.commit()
        conn.close()
        return redirect(url_for('admin.manage_rooms', success='deleted'))

    cursor.execute(f"""
        SELECT p.*, nd.ho_ten as chu_nha_ten
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        WHERE p.nguoi_dung_id = %s
        ORDER BY p.id DESC
    """, (user['id'],))
    rooms = cursor.fetchall()
    cursor.execute("SELECT * FROM tien_ich ORDER BY ten")
    tien_ich_list = cursor.fetchall()

    if request.args.get('success') == 'added':
        success_message = 'Đã thêm phòng thành công!'
    elif request.args.get('success') == 'deleted':
        success_message = 'Đã xóa phòng thành công!'
    elif request.args.get('success') == 'updated':
        success_message = 'Đã cập nhật phòng thành công!'

    conn.close()
    return render_template('admin/manage_rooms.html', rooms=rooms, tien_ich_list=tien_ich_list, error=error, success_message=success_message)


@admin_bp.route('/bookings')
@require_login
@require_chu_nha
def chu_nha_bookings():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    if request.method == 'POST':
        action = request.form.get('action')
        booking_id = request.form.get('booking_id', type=int)
        if booking_id and action:
            cursor.execute("SELECT dp.* FROM dat_phong dp INNER JOIN phong p ON dp.phong_id = p.id WHERE dp.id = %s AND p.nguoi_dung_id = %s", (booking_id, user['id']))
            booking = cursor.fetchone()
            if booking:
                if action == 'approve':
                    cursor.execute("UPDATE dat_phong SET trang_thai = 'da_xac_nhan' WHERE id = %s", (booking_id,))
                    cursor.execute("UPDATE phong SET trang_thai = 'da_thue' WHERE id = %s", (booking['phong_id'],))
                elif action == 'reject':
                    cursor.execute("UPDATE dat_phong SET trang_thai = 'da_huy' WHERE id = %s", (booking_id,))
                conn.commit()
        conn.close()
        return redirect(url_for('admin.chu_nha_bookings'))

    cursor.execute("""
        SELECT dp.*, p.tieu_de as phong_tieu_de, p.dia_chi as phong_dia_chi, p.gia as phong_gia, p.id as phong_id,
               nd.ho_ten as khach_hang_ten, nd.so_dien_thoai as khach_hang_sdt, nd.email as khach_hang_email
        FROM dat_phong dp
        INNER JOIN phong p ON dp.phong_id = p.id
        INNER JOIN nguoi_dung nd ON dp.nguoi_dung_id = nd.id
        WHERE p.nguoi_dung_id = %s AND dp.trang_thai != 'da_huy'
        ORDER BY dp.nguoi_dung_id, dp.ngay_tao DESC
    """, (user['id'],))
    all_bookings = cursor.fetchall()

    grouped = {}
    for b in all_bookings:
        cid = b['nguoi_dung_id']
        if cid not in grouped:
            grouped[cid] = {'customer_info': {'id': cid, 'ten': b['khach_hang_ten'], 'email': b['khach_hang_email'], 'sdt': b['khach_hang_sdt']}, 'bookings': []}
        grouped[cid]['bookings'].append(b)

    stats = {'cho_xac_nhan': sum(1 for b in all_bookings if b['trang_thai'] == 'cho_xac_nhan' ),
             'da_xac_nhan': sum(1 for b in all_bookings if b['trang_thai'] == 'da_xac_nhan'),
             'total': len(all_bookings)}

    conn.close()
    return render_template('admin/chu_nha_bookings.html', grouped_bookings=grouped, stats=stats)


@admin_bp.route('/edit-room/<int:room_id>', methods=['GET', 'POST'])
@require_login
@require_chu_nha
def edit_room(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cursor.execute("SELECT * FROM phong WHERE id = %s AND nguoi_dung_id = %s", (room_id, user['id']))
    phong = cursor.fetchone()
    if not phong:
        conn.close()
        return redirect(url_for('admin.manage_rooms'))

    cursor.execute("SELECT * FROM anh_phong WHERE phong_id = %s ORDER BY la_anh_chinh DESC", (room_id,))
    current_images = cursor.fetchall()
    cursor.execute("SELECT tien_ich_id FROM phong_tien_ich WHERE phong_id = %s", (room_id,))
    current_tien_ich = [r['tien_ich_id'] for r in cursor.fetchall()]
    cursor.execute("SELECT * FROM tien_ich ORDER BY ten")
    all_tien_ich = cursor.fetchall()

    error = ''
    if request.method == 'POST':
        tieu_de = (request.form.get('tieu_de') or '').strip()
        mo_ta = (request.form.get('mo_ta') or '').strip()
        dia_chi = (request.form.get('dia_chi') or '').strip()
        gia = request.form.get('gia', '0')
        quan_huyen = (request.form.get('quan_huyen') or '').strip()
        dien_tich = request.form.get('dien_tich') or None
        trang_thai = request.form.get('trang_thai', 'con_trong')
        tien_ich_ids = request.form.getlist('tien_ich')

        if not tieu_de or not dia_chi or not gia:
            error = 'Vui lòng điền đầy đủ thông tin bắt buộc'
        else:
            try:
                cursor.execute("UPDATE phong SET tieu_de=%s, mo_ta=%s, dia_chi=%s, quan_huyen=%s, gia=%s, dien_tich=%s, trang_thai=%s WHERE id=%s AND nguoi_dung_id=%s",
                             (tieu_de, mo_ta or None, dia_chi, quan_huyen or None, float(gia), float(dien_tich) if dien_tich else None, trang_thai, room_id, user['id']))
                cursor.execute("DELETE FROM phong_tien_ich WHERE phong_id = %s", (room_id,))
                for tid in tien_ich_ids:
                    if str(tid).isdigit():
                        cursor.execute("INSERT INTO phong_tien_ich (phong_id, tien_ich_id) VALUES (%s, %s)", (room_id, int(tid)))
                conn.commit()
                conn.close()
                return redirect(url_for('admin.manage_rooms', success='updated'))
            except Exception as e:
                error = str(e)

    conn.close()
    return render_template('admin/edit_room.html', phong=phong, current_images=current_images, current_tien_ich=current_tien_ich, all_tien_ich=all_tien_ich, error=error)
