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
