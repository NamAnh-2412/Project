"""Tạo ảnh VietQR và mã nội dung chuyển khoản."""
import os
from urllib.parse import quote


def ma_coc(dat_phong_id):
    return f'COCPHONG{int(dat_phong_id)}'


def lay_chuyen_khoan(cursor):
    """Ưu tiên dòng admin đang bật. Không có thì đọc .env."""
    row = None
    try:
        cursor.execute(
            """SELECT * FROM cau_hinh_chuyen_khoan
               WHERE dang_dung IS TRUE ORDER BY id DESC LIMIT 1"""
        )
        row = cursor.fetchone()
    except Exception:
        row = None
    if row and (row.get('so_tai_khoan') or '').strip():
        return {
            'ten_ngan_hang': row.get('ten_ngan_hang') or '',
            'bank_bin': (row.get('bank_bin') or '970422').strip(),
            'so_tai_khoan': row['so_tai_khoan'].strip(),
            'chu_tai_khoan': row.get('chu_tai_khoan') or '',
            'huong_dan': row.get('huong_dan') or '',
        }
    return {
        'ten_ngan_hang': '',
        'bank_bin': (os.environ.get('BANK_BIN') or '970422').strip(),
        'so_tai_khoan': (os.environ.get('BANK_ACCOUNT') or '').strip(),
        'chu_tai_khoan': (os.environ.get('BANK_ACCOUNT_NAME') or 'CHU TRO').strip(),
        'huong_dan': '',
    }


def url_vietqr(so_tien, noi_dung, cau_hinh=None):
    cau_hinh = cau_hinh or {}
    tai_khoan = (cau_hinh.get('so_tai_khoan') or os.environ.get('BANK_ACCOUNT') or '').strip()
    if not tai_khoan:
        return None
    ngan_hang = (cau_hinh.get('bank_bin') or os.environ.get('BANK_BIN') or '970422').strip()
    ten = (cau_hinh.get('chu_tai_khoan') or os.environ.get('BANK_ACCOUNT_NAME') or 'CHU TRO').strip()
    tien = int(round(float(so_tien or 0)))
    return (
        f'https://img.vietqr.io/image/{ngan_hang}-{tai_khoan}-compact2.png'
        f'?amount={tien}&addInfo={quote(noi_dung)}&accountName={quote(ten)}'
    )
