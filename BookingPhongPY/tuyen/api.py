"""API endpoints - favorite_action (AJAX)"""
from flask import Blueprint, request, jsonify
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor
from phu_tro.decorators_xac_thuc import is_logged_in, is_nguoi_thue, get_current_user

api_bp = Blueprint('api', __name__)


@api_bp.route('/favorite', methods=['POST'])
def favorite_toggle():
    if not is_logged_in():
        return jsonify({'success': False, 'message': 'Vui lòng đăng nhập'})

    conn = get_db_connection()
    user = get_current_user(conn)
    if not user:
        conn.close()
        return jsonify({'success': False, 'message': 'Vui lòng đăng nhập'})

    if not is_nguoi_thue(conn):
        conn.close()
        return jsonify({'success': False, 'message': 'Chỉ Người thuê mới có thể yêu thích phòng'})

    phong_id = request.form.get('phong_id', 0, type=int)
    action = request.form.get('action', '')

    if not phong_id or action != 'toggle':
        conn.close()
        return jsonify({'success': False, 'message': 'Thông tin không hợp lệ'})

    cursor = get_cursor(conn)
    try:
        cursor.execute("SELECT nguoi_dung_id FROM phong WHERE id = %s", (phong_id,))
        phong_owner = cursor.fetchone()
        if phong_owner and phong_owner['nguoi_dung_id'] == user['id']:
            return jsonify({'success': False, 'message': 'Bạn không thể yêu thích phòng của chính mình'})

        cursor.execute("SELECT id FROM phong WHERE id = %s", (phong_id,))
        if not cursor.fetchone():
            return jsonify({'success': False, 'message': 'Phòng không tồn tại'})

        cursor.execute("SELECT COUNT(*) as count FROM yeu_thich WHERE nguoi_dung_id = %s AND phong_id = %s", (user['id'], phong_id))
        is_favorite = cursor.fetchone()['count'] > 0

        if is_favorite:
            cursor.execute("DELETE FROM yeu_thich WHERE nguoi_dung_id = %s AND phong_id = %s", (user['id'], phong_id))
            conn.commit()
            return jsonify({'success': True, 'favorited': False, 'message': 'Đã bỏ yêu thích'})
        else:
            cursor.execute("INSERT INTO yeu_thich (nguoi_dung_id, phong_id) VALUES (%s, %s)", (user['id'], phong_id))
            conn.commit()
            return jsonify({'success': True, 'favorited': True, 'message': 'Đã thêm vào yêu thích'})
    except Exception as e:
        return jsonify({'success': False, 'message': f'Có lỗi xảy ra: {e}'})
    finally:
        conn.close()
