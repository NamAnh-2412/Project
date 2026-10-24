"""Điểm vào của Gunicorn. Lịch nhắc việc chạy ở kich_ban/chay_lich.py, không nằm trong worker web."""
from app import app

if __name__ == '__main__':
    app.run()
