"""Chat và API tin nhắn. Route gắn vào blueprint tương tác."""
from flask import flash, jsonify, redirect, render_template, request, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from dich_vu.thong_bao_service import tao_thong_bao
from phu_tro.bao_mat import lam_sach
from phu_tro.decorators_xac_thuc import get_current_user, require_login
from tuyen.tuong_tac import tuong_tac_bp


def _hop_le_chat(cursor, user_id, doi_phuong_id, phong_id):
    cursor.execute('SELECT * FROM phong WHERE id = %s', (phong_id,))
    phong = cursor.fetchone()
    if not phong:
        return None
    chu = phong['nguoi_dung_id']
    if user_id not in (chu, doi_phuong_id) or doi_phuong_id == user_id:
        return None
    if chu not in (user_id, doi_phuong_id):
        return None
    return phong


@tuong_tac_bp.route('/chat')
@require_login
def chat_ds():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return redirect(url_for('main.index'))
    cursor.execute(
        """SELECT sub.phong_id, sub.tieu_de, sub.doi_phuong, nd.ho_ten, sub.luc
           FROM (
               SELECT tn.phong_id, p.tieu_de,
                      CASE WHEN tn.nguoi_gui_id = %s THEN tn.nguoi_nhan_id ELSE tn.nguoi_gui_id END AS doi_phuong,
                      MAX(tn.ngay_tao) AS luc
               FROM tin_nhan tn
               JOIN phong p ON p.id = tn.phong_id
               WHERE tn.nguoi_gui_id = %s OR tn.nguoi_nhan_id = %s
               GROUP BY tn.phong_id, p.tieu_de, 3
           ) sub
           JOIN nguoi_dung nd ON nd.id = sub.doi_phuong
           ORDER BY sub.luc DESC""",
        (user['id'], user['id'], user['id']),
    )
    hoi_thoai = cursor.fetchall()
    return render_template('tuong_tac/chat_ds.html', hoi_thoai=hoi_thoai)


@tuong_tac_bp.route('/chat/<int:doi_phuong_id>')
@require_login
def chat(doi_phuong_id):
    phong_id = request.args.get('phong_id', 0, type=int)
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return redirect(url_for('main.index'))
    phong = _hop_le_chat(cursor, user['id'], doi_phuong_id, phong_id)
    if not phong:
        flash('Không mở được cuộc trò chuyện này.', 'error')
        return redirect(url_for('tuong_tac.chat_ds'))
    cursor.execute('SELECT ho_ten, email FROM nguoi_dung WHERE id = %s', (doi_phuong_id,))
    doi_phuong = cursor.fetchone()
    dat_phong_id = request.args.get('dat_phong_id', 0, type=int)
    cursor.execute(
        """SELECT tn.*, COALESCE(dp.gia_thue_chot, p.gia) AS gia_the
           FROM tin_nhan tn
           JOIN phong p ON p.id = tn.phong_id
           LEFT JOIN dat_phong dp ON dp.id = tn.dat_phong_id
           WHERE tn.phong_id = %s
             AND ((tn.nguoi_gui_id = %s AND tn.nguoi_nhan_id = %s) OR (tn.nguoi_gui_id = %s AND tn.nguoi_nhan_id = %s))
           ORDER BY tn.id ASC""",
        (phong_id, user['id'], doi_phuong_id, doi_phuong_id, user['id']),
    )
    tin = cursor.fetchall()
    cursor.execute(
        """UPDATE tin_nhan SET da_doc = TRUE
           WHERE phong_id = %s AND nguoi_nhan_id = %s AND nguoi_gui_id = %s""",
        (phong_id, user['id'], doi_phuong_id),
    )
    conn.commit()
    return render_template(
        'tuong_tac/chat.html',
        phong=phong,
        doi_phuong=doi_phuong,
        doi_phuong_id=doi_phuong_id,
        tin=tin,
        toi=user['id'],
        dat_phong_id=dat_phong_id,
    )


def lay_tin_nhan_api():
    """Dùng chung cho API polling."""
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return jsonify({'ok': False}), 401
    doi_phuong_id = request.args.get('voi', 0, type=int)
    phong_id = request.args.get('phong_id', 0, type=int)
    sau = request.args.get('sau', 0, type=int)
    if not _hop_le_chat(cursor, user['id'], doi_phuong_id, phong_id):
        return jsonify({'ok': False}), 403
    cursor.execute(
        """SELECT tn.id, tn.nguoi_gui_id, tn.noi_dung, tn.ngay_tao, tn.la_the, tn.dat_phong_id,
                  p.tieu_de, COALESCE(dp.gia_thue_chot, p.gia) AS gia
           FROM tin_nhan tn
           JOIN phong p ON p.id = tn.phong_id
           LEFT JOIN dat_phong dp ON dp.id = tn.dat_phong_id
           WHERE tn.phong_id = %s AND tn.id > %s
             AND ((tn.nguoi_gui_id = %s AND tn.nguoi_nhan_id = %s) OR (tn.nguoi_gui_id = %s AND tn.nguoi_nhan_id = %s))
           ORDER BY tn.id ASC""",
        (phong_id, sau, user['id'], doi_phuong_id, doi_phuong_id, user['id']),
    )
    rows = cursor.fetchall()
    return jsonify({
        'ok': True,
        'tin': [
            {
                'id': r['id'],
                'toi_gui': r['nguoi_gui_id'] == user['id'],
                'noi_dung': r['noi_dung'],
                'la_the': bool(r.get('la_the')),
                'tieu_de': r.get('tieu_de'),
                'gia': float(r['gia'] or 0),
                'dat_phong_id': r.get('dat_phong_id'),
                'luc': r['ngay_tao'].strftime('%H:%M %d/%m') if r['ngay_tao'] else '',
            }
            for r in rows
        ],
    })


def gui_tin_nhan_api():
    conn = get_db_connection()
    cursor = get_cursor(conn)
    user = get_current_user(conn)
    if not user:
        return jsonify({'ok': False, 'message': 'Đăng nhập lại'}), 401
    doi_phuong_id = request.form.get('voi', 0, type=int)
    phong_id = request.form.get('phong_id', 0, type=int)
    noi_dung = lam_sach(request.form.get('noi_dung'), 2000)
    la_the = request.form.get('la_the') == '1'
    dat_phong_id = request.form.get('dat_phong_id', type=int)
    phong = _hop_le_chat(cursor, user['id'], doi_phuong_id, phong_id)
    if la_the and phong:
        noi_dung = noi_dung or phong['tieu_de']
    if not phong or not noi_dung:
        return jsonify({'ok': False, 'message': 'Tin nhắn không hợp lệ'}), 400
    cursor.execute(
        """INSERT INTO tin_nhan (phong_id, nguoi_gui_id, nguoi_nhan_id, noi_dung, dat_phong_id, la_the)
           VALUES (%s, %s, %s, %s, %s, %s) RETURNING id""",
        (phong_id, user['id'], doi_phuong_id, noi_dung, dat_phong_id or None, la_the),
    )
    tin_id = cursor.fetchone()['id']
    tao_thong_bao(
        cursor,
        doi_phuong_id,
        'Tin nhắn mới',
        noi_dung[:140],
        url_for('tuong_tac.chat', doi_phuong_id=user['id'], phong_id=phong_id),
    )
    conn.commit()
    return jsonify({'ok': True, 'id': tin_id})
