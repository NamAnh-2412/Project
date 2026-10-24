"""Helper functions - tương đương các hàm trong config/config.php"""

# Trạng thái phòng
TRANG_THAI_PHONG = {
    'con_trong': 'Còn trống',
    'da_thue': 'Đã thuê',
}

# Trạng thái booking
TRANG_THAI_BOOKING = {
    'cho_xac_nhan': {'text': 'Chờ xác nhận', 'color': '#f39c12'},
    'da_xac_nhan': {'text': 'Đã xác nhận', 'color': '#27ae60'},
    'da_huy': {'text': 'Đã hủy', 'color': '#e74c3c'},
}


def get_trang_thai_phong_text(trang_thai):
    """Lấy text trạng thái phòng"""
    return TRANG_THAI_PHONG.get(trang_thai, trang_thai or '')


def get_trang_thai_booking_info(trang_thai):
    """Lấy text và màu trạng thái booking"""
    info = TRANG_THAI_BOOKING.get(trang_thai, {'text': trang_thai or '', 'color': '#7f8c8d'})
    return info


def get_anh_chinh_subquery():
    """Subquery lấy ảnh chính của phòng"""
    return "(SELECT duong_dan_anh FROM anh_phong WHERE phong_id = p.id AND la_anh_chinh = 1 LIMIT 1) as anh_chinh"


def get_anh_dau_tien_subquery():
    """Subquery lấy ảnh đầu tiên của phòng"""
    return "(SELECT duong_dan_anh FROM anh_phong WHERE phong_id = p.id LIMIT 1) as anh_dau_tien"


def dinh_dang_tien(so):
    """15000000 -> 15 triệu, 2100000 -> 2,1 triệu, 500000 -> 500 nghìn."""
    try:
        n = float(so)
    except (TypeError, ValueError):
        return ''
    if n >= 1_000_000:
        gia_tri = n / 1_000_000
        hau = 'triệu'
    elif n >= 1000:
        gia_tri = n / 1000
        hau = 'nghìn'
    else:
        return f'{int(round(n))} đ'
    if abs(gia_tri - round(gia_tri)) < 0.05:
        chu = str(int(round(gia_tri)))
    else:
        chu = f'{gia_tri:.1f}'.replace('.', ',')
    return f'{chu} {hau}'


def anh_va_lich_hen(cursor, phong_ids, gioi_han=4):
    """Ảnh (tối đa 4) và phòng đang có lịch xem sắp tới."""
    anh = {}
    hen = set()
    ids = [int(i) for i in phong_ids]
    if not ids:
        return anh, hen
    cursor.execute(
        """SELECT phong_id, duong_dan_anh FROM anh_phong
           WHERE phong_id = ANY(%s)
           ORDER BY la_anh_chinh DESC, id ASC""",
        (ids,),
    )
    for row in cursor.fetchall():
        ds = anh.setdefault(row['phong_id'], [])
        if len(ds) < gioi_han:
            ds.append(row['duong_dan_anh'])
    cursor.execute(
        """SELECT DISTINCT phong_id FROM lich_hen_xem_phong
           WHERE phong_id = ANY(%s)
             AND trang_thai IN ('cho_xac_nhan', 'chap_nhan')
             AND thoi_gian_hen >= NOW()""",
        (ids,),
    )
    hen = {row['phong_id'] for row in cursor.fetchall()}
    return anh, hen


LOAI_PHONG = {
    'phong_tro': 'Phòng trọ',
    'chung_cu_mini': 'Chung cư mini',
    'nha_nguyen_can': 'Nhà nguyên căn',
    'homestay': 'Homestay',
    'o_ghep': 'Ở ghép',
}

SAP_XEP = {
    'moi_nhat': 'COALESCE(p.is_vip, FALSE) DESC, p.ngay_tao DESC, p.id DESC',
    'gia_tang': 'p.gia ASC, p.id DESC',
    'gia_giam': 'p.gia DESC, p.id DESC',
    'dien_tich_lon': 'p.dien_tich DESC NULLS LAST, p.id DESC',
    'dien_tich_nho': 'p.dien_tich ASC NULLS LAST, p.id DESC',
}


def nhan_loai_phong(ma):
    return LOAI_PHONG.get(ma, ma or '')


def thong_tin_trang(page, total, per_page=9):
    try:
        page = int(page or 1)
    except (TypeError, ValueError):
        page = 1
    if page < 1:
        page = 1
    pages = max(1, (int(total) + per_page - 1) // per_page)
    if page > pages:
        page = pages
    return {
        'page': page,
        'pages': pages,
        'per_page': per_page,
        'total': int(total),
        'offset': (page - 1) * per_page,
    }


def dieu_kien_phong_cong_khai(alias='p'):
    return (
        f"COALESCE({alias}.trang_thai_duyet, 'da_duyet') = 'da_duyet' "
        f"AND COALESCE({alias}.an_tin, FALSE) = FALSE"
    )


def url_anh(path):
    """Đường dẫn lưu trong database thành URL ảnh trên web."""
    from flask import url_for

    if not path:
        return url_for('static', filename='images/no-image.svg')
    chuoi = str(path)
    if chuoi.startswith('uploads/'):
        return url_for('serve_upload', filename=chuoi.replace('uploads/', '', 1))
    if chuoi.startswith('assets/'):
        return url_for('static', filename=chuoi.replace('assets/', '', 1))
    return url_for('static', filename=chuoi)
