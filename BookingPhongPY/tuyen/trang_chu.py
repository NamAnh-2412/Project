"""Tuyến trang chủ, tìm kiếm và bản đồ."""
from urllib.parse import urlencode

from flask import Blueprint, render_template, request, session, url_for

from cau_hinh.co_so_du_lieu import get_cursor, get_db_connection
from phu_tro.decorators_xac_thuc import is_logged_in
from phu_tro.tro_giup import (
    LOAI_PHONG,
    SAP_XEP,
    anh_va_lich_hen,
    dieu_kien_phong_cong_khai,
    get_anh_chinh_subquery,
    get_anh_dau_tien_subquery,
    thong_tin_trang,
)

main_bp = Blueprint('main', __name__)


def _tham_so_loc():
    cap = [(k, v) for k, v in request.args.items(multi=True) if k != 'page' and v != '']
    return urlencode(cap)


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
    loai_phong = request.args.get('loai_phong', '')
    sap_xep = request.args.get('sap_xep', 'moi_nhat')
    tien_ich_chon = [int(x) for x in request.args.getlist('tien_ich') if str(x).isdigit()]

    where_conditions = [dieu_kien_phong_cong_khai('p')]
    params = []

    if trang_thai_filter != 'all':
        where_conditions.append('p.trang_thai = %s')
        params.append(trang_thai_filter)

    if search_query:
        search_param = f'%{search_query}%'
        where_conditions.append('(p.tieu_de ILIKE %s OR p.dia_chi ILIKE %s OR p.mo_ta ILIKE %s)')
        params.extend([search_param, search_param, search_param])

    if dien_tich_min and str(dien_tich_min).replace('.', '').isdigit():
        where_conditions.append('p.dien_tich >= %s')
        params.append(float(dien_tich_min))

    if dien_tich_max and str(dien_tich_max).replace('.', '').isdigit():
        where_conditions.append('p.dien_tich <= %s')
        params.append(float(dien_tich_max))

    if gia_min and str(gia_min).replace('.', '').replace('-', '').isdigit():
        where_conditions.append('p.gia >= %s')
        params.append(float(gia_min))

    if gia_max and str(gia_max).replace('.', '').replace('-', '').isdigit():
        where_conditions.append('p.gia <= %s')
        params.append(float(gia_max))

    if dia_chi_filter:
        dia_chi_param = f'%{dia_chi_filter}%'
        where_conditions.append('(p.dia_chi ILIKE %s OR p.quan_huyen ILIKE %s)')
        params.extend([dia_chi_param, dia_chi_param])

    if loai_phong in LOAI_PHONG:
        where_conditions.append('p.loai_phong = %s')
        params.append(loai_phong)

    if tien_ich_chon:
        placeholders = ','.join(['%s'] * len(tien_ich_chon))
        where_conditions.append(
            f"""p.id IN (
                SELECT phong_id FROM phong_tien_ich
                WHERE tien_ich_id IN ({placeholders})
                GROUP BY phong_id
                HAVING COUNT(DISTINCT tien_ich_id) = %s
            )"""
        )
        params.extend(tien_ich_chon)
        params.append(len(tien_ich_chon))

    where_clause = 'WHERE ' + ' AND '.join(where_conditions)
    order_sql = SAP_XEP.get(sap_xep, SAP_XEP['moi_nhat'])

    cursor.execute(f'SELECT COUNT(*) AS total FROM phong p {where_clause}', params)
    total = cursor.fetchone()['total']
    trang = thong_tin_trang(request.args.get('page', 1), total, per_page=9)

    cursor.execute(
        f"""SELECT p.*, nd.ho_ten as chu_nha_ten,
               {get_anh_chinh_subquery()}, {get_anh_dau_tien_subquery()},
               (SELECT ROUND(AVG(so_sao)::numeric, 1) FROM danh_gia dg WHERE dg.phong_id = p.id) AS diem_tb,
               (SELECT COUNT(*) FROM danh_gia dg WHERE dg.phong_id = p.id) AS so_danh_gia
            FROM phong p
            LEFT JOIN nguoi_dung nd ON p.nguoi_dung_id = nd.id
            {where_clause}
            ORDER BY {order_sql}
            LIMIT %s OFFSET %s""",
        params + [trang['per_page'], trang['offset']],
    )
    rooms = cursor.fetchall()

    cursor.execute(
        f"""SELECT trang_thai, COUNT(*) AS count FROM phong p
            WHERE {dieu_kien_phong_cong_khai('p')}
            GROUP BY trang_thai"""
    )
    trang_thai_counts = {'all': 0, 'con_trong': 0, 'da_thue': 0}
    for row in cursor.fetchall():
        trang_thai_counts[row['trang_thai']] = row['count']
    trang_thai_counts['all'] = sum(v for k, v in trang_thai_counts.items() if k != 'all')

    favorite_rooms = []
    if is_logged_in() and 'user_id' in session:
        user_id = session.get('user_id')
        cursor.execute(
            """SELECT yt.phong_id FROM yeu_thich yt
               INNER JOIN phong p ON yt.phong_id = p.id
               WHERE yt.nguoi_dung_id = %s AND p.nguoi_dung_id != %s""",
            (user_id, user_id),
        )
        favorite_rooms = [r['phong_id'] for r in cursor.fetchall()]

    cursor.execute('SELECT * FROM tien_ich ORDER BY ten')
    tien_ich_list = cursor.fetchall()

    cursor.execute(
        'SELECT * FROM banner_trang_chu WHERE dang_hien IS TRUE ORDER BY thu_tu, id'
    )
    banners = cursor.fetchall()
    anh_theo_phong, phong_co_hen = anh_va_lich_hen(cursor, [room['id'] for room in rooms])
    chip_loai = []
    for ma, ten in [('', 'Tất cả')] + list(LOAI_PHONG.items()):
        q = request.args.to_dict(flat=True)
        q.pop('page', None)
        if ma:
            q['loai_phong'] = ma
        else:
            q.pop('loai_phong', None)
        chip_loai.append({
            'ten': ten,
            'chon': (loai_phong or '') == ma,
            'href': url_for('main.index') + ('?' + urlencode(q) if q else ''),
        })
    return render_template(
        'index.html',
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
        loai_phong=loai_phong,
        loai_phong_ds=LOAI_PHONG,
        sap_xep=sap_xep,
        tien_ich_list=tien_ich_list,
        tien_ich_chon=tien_ich_chon,
        phan_trang=trang,
        tham_so=_tham_so_loc(),
        banners=banners,
        anh_theo_phong=anh_theo_phong,
        phong_co_hen=phong_co_hen,
        chip_loai=chip_loai,
    )
