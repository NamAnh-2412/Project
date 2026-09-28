"""Tuyến phòng: chi tiết phòng, đặt phòng, yêu thích"""
from flask import Blueprint, render_template, request, redirect, url_for, flash
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor
from phu_tro.decorators_xac_thuc import require_login, require_nguoi_thue, get_current_user
from phu_tro.tro_giup import get_trang_thai_phong_text, get_trang_thai_booking_info, get_anh_chinh_subquery, get_anh_dau_tien_subquery

rooms_bp = Blueprint('rooms', __name__)


@rooms_bp.route('/room/<int:room_id>')
def room_detail(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute("""
        SELECT p.*, nd.ho_ten as chu_nha_ten, nd.email as chu_nha_email, nd.so_dien_thoai as chu_nha_sdt
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        WHERE p.id = %s
    """, (room_id,))
    phong = cursor.fetchone()

    if not phong:
        conn.close()
        return redirect(url_for('main.index'))

    cursor.execute("SELECT * FROM anh_phong WHERE phong_id = %s ORDER BY la_anh_chinh DESC, id ASC", (room_id,))
    images = cursor.fetchall()

    cursor.execute("""
        SELECT ti.* FROM tien_ich ti
        INNER JOIN phong_tien_ich pti ON ti.id = pti.tien_ich_id
        WHERE pti.phong_id = %s
    """, (room_id,))
    tien_ich_list = cursor.fetchall()

    user = get_current_user(conn)
    is_chu_nha = user and user.get('vai_tro') == 'chu_nha'
    is_admin = 'admin_id' in request.session
    is_owner = user and phong['nguoi_dung_id'] == user['id']

    has_confirmed_booking = False
    if user and not is_owner and not is_admin:
        cursor.execute(
            "SELECT COUNT(*) as count FROM dat_phong WHERE phong_id = %s AND nguoi_dung_id = %s AND trang_thai = 'da_xac_nhan'",
            (room_id, user['id'])
        )
        has_confirmed_booking = cursor.fetchone()['count'] > 0

    is_favorite = False
    if user and not is_owner and not is_admin:
        cursor.execute("SELECT COUNT(*) as count FROM yeu_thich WHERE nguoi_dung_id = %s AND phong_id = %s", (user['id'], room_id))
        is_favorite = cursor.fetchone()['count'] > 0

    error_message = request.args.get('error', '')
    conn.close()

    return render_template('rooms/room_detail.html',
        phong=phong, images=images, tien_ich_list=tien_ich_list,
        is_owner=is_owner, is_chu_nha=is_chu_nha, is_admin=is_admin,
        has_confirmed_booking=has_confirmed_booking, is_favorite=is_favorite,
        error_message=error_message,
    )


@rooms_bp.route('/booking', methods=['GET', 'POST'])
@require_login
@require_nguoi_thue
def booking():
    phong_id = request.args.get('phong_id', 0, type=int)
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cursor.execute("SELECT * FROM phong WHERE id = %s", (phong_id,))
    phong = cursor.fetchone()

    if not phong or phong['trang_thai'] != 'con_trong':
        conn.close()
        return redirect(url_for('main.index'))

    if phong['nguoi_dung_id'] == user['id']:
        conn.close()
        return redirect(url_for('rooms.room_detail', room_id=phong_id, error='own_room'))

    error = ''
    success = ''
    if request.method == 'POST':
        tin_nhan = (request.form.get('tin_nhan') or '').strip()
        so_dien_thoai_lien_he = (request.form.get('so_dien_thoai_lien_he') or '').strip()
        try:
            cursor.execute(
                "INSERT INTO dat_phong (phong_id, nguoi_dung_id, tin_nhan, so_dien_thoai_lien_he, trang_thai) VALUES (%s, %s, %s, %s, 'cho_xac_nhan')",
                (phong_id, user['id'], tin_nhan or None, so_dien_thoai_lien_he or None)
            )
            conn.commit()
            success = 'Đặt phòng thành công! Vui lòng chờ chủ nhà xác nhận.'
        except Exception as e:
            error = f'Có lỗi xảy ra: {e}'

    conn.close()

    if success:
        return render_template('rooms/booking.html', phong=phong, success=success, error=error)
    return render_template('rooms/booking.html', phong=phong, success=success, error=error, user=user)


@rooms_bp.route('/my-bookings')
@require_login
@require_nguoi_thue
def my_bookings():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cancel_id = request.args.get('cancel', 0, type=int)
    if cancel_id:
        cursor.execute("SELECT * FROM dat_phong WHERE id = %s AND nguoi_dung_id = %s", (cancel_id, user['id']))
        booking = cursor.fetchone()
        if booking and booking['trang_thai'] in ('cho_xac_nhan', 'da_xac_nhan'):
            cursor.execute("UPDATE dat_phong SET trang_thai = 'da_huy' WHERE id = %s", (cancel_id,))
            if booking['trang_thai'] == 'da_xac_nhan':
                cursor.execute("UPDATE phong SET trang_thai = 'con_trong' WHERE id = %s", (booking['phong_id'],))
            conn.commit()
            conn.close()
            return redirect(url_for('rooms.my_bookings', success='cancelled'))
        conn.close()
        return redirect(url_for('rooms.my_bookings', error='invalid_booking'))

    cursor.execute(f"""
        SELECT dp.*, p.tieu_de as phong_tieu_de, p.dia_chi as phong_dia_chi, p.gia,
               p.nguoi_dung_id as chu_nha_id, nd.ho_ten as chu_nha_ten,
               nd.so_dien_thoai as chu_nha_sdt, nd.email as chu_nha_email,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
        FROM dat_phong dp
        JOIN phong p ON dp.phong_id = p.id
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        WHERE dp.nguoi_dung_id = %s
        ORDER BY dp.id DESC, dp.ngay_tao DESC
    """, (user['id'],))
    bookings = cursor.fetchall()
    conn.close()

    return render_template('rooms/my_bookings.html', bookings=bookings)


@rooms_bp.route('/favorites')
@require_login
@require_nguoi_thue
def favorites():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cursor.execute(f"""
        SELECT p.*, {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()},
               yt.ngay_tao as ngay_yeu_thich
        FROM yeu_thich yt
        INNER JOIN phong p ON yt.phong_id = p.id
        WHERE yt.nguoi_dung_id = %s
        ORDER BY yt.ngay_tao DESC, p.id DESC
    """, (user['id'],))
    favorite_rooms = cursor.fetchall()
    conn.close()

    return render_template('rooms/favorites.html', favorite_rooms=favorite_rooms)
