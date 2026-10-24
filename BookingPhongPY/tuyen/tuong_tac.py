"""Đánh giá, hỏi đáp, báo cáo và thông báo. Chat nằm ở hoi_thoai.py."""
from flask import Blueprint, flash, redirect, render_template, request, session, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.thong_bao_service import tao_thong_bao
from phu_tro.bao_mat import lam_sach, so_nguyen
from phu_tro.decorators_xac_thuc import get_current_user, require_login

tuong_tac_bp = Blueprint('tuong_tac', __name__)


def _phong(cursor, room_id):
    cursor.execute('SELECT * FROM phong WHERE id = %s', (room_id,))
    return cursor.fetchone()


@tuong_tac_bp.route('/phong/<int:room_id>/danh-gia', methods=['POST'])
@require_login
def danh_gia(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    phong = _phong(cursor, room_id)
    if not user or not phong:
        return redirect(url_for('main.index'))
    cursor.execute(
        """SELECT COUNT(*) AS c FROM dat_phong
           WHERE phong_id = %s AND nguoi_dung_id = %s AND trang_thai = 'da_xac_nhan'""",
        (room_id, user['id']),
    )
    if cursor.fetchone()['c'] < 1:
        flash('Chỉ người đã được xác nhận đặt phòng mới đánh giá được.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    if not user.get('email_da_xac_thuc'):
        flash('Hãy xác thực email trước khi đánh giá.', 'error')
        return redirect(url_for('auth.xac_thuc_email'))
    so_sao = so_nguyen(request.form.get('so_sao'), 0)
    noi_dung = lam_sach(request.form.get('noi_dung'), 2000)
    if so_sao < 1 or so_sao > 5 or not noi_dung:
        flash('Chọn từ 1 đến 5 sao và viết nhận xét.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    cursor.execute(
        """INSERT INTO danh_gia (phong_id, nguoi_dung_id, so_sao, noi_dung)
           VALUES (%s, %s, %s, %s)
           ON CONFLICT (phong_id, nguoi_dung_id)
           DO UPDATE SET so_sao = EXCLUDED.so_sao, noi_dung = EXCLUDED.noi_dung, ngay_tao = CURRENT_TIMESTAMP""",
        (room_id, user['id'], so_sao, noi_dung),
    )
    tao_thong_bao(cursor, phong['nguoi_dung_id'], 'Có đánh giá mới', noi_dung[:180], url_for('rooms.room_detail', room_id=room_id))
    conn.commit()
    flash('Đã gửi đánh giá.', 'success')
    return redirect(url_for('rooms.room_detail', room_id=room_id))


@tuong_tac_bp.route('/phong/<int:room_id>/phan-hoi/<int:review_id>', methods=['POST'])
@require_login
def phan_hoi(room_id, review_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    phong = _phong(cursor, room_id)
    if not user or not phong or phong['nguoi_dung_id'] != user['id']:
        flash('Chỉ chủ phòng được trả lời đánh giá.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    noi_dung = lam_sach(request.form.get('phan_hoi'), 2000)
    cursor.execute(
        'UPDATE danh_gia SET phan_hoi_chu_nha = %s WHERE id = %s AND phong_id = %s',
        (noi_dung, review_id, room_id),
    )
    conn.commit()
    flash('Đã lưu phản hồi.', 'success')
    return redirect(url_for('rooms.room_detail', room_id=room_id))


@tuong_tac_bp.route('/phong/<int:room_id>/hoi-dap', methods=['POST'])
@require_login
def hoi_dap(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    phong = _phong(cursor, room_id)
    cau_hoi = lam_sach(request.form.get('cau_hoi'), 1000)
    if not user or not phong or not cau_hoi:
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    cursor.execute(
        'INSERT INTO hoi_dap (phong_id, nguoi_dung_id, cau_hoi) VALUES (%s, %s, %s)',
        (room_id, user['id'], cau_hoi),
    )
    if phong['nguoi_dung_id'] != user['id']:
        tao_thong_bao(cursor, phong['nguoi_dung_id'], 'Có câu hỏi mới về phòng', cau_hoi[:180], url_for('rooms.room_detail', room_id=room_id))
    conn.commit()
    flash('Đã gửi câu hỏi.', 'success')
    return redirect(url_for('rooms.room_detail', room_id=room_id))


@tuong_tac_bp.route('/phong/<int:room_id>/tra-loi/<int:qid>', methods=['POST'])
@require_login
def tra_loi(room_id, qid):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    phong = _phong(cursor, room_id)
    if not user or not phong or phong['nguoi_dung_id'] != user['id']:
        flash('Chỉ chủ phòng được trả lời.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    tra_loi_text = lam_sach(request.form.get('cau_tra_loi'), 2000)
    cursor.execute(
        'UPDATE hoi_dap SET cau_tra_loi = %s WHERE id = %s AND phong_id = %s',
        (tra_loi_text, qid, room_id),
    )
    conn.commit()
    return redirect(url_for('rooms.room_detail', room_id=room_id))


@tuong_tac_bp.route('/phong/<int:room_id>/bao-cao', methods=['POST'])
@require_login
def bao_cao(room_id):
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    noi_dung = lam_sach(request.form.get('noi_dung'), 2000)
    loai = lam_sach(request.form.get('loai'), 50) or 'khac'
    if not user or not noi_dung:
        flash('Nhập nội dung báo cáo.', 'error')
        return redirect(url_for('rooms.room_detail', room_id=room_id))
    cursor.execute(
        'INSERT INTO bao_cao (phong_id, nguoi_dung_id, loai, noi_dung) VALUES (%s, %s, %s, %s)',
        (room_id, user['id'], loai, noi_dung),
    )
    conn.commit()
    flash('Đã gửi báo cáo. Quản trị viên sẽ xem xét.', 'success')
    return redirect(url_for('rooms.room_detail', room_id=room_id))


@tuong_tac_bp.route('/thong-bao')
@require_login
def thong_bao():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return redirect(url_for('admin.dashboard'))
    cursor.execute(
        'SELECT * FROM thong_bao WHERE nguoi_dung_id = %s ORDER BY ngay_tao DESC LIMIT 50',
        (user['id'],),
    )
    ds = cursor.fetchall()
    session.pop('_tb', None)
    return render_template('tuong_tac/thong_bao.html', ds=ds)


@tuong_tac_bp.route('/thong-bao/doc-het', methods=['POST'])
@require_login
def doc_het():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if user:
        cursor.execute('UPDATE thong_bao SET da_doc = TRUE WHERE nguoi_dung_id = %s', (user['id'],))
        conn.commit()
    session.pop('_tb', None)
    return redirect(url_for('tuong_tac.thong_bao'))

from tuyen.hoi_thoai import gui_tin_nhan_api, lay_tin_nhan_api  # đăng ký route chat
