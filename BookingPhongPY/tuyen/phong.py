"""Tuyến phòng: chi tiết, đặt phòng, yêu thích."""
from flask import Blueprint, flash, redirect, render_template, request, session, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.dat_phong_service import LoiDon, huy_don, tao_don
from dich_vu.email_service import gui_email
from phu_tro.bao_mat import lam_sach, so_nguyen
from phu_tro.decorators_xac_thuc import get_current_user, require_login, require_nguoi_thue
from phu_tro.tro_giup import anh_va_lich_hen, get_anh_chinh_subquery, get_anh_dau_tien_subquery

rooms_bp = Blueprint('rooms', __name__)


@rooms_bp.route('/room/<int:room_id>')
def room_detail(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)

    cursor.execute(
        """SELECT p.*, nd.ho_ten as chu_nha_ten, nd.email as chu_nha_email,
                  nd.so_dien_thoai as chu_nha_sdt, nd.is_verified as chu_nha_uy_tin
           FROM phong p
           LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
           WHERE p.id = %s""",
        (room_id,),
    )
    phong = cursor.fetchone()
    if not phong:
        return redirect(url_for('main.index'))

    user = get_current_user(conn)
    is_admin = 'admin_id' in session
    is_owner = user and phong['nguoi_dung_id'] == user['id']
    duyet = phong.get('trang_thai_duyet') or 'da_duyet'
    if (duyet != 'da_duyet' or phong.get('an_tin')) and not is_owner and not is_admin:
        flash('Tin này chưa được hiển thị công khai.', 'error')
        return redirect(url_for('main.index'))

    if not is_owner and not is_admin:
        cursor.execute(
            'UPDATE phong SET so_luot_xem = COALESCE(so_luot_xem, 0) + 1 WHERE id = %s',
            (room_id,),
        )
        phong['so_luot_xem'] = (phong.get('so_luot_xem') or 0) + 1

    cursor.execute(
        'SELECT * FROM anh_phong WHERE phong_id = %s ORDER BY la_anh_chinh DESC, id ASC',
        (room_id,),
    )
    images = cursor.fetchall()

    cursor.execute(
        """SELECT ti.* FROM tien_ich ti
           INNER JOIN phong_tien_ich pti ON ti.id = pti.tien_ich_id
           WHERE pti.phong_id = %s""",
        (room_id,),
    )
    tien_ich_list = cursor.fetchall()

    is_chu_nha = user and user.get('vai_tro') == 'chu_nha'
    has_confirmed_booking = False
    if user and not is_owner and not is_admin:
        cursor.execute(
            """SELECT COUNT(*) AS count FROM dat_phong
               WHERE phong_id = %s AND nguoi_dung_id = %s AND trang_thai = 'da_xac_nhan'""",
            (room_id, user['id']),
        )
        has_confirmed_booking = cursor.fetchone()['count'] > 0

    is_favorite = False
    if user and not is_owner and not is_admin:
        cursor.execute(
            'SELECT COUNT(*) AS count FROM yeu_thich WHERE nguoi_dung_id = %s AND phong_id = %s',
            (user['id'], room_id),
        )
        is_favorite = cursor.fetchone()['count'] > 0

    cursor.execute(
        """SELECT dg.*, nd.ho_ten
           FROM danh_gia dg JOIN nguoi_dung nd ON nd.id = dg.nguoi_dung_id
           WHERE dg.phong_id = %s ORDER BY dg.ngay_tao DESC""",
        (room_id,),
    )
    danh_gia_list = cursor.fetchall()
    cursor.execute(
        'SELECT ROUND(AVG(so_sao)::numeric, 1) AS diem, COUNT(*) AS so FROM danh_gia WHERE phong_id = %s',
        (room_id,),
    )
    thong_ke_dg = cursor.fetchone()

    cursor.execute(
        """SELECT hd.*, nd.ho_ten FROM hoi_dap hd
           JOIN nguoi_dung nd ON nd.id = hd.nguoi_dung_id
           WHERE hd.phong_id = %s ORDER BY hd.ngay_tao DESC""",
        (room_id,),
    )
    hoi_dap_list = cursor.fetchall()
    cursor.execute(
        """SELECT id, nguoi_dung_id, het_han_giu FROM dat_phong
           WHERE phong_id = %s AND trang_thai = 'cho_xac_nhan'
             AND (het_han_giu IS NULL OR het_han_giu > NOW())
           ORDER BY id DESC LIMIT 1""",
        (room_id,),
    )
    don_giu = cursor.fetchone()

    return render_template(
        'rooms/room_detail.html',
        phong=phong,
        images=images,
        tien_ich_list=tien_ich_list,
        is_owner=is_owner,
        la_chu_nha=is_chu_nha,
        la_admin=is_admin,
        has_confirmed_booking=has_confirmed_booking,
        is_favorite=is_favorite,
        error_message=request.args.get('error', ''),
        danh_gia_list=danh_gia_list,
        diem_tb=thong_ke_dg['diem'],
        so_danh_gia=thong_ke_dg['so'],
        hoi_dap_list=hoi_dap_list,
        don_giu=don_giu,
    )


@rooms_bp.route('/booking', methods=['GET', 'POST'])
@require_login
@require_nguoi_thue
def booking():
    phong_id = request.args.get('phong_id', 0, type=int) or request.form.get('phong_id', 0, type=int)
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cursor.execute('SELECT * FROM phong WHERE id = %s', (phong_id,))
    phong = cursor.fetchone()
    if not phong or phong['trang_thai'] != 'con_trong' or (phong.get('trang_thai_duyet') or 'da_duyet') != 'da_duyet':
        return redirect(url_for('main.index'))
    if phong['nguoi_dung_id'] == user['id']:
        return redirect(url_for('rooms.room_detail', room_id=phong_id, error='own_room'))

    cursor.execute(
        """SELECT id, nguoi_dung_id, het_han_giu FROM dat_phong
           WHERE phong_id = %s AND trang_thai = 'cho_xac_nhan'
             AND (het_han_giu IS NULL OR het_han_giu > NOW())
           ORDER BY id DESC LIMIT 1""",
        (phong_id,),
    )
    don_giu = cursor.fetchone()
    error = ''
    if request.method == 'POST' and not (don_giu and don_giu['nguoi_dung_id'] != user['id']):
        tin_nhan = lam_sach(request.form.get('tin_nhan'), 1000)
        sdt = lam_sach(request.form.get('so_dien_thoai_lien_he'), 20)
        ngay_vao = (request.form.get('ngay_chuyen_vao') or '').strip() or None
        thoi_han = so_nguyen(request.form.get('thoi_han_thue_thang'), 1)
        so_nguoi = so_nguyen(request.form.get('so_nguoi_o'), 1)
        ma_giam = lam_sach(request.form.get('ma_giam'), 40)
        if not sdt:
            error = 'Vui lòng nhập số điện thoại liên hệ'
        elif not ngay_vao:
            error = 'Chọn ngày chuyển vào'
        else:
            try:
                don = tao_don(
                    conn, phong_id, user['id'], tin_nhan, sdt, ngay_vao, thoi_han, so_nguoi, ma_giam,
                )
                cursor.execute('SELECT email FROM nguoi_dung WHERE id = %s', (phong['nguoi_dung_id'],))
                chu = cursor.fetchone()
                if chu and chu.get('email'):
                    gui_email(chu['email'], 'Đơn đặt phòng mới', f"<p>Có đơn mới cho phòng {phong['tieu_de']}.</p>")
                return redirect(url_for('dat_cho.thanh_toan', dat_id=don['id']))
            except LoiDon as exc:
                if exc.don_cu_id:
                    flash(exc.message, 'error')
                    return redirect(url_for('dat_cho.thanh_toan', dat_id=exc.don_cu_id))
                error = exc.message

    return render_template(
        'rooms/booking.html', phong=phong, success='', error=error, user=user, don_giu=don_giu,
    )


@rooms_bp.route('/my-bookings', methods=['GET', 'POST'])
@require_login
@require_nguoi_thue
def my_bookings():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)

    cancel_id = 0
    ly_do = ''
    if request.method == 'POST':
        cancel_id = request.form.get('cancel_id', 0, type=int)
        ly_do = lam_sach(request.form.get('ly_do_huy'), 500)
    else:
        cancel_id = request.args.get('cancel', 0, type=int)

    if cancel_id:
        cursor.execute(
            """SELECT dp.*, p.nguoi_dung_id AS chu_id, p.tieu_de
               FROM dat_phong dp JOIN phong p ON p.id = dp.phong_id
               WHERE dp.id = %s AND dp.nguoi_dung_id = %s""",
            (cancel_id, user['id']),
        )
        booking = cursor.fetchone()
        if booking and booking['trang_thai'] in ('cho_xac_nhan', 'da_xac_nhan'):
            try:
                huy_don(conn, cancel_id, user['id'], la_chu_nha=False, ly_do=ly_do)
            except LoiDon:
                return redirect(url_for('rooms.my_bookings', error='invalid_booking'))
            return redirect(url_for('rooms.my_bookings', success='cancelled'))
        return redirect(url_for('rooms.my_bookings', error='invalid_booking'))

    cursor.execute(
        f"""SELECT dp.*, p.tieu_de as phong_tieu_de, p.dia_chi as phong_dia_chi,
               COALESCE(dp.gia_thue_chot, p.gia) AS gia,
               p.nguoi_dung_id as chu_nha_id, nd.ho_ten as chu_nha_ten,
               nd.so_dien_thoai as chu_nha_sdt, nd.email as chu_nha_email,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
            FROM dat_phong dp
            JOIN phong p ON dp.phong_id = p.id
            LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
            WHERE dp.nguoi_dung_id = %s
            ORDER BY dp.id DESC, dp.ngay_tao DESC""",
        (user['id'],),
    )
    bookings = cursor.fetchall()
    return render_template('rooms/my_bookings.html', bookings=bookings)


@rooms_bp.route('/favorites')
@require_login
@require_nguoi_thue
def favorites():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    cursor.execute(
        f"""SELECT p.*, {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()},
               yt.ngay_tao as ngay_yeu_thich
            FROM yeu_thich yt
            INNER JOIN phong p ON yt.phong_id = p.id
            WHERE yt.nguoi_dung_id = %s
            ORDER BY yt.ngay_tao DESC, p.id DESC""",
        (user['id'],),
    )
    favorite_rooms = cursor.fetchall()
    anh_theo_phong, phong_co_hen = anh_va_lich_hen(cursor, [room['id'] for room in favorite_rooms])
    return render_template(
        'rooms/favorites.html',
        favorite_rooms=favorite_rooms,
        anh_theo_phong=anh_theo_phong,
        phong_co_hen=phong_co_hen,
    )
