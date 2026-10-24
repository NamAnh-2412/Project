"""Đặt lịch xem phòng và đặt cọc VietQR."""
from flask import Blueprint, abort, flash, jsonify, redirect, render_template, request, url_for

from cau_hinh.cai_dat import WEBHOOK_SECRET
from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.dat_phong_service import LoiDon, ghi_nhan_coc
from dich_vu.email_service import gui_email
from dich_vu.payment_service import lay_chuyen_khoan, ma_coc, url_vietqr
from dich_vu.thong_bao_service import tao_thong_bao
from phu_tro.bao_mat import lam_sach
from phu_tro.decorators_xac_thuc import get_current_user, require_chu_nha, require_login, require_nguoi_thue
from phu_tro.mo_rong import csrf

dat_cho_bp = Blueprint('dat_cho', __name__)


@dat_cho_bp.route('/phong/<int:room_id>/lich-hen', methods=['POST'])
@require_login
@require_nguoi_thue
def dat_lich(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    cursor.execute('SELECT * FROM phong WHERE id = %s', (room_id,))
    phong = cursor.fetchone()
    thoi_gian = (request.form.get('thoi_gian_hen') or '').strip()
    if not thoi_gian:
        ngay = (request.form.get('ngay_hen') or '').strip()
        gio = (request.form.get('khung_gio') or '09:00').strip()
        if ngay and gio in ('09:00', '14:00', '18:00'):
            thoi_gian = f'{ngay} {gio}'
    ghi_chu = lam_sach(request.form.get('ghi_chu'), 500)
    if not phong or not thoi_gian:
        flash('Chọn ngày giờ xem phòng.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    if phong['nguoi_dung_id'] == user['id']:
        flash('Bạn không đặt lịch xem phòng của chính mình.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    cursor.execute(
        """INSERT INTO lich_hen_xem_phong (phong_id, nguoi_dung_id, thoi_gian_hen, ghi_chu)
           VALUES (%s, %s, %s, %s)""",
        (room_id, user['id'], thoi_gian.replace('T', ' '), ghi_chu or None),
    )
    tao_thong_bao(
        cursor,
        phong['nguoi_dung_id'],
        'Có lịch hẹn xem phòng',
        f"{user.get('ho_ten') or 'Khách'} muốn xem {phong['tieu_de']} lúc {thoi_gian}.",
        url_for('pms.lich_hen'),
    )
    cursor.execute('SELECT email FROM nguoi_dung WHERE id = %s', (phong['nguoi_dung_id'],))
    chu = cursor.fetchone()
    if chu:
        gui_email(chu['email'], 'Lịch hẹn xem phòng', f"<p>Có lịch hẹn mới cho {phong['tieu_de']}.</p>")
    conn.commit()
    flash('Đã gửi lịch hẹn. Chờ chủ nhà xác nhận.', 'success')
    return redirect(url_for('dat_cho.lich_cua_toi'))


@dat_cho_bp.route('/lich-hen-cua-toi')
@require_login
@require_nguoi_thue
def lich_cua_toi():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    cursor.execute(
        """SELECT lh.*, p.tieu_de, p.dia_chi
           FROM lich_hen_xem_phong lh JOIN phong p ON p.id = lh.phong_id
           WHERE lh.nguoi_dung_id = %s ORDER BY lh.thoi_gian_hen DESC""",
        (user['id'],),
    )
    lich = cursor.fetchall()
    return render_template('dat_cho/lich_cua_toi.html', lich=lich)


@dat_cho_bp.route('/dat-phong/<int:dat_id>/thanh-toan', methods=['GET', 'POST'])
@require_login
def thanh_toan(dat_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return redirect(url_for('main.index'))
    cursor.execute(
        """SELECT dp.*, p.tieu_de, p.dia_chi, p.gia, p.tien_coc, p.nguoi_dung_id AS chu_id
           FROM dat_phong dp JOIN phong p ON p.id = dp.phong_id
           WHERE dp.id = %s AND dp.nguoi_dung_id = %s""",
        (dat_id, user['id']),
    )
    don = cursor.fetchone()
    if not don:
        flash('Không tìm thấy đơn đặt phòng.', 'error')
        return redirect(url_for('rooms.my_bookings'))

    so_tien = float(don['tien_coc_phai_tra'] or don['tien_coc'] or 0)
    ma = ma_coc(dat_id)
    cursor.execute(
        """SELECT * FROM giao_dich_thanh_toan
           WHERE dat_phong_id = %s AND loai = 'coc' AND trang_thai <> 'da_huy'
           ORDER BY id DESC LIMIT 1""",
        (dat_id,),
    )
    giao_dich = cursor.fetchone()
    if so_tien > 0 and not giao_dich:
        cursor.execute(
            """INSERT INTO giao_dich_thanh_toan
               (ma_giao_dich, dat_phong_id, nguoi_dung_id, so_tien, phuong_thuc, trang_thai, thong_tin_nhan, loai)
               VALUES (%s, %s, %s, %s, 'vietqr', 'dang_cho', %s, 'coc') RETURNING *""",
            (ma, dat_id, user['id'], so_tien, ma),
        )
        giao_dich = cursor.fetchone()
        conn.commit()

    if request.method == 'POST' and request.form.get('action') == 'da_chuyen' and giao_dich:
        tao_thong_bao(
            cursor,
            don['chu_id'],
            'Khách báo đã chuyển cọc',
            f"Đơn #{dat_id} nội dung {ma}, số tiền {so_tien:,.0f} đ.",
            url_for('admin.chu_nha_bookings'),
        )
        conn.commit()
        flash('Đã báo cho chủ nhà. Đơn chuyển sang đã cọc khi chủ nhà xác nhận.', 'success')

    ck = lay_chuyen_khoan(cursor)
    return render_template(
        'dat_cho/thanh_toan.html',
        don=don,
        giao_dich=giao_dich,
        qr=url_vietqr(so_tien, ma, ck) if so_tien > 0 else None,
        so_tien=so_tien,
        ma=ma,
        chuyen_khoan=ck,
    )


@dat_cho_bp.route('/dat-phong/<int:dat_id>/xac-nhan-coc', methods=['POST'])
@require_login
@require_chu_nha
def xac_nhan_coc(dat_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    cursor.execute(
        """SELECT dp.* FROM dat_phong dp
           JOIN phong p ON p.id = dp.phong_id
           WHERE dp.id = %s AND p.nguoi_dung_id = %s""",
        (dat_id, user['id']),
    )
    don = cursor.fetchone()
    if not don:
        abort(404)
    try:
        _don, moi = ghi_nhan_coc(conn, dat_id)
    except LoiDon as exc:
        flash(exc.message, 'error')
        return redirect(url_for('admin.chu_nha_bookings'))
    flash('Đã xác nhận tiền cọc.' if moi else 'Khoản cọc này đã được ghi nhận trước đó.', 'success')
    return redirect(url_for('admin.chu_nha_bookings'))


@dat_cho_bp.route('/thanh-toan/webhook', methods=['POST'])
@csrf.exempt
def webhook_thanh_toan():
    secret = request.headers.get('X-Webhook-Secret', '')
    if not WEBHOOK_SECRET or secret != WEBHOOK_SECRET:
        abort(403)
    payload = request.get_json(silent=True) or {}
    noi_dung = str(payload.get('noi_dung') or payload.get('content') or '')
    ma = str(payload.get('ma_giao_dich') or '')
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(
        """SELECT * FROM giao_dich_thanh_toan
           WHERE ma_giao_dich = %s
              OR thong_tin_nhan = %s
              OR (%s <> '' AND thong_tin_nhan IS NOT NULL AND strpos(%s, thong_tin_nhan) > 0)
           ORDER BY id DESC LIMIT 1""",
        (ma, ma, noi_dung, noi_dung),
    )
    gd = cursor.fetchone()
    if not gd:
        return jsonify({'ok': False, 'message': 'Không thấy giao dịch'}), 404
    cursor.execute('SELECT * FROM dat_phong WHERE id = %s', (gd['dat_phong_id'],))
    don = cursor.fetchone()
    if don:
        try:
            ghi_nhan_coc(conn, don['id'])
        except LoiDon:
            return jsonify({'ok': False, 'message': 'Không ghi được cọc'}), 409
    return jsonify({'ok': True})
