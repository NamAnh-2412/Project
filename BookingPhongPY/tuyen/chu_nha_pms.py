"""Hợp đồng, hóa đơn và lịch hẹn phía chủ nhà."""
from datetime import date

from flask import Blueprint, flash, redirect, render_template, request, send_file, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.dat_phong_service import LoiDon, tao_hop_dong
from dich_vu.hoa_don_service import tao_pdf_hoa_don, tinh_hoa_don
from dich_vu.thong_bao_service import tao_thong_bao
from phu_tro.bao_mat import lam_sach, so_nguyen, so_thuc
from phu_tro.decorators_xac_thuc import get_current_user, require_chu_nha, require_login

pms_bp = Blueprint('pms', __name__)


def _phong_cua_toi(cursor, phong_id, user_id):
    cursor.execute('SELECT * FROM phong WHERE id = %s AND nguoi_dung_id = %s', (phong_id, user_id))
    return cursor.fetchone()


@pms_bp.route('/lich-hen', methods=['GET', 'POST'])
@require_login
@require_chu_nha
def lich_hen():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if request.method == 'POST':
        lich_id = request.form.get('lich_id', type=int)
        action = request.form.get('action')
        gio_moi = (request.form.get('gio_moi') or '').strip()
        cursor.execute(
            """SELECT lh.*, p.tieu_de FROM lich_hen_xem_phong lh
               JOIN phong p ON p.id = lh.phong_id
               WHERE lh.id = %s AND p.nguoi_dung_id = %s""",
            (lich_id, user['id']),
        )
        lich = cursor.fetchone()
        if lich and action in ('chap_nhan', 'tu_choi', 'doi_gio', 'hoan_thanh'):
            trang = {'chap_nhan': 'chap_nhan', 'tu_choi': 'tu_choi', 'hoan_thanh': 'hoan_thanh', 'doi_gio': 'cho_xac_nhan'}[action]
            if action == 'doi_gio' and gio_moi:
                cursor.execute(
                    """UPDATE lich_hen_xem_phong
                       SET trang_thai = %s, thoi_gian_hen = %s, ghi_chu = COALESCE(ghi_chu, '') || %s
                       WHERE id = %s""",
                    (trang, gio_moi.replace('T', ' '), ' | Chủ nhà đề xuất giờ mới', lich_id),
                )
            else:
                cursor.execute('UPDATE lich_hen_xem_phong SET trang_thai = %s WHERE id = %s', (trang, lich_id))
            tao_thong_bao(
                cursor,
                lich['nguoi_dung_id'],
                'Lịch xem phòng được cập nhật',
                f"{lich['tieu_de']}: {trang}",
                url_for('dat_cho.lich_cua_toi'),
            )
            conn.commit()
            flash('Đã cập nhật lịch hẹn.', 'success')
        return redirect(url_for('pms.lich_hen'))

    cursor.execute(
        """SELECT lh.*, p.tieu_de, nd.ho_ten, nd.email, nd.so_dien_thoai
           FROM lich_hen_xem_phong lh
           JOIN phong p ON p.id = lh.phong_id
           JOIN nguoi_dung nd ON nd.id = lh.nguoi_dung_id
           WHERE p.nguoi_dung_id = %s
           ORDER BY lh.thoi_gian_hen DESC""",
        (user['id'],),
    )
    lich = cursor.fetchall()
    return render_template('chu_nha/lich_hen.html', lich=lich)


@pms_bp.route('/hop-dong', methods=['GET', 'POST'])
@require_login
@require_chu_nha
def hop_dong():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    error = ''
    if request.method == 'POST':
        phong_id = request.form.get('phong_id', type=int)
        email = lam_sach(request.form.get('email_khach'), 255)
        ngay_bat_dau = (request.form.get('ngay_bat_dau') or '').strip()
        ngay_ket_thuc = (request.form.get('ngay_ket_thuc') or '').strip() or None
        gia = so_thuc(request.form.get('gia_thue'), None)
        coc = so_thuc(request.form.get('tien_dat_coc'), 0) or 0
        phong = _phong_cua_toi(cursor, phong_id, user['id'])
        cursor.execute("SELECT * FROM nguoi_dung WHERE email = %s AND vai_tro = 'nguoi_thue'", (email,))
        khach = cursor.fetchone()
        if not phong or not khach or not ngay_bat_dau or gia is None:
            error = 'Chọn phòng của bạn, email người thuê đã có tài khoản, ngày bắt đầu và giá thuê.'
        else:
            try:
                tao_hop_dong(conn, user['id'], phong_id, khach['id'], ngay_bat_dau, ngay_ket_thuc, gia, coc)
                flash('Đã tạo hợp đồng.', 'success')
                return redirect(url_for('pms.hop_dong'))
            except LoiDon as exc:
                error = exc.message

    cursor.execute('SELECT id, tieu_de FROM phong WHERE nguoi_dung_id = %s ORDER BY id DESC', (user['id'],))
    phong_list = cursor.fetchall()
    cursor.execute(
        """SELECT hd.*, p.tieu_de, nd.ho_ten, nd.email
           FROM hop_dong_thue hd
           JOIN phong p ON p.id = hd.phong_id
           JOIN nguoi_dung nd ON nd.id = hd.nguoi_dung_id
           WHERE p.nguoi_dung_id = %s
           ORDER BY hd.id DESC""",
        (user['id'],),
    )
    hop_dong_list = cursor.fetchall()
    return render_template('chu_nha/hop_dong.html', phong_list=phong_list, hop_dong_list=hop_dong_list, error=error, hom_nay=date.today().isoformat())


@pms_bp.route('/hoa-don', methods=['GET', 'POST'])
@require_login
@require_chu_nha
def hoa_don():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    error = ''
    if request.method == 'POST':
        hop_dong_id = request.form.get('hop_dong_id', type=int)
        thang = lam_sach(request.form.get('thang_nam'), 7)
        so_dien_cu = so_nguyen(request.form.get('so_dien_cu'), 0)
        so_dien_moi = so_nguyen(request.form.get('so_dien_moi'), 0)
        so_nuoc_cu = so_nguyen(request.form.get('so_nuoc_cu'), 0)
        so_nuoc_moi = so_nguyen(request.form.get('so_nuoc_moi'), 0)
        cursor.execute(
            """SELECT hd.*, p.tien_dien, p.tien_nuoc, p.tien_dich_vu, p.tieu_de, p.nguoi_dung_id AS chu_id
               FROM hop_dong_thue hd JOIN phong p ON p.id = hd.phong_id
               WHERE hd.id = %s AND p.nguoi_dung_id = %s""",
            (hop_dong_id, user['id']),
        )
        hd = cursor.fetchone()
        if not hd or len(thang) != 7 or thang[4] != '-':
            error = 'Chọn hợp đồng và tháng dạng YYYY-MM.'
        elif so_dien_moi < so_dien_cu or so_nuoc_moi < so_nuoc_cu:
            error = 'Số mới phải lớn hơn hoặc bằng số cũ.'
        else:
            gia_dien = float(hd['tien_dien'] or 0)
            gia_nuoc = float(hd['tien_nuoc'] or 0)
            tien_dv = float(hd['tien_dich_vu'] or 0)
            ket_qua = tinh_hoa_don(so_dien_cu, so_dien_moi, gia_dien, so_nuoc_cu, so_nuoc_moi, gia_nuoc, float(hd['gia_thue_hang_thang']), tien_dv)
            try:
                cursor.execute(
                    """INSERT INTO hoa_don_thang
                       (hop_dong_id, thang_nam, so_dien_cu, so_dien_moi, gia_dien,
                        so_nuoc_cu, so_nuoc_moi, gia_nuoc, tien_phong, tien_dich_vu, tong_tien)
                       VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                    (hop_dong_id, thang, so_dien_cu, so_dien_moi, gia_dien, so_nuoc_cu, so_nuoc_moi, gia_nuoc,
                     float(hd['gia_thue_hang_thang']), tien_dv, ket_qua['tong']),
                )
                tao_thong_bao(
                    cursor,
                    hd['nguoi_dung_id'],
                    f"Hóa đơn {thang}",
                    f"{hd['tieu_de']}: {ket_qua['tong']:,.0f} đ",
                    url_for('pms.hoa_don'),
                )
                conn.commit()
                flash(f"Đã chốt hóa đơn. Tổng {ket_qua['tong']:,.0f} đ.", 'success')
                return redirect(url_for('pms.hoa_don'))
            except Exception:
                error = 'Tháng này đã có hóa đơn cho hợp đồng đó.'

    cursor.execute(
        """SELECT hd.id, p.tieu_de, nd.ho_ten
           FROM hop_dong_thue hd
           JOIN phong p ON p.id = hd.phong_id
           JOIN nguoi_dung nd ON nd.id = hd.nguoi_dung_id
           WHERE p.nguoi_dung_id = %s AND hd.trang_thai = 'dang_hieu_luc'
           ORDER BY hd.id DESC""",
        (user['id'],),
    )
    hop_dong_list = cursor.fetchall()
    cursor.execute(
        """SELECT h.*, p.tieu_de, nd.ho_ten
           FROM hoa_don_thang h
           JOIN hop_dong_thue hd ON hd.id = h.hop_dong_id
           JOIN phong p ON p.id = hd.phong_id
           JOIN nguoi_dung nd ON nd.id = hd.nguoi_dung_id
           WHERE p.nguoi_dung_id = %s
           ORDER BY h.id DESC""",
        (user['id'],),
    )
    hoa_don_list = cursor.fetchall()
    return render_template('chu_nha/hoa_don.html', hop_dong_list=hop_dong_list, hoa_don_list=hoa_don_list, error=error)


def _lay_hoa_don_chu(cursor, hoa_don_id, user_id):
    cursor.execute(
        """SELECT h.*, hd.nguoi_dung_id, hd.gia_thue_hang_thang,
                  p.tieu_de, p.dia_chi, p.nguoi_dung_id AS chu_id,
                  nd.ho_ten, nd.email
           FROM hoa_don_thang h
           JOIN hop_dong_thue hd ON hd.id = h.hop_dong_id
           JOIN phong p ON p.id = hd.phong_id
           JOIN nguoi_dung nd ON nd.id = hd.nguoi_dung_id
           WHERE h.id = %s AND p.nguoi_dung_id = %s""",
        (hoa_don_id, user_id),
    )
    return cursor.fetchone()


@pms_bp.route('/hoa-don/<int:hoa_don_id>')
@require_login
@require_chu_nha
def in_hoa_don(hoa_don_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    row = _lay_hoa_don_chu(cursor, hoa_don_id, user['id'])
    if not row:
        flash('Không thấy hóa đơn.', 'error')
        return redirect(url_for('pms.hoa_don'))
    return render_template('chu_nha/hoa_don_in.html', hd=row)


@pms_bp.route('/hoa-don/<int:hoa_don_id>/pdf')
@require_login
@require_chu_nha
def pdf_hoa_don(hoa_don_id):
    from io import BytesIO
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    row = _lay_hoa_don_chu(cursor, hoa_don_id, user['id'])
    if not row:
        flash('Không thấy hóa đơn.', 'error')
        return redirect(url_for('pms.hoa_don'))
    pdf = tao_pdf_hoa_don(row, row, row, row)
    return send_file(BytesIO(pdf), mimetype='application/pdf', as_attachment=True, download_name=f'hoa-don-{hoa_don_id}.pdf')


@pms_bp.route('/hoa-don/<int:hoa_don_id>/da-dong', methods=['POST'])
@require_login
@require_chu_nha
def da_dong(hoa_don_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    row = _lay_hoa_don_chu(cursor, hoa_don_id, user['id'])
    if row:
        cursor.execute("UPDATE hoa_don_thang SET trang_thai = 'da_dong' WHERE id = %s", (hoa_don_id,))
        tao_thong_bao(cursor, row['nguoi_dung_id'], 'Hóa đơn đã được ghi nhận thanh toán', row['tieu_de'], None)
        conn.commit()
        flash('Đã đánh dấu đã thu.', 'success')
    return redirect(url_for('pms.hoa_don'))
