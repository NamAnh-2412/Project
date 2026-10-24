"""Phòng và đơn của chủ nhà. Route gắn vào blueprint admin."""
from flask import current_app, flash, redirect, render_template, request, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.phong_dang_service import cap_nhat_tin_phong, danh_sach_tien_ich, tao_tin_phong
from dich_vu.thong_ke_service import thong_ke_chu_nha
from dich_vu.upload_service import xoa_phong_va_tep
from phu_tro.bao_mat import lam_sach, so_thuc
from phu_tro.decorators_xac_thuc import get_current_user, require_chu_nha, require_login
from phu_tro.tro_giup import LOAI_PHONG
from tuyen.quan_tri import admin_bp


@admin_bp.route('/chu-nha')
@require_login
@require_chu_nha
def chu_nha_index():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    stats = thong_ke_chu_nha(cursor, user['id'])
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
        tieu_de = lam_sach(request.form.get('tieu_de'), 255)
        mo_ta = lam_sach(request.form.get('mo_ta'), 5000)
        dia_chi = lam_sach(request.form.get('dia_chi'), 500)
        gia = so_thuc(request.form.get('gia'), 0)
        quan_huyen = lam_sach(request.form.get('quan_huyen'), 100)
        dien_tich = so_thuc(request.form.get('dien_tich'), None)
        trang_thai = request.form.get('trang_thai', 'con_trong')
        if trang_thai not in ('con_trong', 'da_thue'):
            trang_thai = 'con_trong'
        loai_phong = request.form.get('loai_phong', 'phong_tro')
        if loai_phong not in LOAI_PHONG:
            loai_phong = 'phong_tro'
        tien_ich_ids = request.form.getlist('tien_ich')
        vi_do = so_thuc(request.form.get('vi_do'), None)
        kinh_do = so_thuc(request.form.get('kinh_do'), None)

        if not tieu_de or not dia_chi or not gia:
            error = 'Vui lòng điền đầy đủ thông tin bắt buộc (tiêu đề, địa chỉ, giá)'
        elif gia <= 0:
            error = 'Giá phòng phải là số dương'
        else:
            try:
                tao_tin_phong(
                    cursor,
                    user['id'],
                    {
                        'tieu_de': tieu_de,
                        'mo_ta': mo_ta,
                        'dia_chi': dia_chi,
                        'quan_huyen': quan_huyen,
                        'gia': gia,
                        'dien_tich': dien_tich,
                        'trang_thai': trang_thai,
                        'loai_phong': loai_phong,
                        'tien_dien': so_thuc(request.form.get('tien_dien'), 0) or 0,
                        'tien_nuoc': so_thuc(request.form.get('tien_nuoc'), 0) or 0,
                        'tien_dich_vu': so_thuc(request.form.get('tien_dich_vu'), 0) or 0,
                        'tien_coc': so_thuc(request.form.get('tien_coc'), 0) or 0,
                        'vi_do': vi_do,
                        'kinh_do': kinh_do,
                        'tien_ich_ids': tien_ich_ids,
                    },
                    request.files.getlist('images_upload'),
                )
                conn.commit()
                return redirect(url_for('admin.manage_rooms', success='added'))
            except Exception:
                current_app.logger.exception('Không lưu được phòng')
                error = 'Có lỗi xảy ra khi lưu phòng.'

    delete_id = request.args.get('delete', 0, type=int)
    if delete_id:
        cursor.execute('SELECT id FROM phong WHERE id = %s AND nguoi_dung_id = %s', (delete_id, user['id']))
        if cursor.fetchone():
            xoa_phong_va_tep(cursor, delete_id)
            conn.commit()
        return redirect(url_for('admin.manage_rooms', success='deleted'))

    cursor.execute(f"""
        SELECT p.*, nd.ho_ten as chu_nha_ten
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        WHERE p.nguoi_dung_id = %s
        ORDER BY p.id DESC
    """, (user['id'],))
    rooms = cursor.fetchall()
    tien_ich_list = danh_sach_tien_ich(cursor)

    if request.args.get('success') == 'added':
        success_message = 'Đã gửi tin. Phòng hiện trên trang chủ sau khi admin duyệt.'
    elif request.args.get('success') == 'deleted':
        success_message = 'Đã xóa phòng thành công!'
    elif request.args.get('success') == 'updated':
        success_message = 'Đã cập nhật phòng thành công!'

    return render_template(
        'admin/manage_rooms.html',
        rooms=rooms,
        tien_ich_list=tien_ich_list,
        error=error,
        success_message=success_message,
        loai_phong_ds=LOAI_PHONG,
    )


@admin_bp.route('/bookings', methods=['GET', 'POST'])
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
                from dich_vu.dat_phong_service import LoiDon, huy_don, xac_nhan_don
                try:
                    if action == 'approve':
                        xac_nhan_don(conn, booking_id, user['id'])
                    elif action == 'reject':
                        huy_don(conn, booking_id, user['id'], la_chu_nha=True, ly_do='chu_nha_tu_choi')
                except LoiDon as exc:
                    flash(exc.message, 'error')
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
        return redirect(url_for('admin.manage_rooms'))

    cursor.execute("SELECT * FROM anh_phong WHERE phong_id = %s ORDER BY la_anh_chinh DESC", (room_id,))
    current_images = cursor.fetchall()
    cursor.execute("SELECT tien_ich_id FROM phong_tien_ich WHERE phong_id = %s", (room_id,))
    current_tien_ich = [r['tien_ich_id'] for r in cursor.fetchall()]
    all_tien_ich = danh_sach_tien_ich(cursor)

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
        loai_phong = request.form.get('loai_phong', 'phong_tro')
        if loai_phong not in LOAI_PHONG:
            loai_phong = 'phong_tro'
        if trang_thai not in ('con_trong', 'da_thue'):
            trang_thai = 'con_trong'

        if not tieu_de or not dia_chi or not gia:
            error = 'Vui lòng điền đầy đủ thông tin bắt buộc'
        else:
            try:
                cap_nhat_tin_phong(
                    cursor,
                    room_id,
                    user['id'],
                    {
                        'tieu_de': tieu_de,
                        'mo_ta': mo_ta,
                        'dia_chi': dia_chi,
                        'quan_huyen': quan_huyen,
                        'gia': float(gia),
                        'dien_tich': float(dien_tich) if dien_tich else None,
                        'trang_thai': trang_thai,
                        'loai_phong': loai_phong,
                        'tien_dien': so_thuc(request.form.get('tien_dien'), 0) or 0,
                        'tien_nuoc': so_thuc(request.form.get('tien_nuoc'), 0) or 0,
                        'tien_dich_vu': so_thuc(request.form.get('tien_dich_vu'), 0) or 0,
                        'tien_coc': so_thuc(request.form.get('tien_coc'), 0) or 0,
                        'vi_do': so_thuc(request.form.get('vi_do'), None),
                        'kinh_do': so_thuc(request.form.get('kinh_do'), None),
                        'an_tin': request.form.get('an_tin') == '1',
                        'tien_ich_ids': tien_ich_ids,
                    },
                    request.files.getlist('images_upload'),
                    request.form.getlist('xoa_anh'),
                    request.form.get('anh_chinh', type=int),
                )
                conn.commit()
                return redirect(url_for('admin.manage_rooms', success='updated'))
            except Exception:
                current_app.logger.exception('Không cập nhật được phòng')
                error = 'Có lỗi xảy ra khi lưu phòng.'

    return render_template(
        'admin/edit_room.html',
        phong=phong,
        current_images=current_images,
        current_tien_ich=current_tien_ich,
        all_tien_ich=all_tien_ich,
        error=error,
        loai_phong_ds=LOAI_PHONG,
    )
