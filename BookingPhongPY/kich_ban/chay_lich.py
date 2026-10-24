"""Một tiến trình lịch duy nhất: hủy đơn quá hạn và nhắc hóa đơn. Không phục vụ web."""
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from dich_vu.lich_trinh import khoi_dong_lich


def main():
    khoi_dong_lich()
    if os.environ.get('FLASK_ENV') == 'testing' or os.environ.get('TESTING') == '1':
        return
    while True:
        time.sleep(3600)


if __name__ == '__main__':
    main()
