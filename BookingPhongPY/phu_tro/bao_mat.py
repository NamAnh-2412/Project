"""Làm sạch dữ liệu người dùng gửi lên."""


def lam_sach(text, max_len=2000):
    if text is None:
        return ''
    sach = str(text).replace('\x00', '').strip()
    return sach[:max_len]


def so_thuc(value, mac_dinh=None):
    if value is None or str(value).strip() == '':
        return mac_dinh
    try:
        return float(str(value).replace(',', ''))
    except (TypeError, ValueError):
        return mac_dinh


def so_nguyen(value, mac_dinh=None):
    if value is None or str(value).strip() == '':
        return mac_dinh
    try:
        return int(float(value))
    except (TypeError, ValueError):
        return mac_dinh
