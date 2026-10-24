"""Gửi email qua Gmail SMTP. Mật khẩu là mật khẩu ứng dụng 16 ký tự."""
import os
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

from dotenv import load_dotenv

from cau_hinh.cai_dat import BASE_DIR

_loi_gui = ''


def _nap_env():
    load_dotenv(os.path.join(BASE_DIR, '.env'), override=True)


def _gia_tri(ten):
    return (os.environ.get(ten) or '').strip()


def mat_khau_smtp():
    return _gia_tri('SMTP_PASSWORD').replace(' ', '')


def dia_chi_gui():
    nguoi = _gia_tri('SMTP_FROM')
    if not nguoi or nguoi.endswith('@booking.local'):
        nguoi = _gia_tri('SMTP_USER')
    return nguoi


def smtp_da_cau_hinh():
    _nap_env()
    return bool(_gia_tri('SMTP_HOST') and _gia_tri('SMTP_USER') and mat_khau_smtp() and dia_chi_gui())


def loi_gui_gan_nhat():
    return _loi_gui


def gui_email(toi, tieu_de, html):
    global _loi_gui
    _loi_gui = ''
    _nap_env()
    if not toi or not smtp_da_cau_hinh():
        _loi_gui = 'Chưa điền Gmail và mật khẩu ứng dụng trong file .env.'
        return False
    try:
        msg = MIMEMultipart('alternative')
        msg['Subject'] = tieu_de
        msg['From'] = dia_chi_gui()
        msg['To'] = toi
        msg.attach(MIMEText(html, 'html', 'utf-8'))
        host = _gia_tri('SMTP_HOST')
        port = int(os.environ.get('SMTP_PORT') or 587)
        if port == 465:
            server = smtplib.SMTP_SSL(host, port, timeout=20)
        else:
            server = smtplib.SMTP(host, port, timeout=20)
            server.ehlo()
            server.starttls()
            server.ehlo()
        with server:
            server.login(_gia_tri('SMTP_USER'), mat_khau_smtp())
            server.sendmail(dia_chi_gui(), [toi], msg.as_string())
        return True
    except smtplib.SMTPAuthenticationError:
        _loi_gui = 'Gmail từ chối mật khẩu. Dùng mật khẩu ứng dụng 16 ký tự, không dùng mật khẩu đăng nhập.'
        return False
    except Exception:
        _loi_gui = 'Không gửi được thư tới Gmail. Kiểm tra mạng và SMTP_HOST=smtp.gmail.com.'
        return False
