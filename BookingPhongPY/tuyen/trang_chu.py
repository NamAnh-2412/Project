"""Tuyến trang chủ và danh sách phòng"""
from flask import Blueprint, render_template, request, session
from cau_hinh.co_so_du_lieu import get_db_connection, get_cursor
from phu_tro.decorators_xac_thuc import is_logged_in
from phu_tro.tro_giup import get_anh_chinh_subquery, get_anh_dau_tien_subquery, get_trang_thai_phong_text

main_bp = Blueprint('main', __name__)


@main_bp.route('/')
def index():
    conn = get_db_connection()
    cursor = get_cursor(conn)

    trang_thai_filter = request.args.get('trang_thai', 'all')
    search_query = request.args.get('search', '')
    dien_tich_min = request.args.get('dien_tich_min', '')
    dien_tich_max = request.args.get('dien_tich_max', '')
    gia_min = request.args.get('gia_min', '')
    gia_max = request.args.get('gia_max', '')
    dia_chi_filter = request.args.get('dia_chi', '')

    where_conditions = []
    params = []

    if trang_thai_filter != 'all':
        where_conditions.append("p.trang_thai = %s")
        params.append(trang_thai_filter)

    if search_query:
        search_param = f"%{search_query}%"
        where_conditions.append("(p.tieu_de LIKE %s OR p.dia_chi LIKE %s OR p.mo_ta LIKE %s)")
        params.extend([search_param, search_param, search_param])

    if dien_tich_min and dien_tich_min.replace('.', '').isdigit():
        where_conditions.append("p.dien_tich >= %s")
        params.append(float(dien_tich_min))

    if dien_tich_max and dien_tich_max.replace('.', '').isdigit():
        where_conditions.append("p.dien_tich <= %s")
        params.append(float(dien_tich_max))

    if gia_min and gia_min.replace('.', '').replace('-', '').isdigit():
        where_conditions.append("p.gia >= %s")
        params.append(float(gia_min))

    if gia_max and gia_max.replace('.', '').replace('-', '').isdigit():
        where_conditions.append("p.gia <= %s")
        params.append(float(gia_max))

    if dia_chi_filter:
        dia_chi_param = f"%{dia_chi_filter}%"
        where_conditions.append("(p.dia_chi LIKE %s OR p.quan_huyen LIKE %s)")
        params.extend([dia_chi_param, dia_chi_param])

    where_clause = "WHERE " + " AND ".join(where_conditions) if where_conditions else ""

    sql = f"""SELECT p.*, nd.ho_ten as chu_nha_ten,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()}
        FROM phong p
        LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
        {where_clause}
        ORDER BY p.id DESC, p.ngay_tao DESC"""
    cursor.execute(sql, params)
    rooms = cursor.fetchall()

    cursor.execute("SELECT trang_thai, COUNT(*) as count FROM phong GROUP BY trang_thai")
    trang_thai_counts = {'all': 0, 'con_trong': 0, 'da_thue': 0}
    for row in cursor.fetchall():
        trang_thai_counts[row['trang_thai']] = row['count']
    trang_thai_counts['all'] = sum(trang_thai_counts.values())

    favorite_rooms = []
    if is_logged_in() and 'user_id' in session:
        user_id = session.get('user_id')
        cursor.execute("""
            SELECT yt.phong_id FROM yeu_thich yt
            INNER JOIN phong p ON yt.phong_id = p.id
            WHERE yt.nguoi_dung_id = %s AND p.nguoi_dung_id != %s
        """, (user_id, user_id))
        favorite_rooms = [r['phong_id'] for r in cursor.fetchall()]

    conn.close()

    return render_template('index.html',
        rooms=rooms,
        favorite_rooms=favorite_rooms,
        trang_thai_counts=trang_thai_counts,
        trang_thai_filter=trang_thai_filter,
        search_query=search_query,
        dien_tich_min=dien_tich_min,
        dien_tich_max=dien_tich_max,
        gia_min=gia_min,
        gia_max=gia_max,
        dia_chi_filter=dia_chi_filter,
    )
