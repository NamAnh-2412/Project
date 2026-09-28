"""Cấu hình chung - tương đương config/config.php"""
import os

# Đường dẫn gốc
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# URL cơ sở (có thể override bằng biến môi trường)
BASE_URL = os.environ.get('BASE_URL', 'http://127.0.0.1:5000/')

# Thư mục upload
UPLOAD_DIR = os.path.join(BASE_DIR, 'uploads')
UPLOAD_URL = '/uploads/'

# Giới hạn upload
MAX_IMAGE_SIZE = 5 * 1024 * 1024  # 5MB
ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
