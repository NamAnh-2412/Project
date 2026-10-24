"""Duyệt tin, báo cáo, tiện ích, xác minh và khóa tài khoản."""
from flask import Blueprint, flash, redirect, render_template, request, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.thong_bao_service import tao_thong_bao
from dich_vu.upload_service import luu_anh_webp
from phu_tro.bao_mat import lam_sach, so_nguyen, so_thuc
from phu_tro.decorators_xac_thuc import get_current_admin, require_admin

admin_ext_bp = Blueprint('admin_ext', __name__)


def _log(cursor, admin_id, hanh_dong, chi_tiet):
    if not admin_id:
        return
    cursor.execute(
        'INSERT INTO admin_log (admin_id, hanh_dong, chi_tiet) VALUES (%s, %s, %s)',
        (admin_id, hanh_dong[:100], chi_tiet),
    )


@admin_ext_bp.route('/duyet-tin', methods=['GET', 'POST'])
@require_admin
def duyet_tin():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    admin = get_current_admin(conn)
    if request.method == 'POST':
        phong_id = request.form.get('phong_id', type=int)
        action = request.form.get('action')
        cursor.execute('SELECT * FROM phong WHERE id = %s', (phong_id,))
        phong = cursor.fetchone()
        if phong and action in ('da_duyet', 'tu_choi', 'khoa'):
            if action == 'khoa':
                cursor.execute("UPDATE phong SET an_tin = TRUE, trang_thai_duyet = 'tu_choi' WHERE id = %s", (phong_id,))
            else:
                cursor.execute(
                    'UPDATE phong SET trang_thai_duyet = %s, an_tin = FALSE WHERE id = %s',
                    (action, phong_id),
                )
            tao_thong_bao(
                cursor,
                phong['nguoi_dung_id'],
                'Tin đăng được cập nhật',
                f"{phong['tieu_de']}: {action}",
                url_for('rooms.room_detail', room_id=phong_id),
            )
            _log(cursor, admin['id'] if admin else None, 'duyet_tin', f'{phong_id}:{action}')
            conn.commit()
            flash('Đã cập nhật tin đăng.', 'success')
        return redirect(url_for('admin_ext.duyet_tin'))

    cursor.execute(
        """SELECT p.*, nd.ho_ten, nd.email
           FROM phong p LEFT JOIN nguoi_dung nd ON nd.id = p.nguoi_dung_id
           ORDER BY CASE WHEN p.trang_thai_duyet = 'cho_duyet' THEN 0 ELSE 1 END, p.id DESC
           LIMIT 100"""
    )
    ds = cursor.fetchall()
    return render_template('admin/duyet_tin.html', ds=ds)


@admin_ext_bp.route('/bao-cao', methods=['GET', 'POST'])
@require_admin
def bao_cao():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    admin = get_current_admin(conn)
    if request.method == 'POST':
        bao_cao_id = request.form.get('bao_cao_id', type=int)
        trang = request.form.get('trang_thai')
        if trang in ('moi', 'dang_xu_ly', 'da_xu_ly'):
            cursor.execute('UPDATE bao_cao SET trang_thai = %s WHERE id = %s', (trang, bao_cao_id))
            _log(cursor, admin['id'] if admin else None, 'bao_cao', f'{bao_cao_id}:{trang}')
            conn.commit()
            flash('Đã cập nhật báo cáo.', 'success')
        return redirect(url_for('admin_ext.bao_cao'))
    cursor.execute(
        """SELECT b.*, p.tieu_de, nd.ho_ten, nd.email
           FROM bao_cao b
           LEFT JOIN phong p ON p.id = b.phong_id
           LEFT JOIN nguoi_dung nd ON nd.id = b.nguoi_dung_id
           ORDER BY b.id DESC LIMIT 100"""
    )
    ds = cursor.fetchall()
    return render_template('admin/bao_cao.html', ds=ds)


@admin_ext_bp.route('/tien-ich', methods=['GET', 'POST'])
@require_admin
def tien_ich():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    if request.method == 'POST':
        action = request.form.get('action')
        if action == 'xoa':
            cursor.execute('DELETE FROM tien_ich WHERE id = %s', (request.form.get('id', type=int),))
        else:
            ten = lam_sach(request.form.get('ten'), 100)
            icon = lam_sach(request.form.get('icon'), 20)
            if ten:
                cursor.execute('INSERT INTO tien_ich (ten, icon) VALUES (%s, %s)', (ten, icon or None))
        conn.commit()
        flash('Đã cập nhật tiện ích.', 'success')
        return redirect(url_for('admin_ext.tien_ich'))
    cursor.execute('SELECT * FROM tien_ich ORDER BY ten')
    ds = cursor.fetchall()
    return render_template('admin/tien_ich.html', ds=ds)


@admin_ext_bp.route('/xac-minh/<int:user_id>', methods=['POST'])
@require_admin
def xac_minh(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    admin = get_current_admin(conn)
    cursor.execute('UPDATE nguoi_dung SET is_verified = NOT COALESCE(is_verified, FALSE) WHERE id = %s RETURNING is_verified, ho_ten', (user_id,))
    row = cursor.fetchone()
    if row:
        _log(cursor, admin['id'] if admin else None, 'xac_minh', f"{user_id}:{row['is_verified']}")
        tao_thong_bao(cursor, user_id, 'Trạng thái xác minh thay đổi', 'Quản trị viên đã cập nhật dấu uy tín.', url_for('tai_khoan.ho_so'))
        conn.commit()
        flash('Đã đổi trạng thái xác minh.', 'success')
    return redirect(request.referrer or url_for('admin.manage_chu_nha'))


@admin_ext_bp.route('/khoa/<int:user_id>', methods=['POST'])
@require_admin
def khoa_tai_khoan(user_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    admin = get_current_admin(conn)
    cursor.execute(
        'UPDATE nguoi_dung SET bi_khoa = NOT COALESCE(bi_khoa, FALSE) WHERE id = %s RETURNING bi_khoa',
        (user_id,),
    )
    row = cursor.fetchone()
    if row:
        _log(cursor, admin['id'] if admin else None, 'khoa_tai_khoan', f"{user_id}:{row['bi_khoa']}")
        conn.commit()
        flash('Đã đổi trạng thái khóa tài khoản.', 'success')
    return redirect(request.referrer or url_for('admin.dashboard'))


@admin_ext_bp.route('/vip/<int:phong_id>', methods=['POST'])
@require_admin
def vip(phong_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    cursor.execute(
        'UPDATE phong SET is_vip = NOT COALESCE(is_vip, FALSE) WHERE id = %s',
        (phong_id,),
    )
    conn.commit()
    flash('Đã đổi gói tin nổi bật.', 'success')
    return redirect(url_for('admin_ext.duyet_tin'))


@admin_ext_bp.route('/chuyen-khoan', methods=['GET', 'POST'])
@require_admin
def chuyen_khoan():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    if request.method == 'POST':
        so = lam_sach(request.form.get('so_tai_khoan'), 40)
        if not so:
            flash('Nhập số tài khoản.', 'error')
        else:
            cursor.execute('UPDATE cau_hinh_chuyen_khoan SET dang_dung = FALSE WHERE dang_dung IS TRUE')
            cursor.execute(
                """INSERT INTO cau_hinh_chuyen_khoan
                   (ten_ngan_hang, bank_bin, so_tai_khoan, chu_tai_khoan, huong_dan, dang_dung)
                   VALUES (%s, %s, %s, %s, %s, TRUE)""",
                (
                    lam_sach(request.form.get('ten_ngan_hang'), 120),
                    lam_sach(request.form.get('bank_bin'), 20) or '970422',
                    so,
                    lam_sach(request.form.get('chu_tai_khoan'), 120),
                    lam_sach(request.form.get('huong_dan'), 500),
                ),
            )
            conn.commit()
            flash('Đã lưu tài khoản nhận cọc.', 'success')
    cursor.execute(
        'SELECT * FROM cau_hinh_chuyen_khoan WHERE dang_dung IS TRUE ORDER BY id DESC LIMIT 1'
    )
    cau_hinh = cursor.fetchone()
    return render_template('admin/chuyen_khoan.html', cau_hinh=cau_hinh)


@admin_ext_bp.route('/banner', methods=['GET', 'POST'])
@require_admin
def banner():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    if request.method == 'POST':
        action = request.form.get('action')
        if action == 'xoa':
            cursor.execute('DELETE FROM banner_trang_chu WHERE id = %s', (request.form.get('banner_id', type=int),))
            conn.commit()
        else:
            tieu_de = lam_sach(request.form.get('tieu_de'), 255)
            if tieu_de:
                anh = luu_anh_webp(request.files.get('anh'), 0, request.form.get('thu_tu', type=int) or 0)
                cursor.execute(
                    """INSERT INTO banner_trang_chu (tieu_de, anh, lien_ket, thu_tu, dang_hien)
                       VALUES (%s, %s, %s, %s, TRUE)""",
                    (
                        tieu_de,
                        anh,
                        lam_sach(request.form.get('lien_ket'), 500),
                        so_nguyen(request.form.get('thu_tu'), 0),
                    ),
                )
                conn.commit()
                flash('Đã thêm banner.', 'success')
    cursor.execute('SELECT * FROM banner_trang_chu ORDER BY thu_tu, id')
    ds = cursor.fetchall()
    return render_template('admin/banner.html', ds=ds)


@admin_ext_bp.route('/ma-giam', methods=['GET', 'POST'])
@require_admin
def ma_giam():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    if request.method == 'POST':
        ma = (lam_sach(request.form.get('ma'), 40) or '').upper()
        loai = request.form.get('loai')
        if loai not in ('phan_tram', 'so_tien'):
            loai = 'so_tien'
        gia_tri = so_thuc(request.form.get('gia_tri'), None)
        if not ma or gia_tri is None:
            flash('Nhập mã và giá trị giảm.', 'error')
        else:
            cursor.execute(
                """INSERT INTO ma_giam (ma, loai, gia_tri, tran_giam, het_han, so_lan_toi_da)
                   VALUES (%s, %s, %s, %s, %s, %s)
                   ON CONFLICT (ma) DO UPDATE
                   SET loai = EXCLUDED.loai, gia_tri = EXCLUDED.gia_tri, dang_dung = TRUE""",
                (
                    ma,
                    loai,
                    gia_tri,
                    so_thuc(request.form.get('tran_giam'), None),
                    (request.form.get('het_han') or '').strip() or None,
                    request.form.get('so_lan_toi_da', type=int),
                ),
            )
            conn.commit()
            flash('Đã lưu mã giảm. Mã chỉ trừ tiền thuê tháng đầu, không trừ tiền cọc.', 'success')
    cursor.execute('SELECT * FROM ma_giam ORDER BY id DESC')
    ds = cursor.fetchall()
    return render_template('admin/ma_giam.html', ds=ds)
