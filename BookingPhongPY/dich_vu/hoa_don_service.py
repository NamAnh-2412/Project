"""Tính tiền hóa đơn tháng và xuất PDF tiếng Việt."""
import os

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas


def tinh_hoa_don(so_dien_cu, so_dien_moi, gia_dien, so_nuoc_cu, so_nuoc_moi, gia_nuoc, tien_phong, tien_dich_vu):
    dien_tieu_thu = max(0, int(so_dien_moi) - int(so_dien_cu))
    nuoc_tieu_thu = max(0, int(so_nuoc_moi) - int(so_nuoc_cu))
    tien_dien = dien_tieu_thu * float(gia_dien or 0)
    tien_nuoc = nuoc_tieu_thu * float(gia_nuoc or 0)
    tong = tien_dien + tien_nuoc + float(tien_phong or 0) + float(tien_dich_vu or 0)
    return {
        'dien_tieu_thu': dien_tieu_thu,
        'nuoc_tieu_thu': nuoc_tieu_thu,
        'tien_dien': tien_dien,
        'tien_nuoc': tien_nuoc,
        'tong': tong,
    }


def _font_tieng_viet():
    ung_vien = [
        r'C:\Windows\Fonts\arial.ttf',
        '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',
        '/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf',
    ]
    for path in ung_vien:
        if os.path.isfile(path):
            pdfmetrics.registerFont(TTFont('InvoiceFont', path))
            return 'InvoiceFont'
    return 'Helvetica'


def tao_pdf_hoa_don(hoa_don, hop_dong, phong, khach):
    from io import BytesIO
    font = _font_tieng_viet()
    buf = BytesIO()
    pdf = canvas.Canvas(buf, pagesize=A4)
    pdf.setFont(font, 16)
    y = 280 * mm
    pdf.drawString(20 * mm, y, 'Phieu thu tien phong')
    pdf.setFont(font, 11)
    dong = [
        f"Hoa don thang: {hoa_don['thang_nam']}",
        f"Phong: {phong['tieu_de']}",
        f"Dia chi: {phong['dia_chi']}",
        f"Khach thue: {khach.get('ho_ten') or khach.get('email')}",
        f"Dien: {hoa_don['so_dien_cu']} -> {hoa_don['so_dien_moi']} (don gia {hoa_don['gia_dien']})",
        f"Nuoc: {hoa_don['so_nuoc_cu']} -> {hoa_don['so_nuoc_moi']} (don gia {hoa_don['gia_nuoc']})",
        f"Tien phong: {hoa_don['tien_phong']}",
        f"Tien dich vu: {hoa_don['tien_dich_vu']}",
        f"Tong tien: {hoa_don['tong_tien']}",
        f"Trang thai: {hoa_don['trang_thai']}",
    ]
    for line in dong:
        y -= 8 * mm
        pdf.drawString(20 * mm, y, str(line)[:110])
    pdf.showPage()
    pdf.save()
    return buf.getvalue()
