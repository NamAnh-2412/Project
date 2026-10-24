"""Cấu hình chung. Giá trị nhạy cảm đọc từ file .env."""
import os
from dotenv import load_dotenv

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
load_dotenv(os.path.join(BASE_DIR, '.env'))

BASE_URL = os.environ.get('BASE_URL', 'http://127.0.0.1:5000/')
SECRET_KEY = os.environ.get('SECRET_KEY', 'dev-only-doi-trong-env')
FLASK_ENV = os.environ.get('FLASK_ENV', 'development')

UPLOAD_DIR = os.path.join(BASE_DIR, 'uploads')
UPLOAD_URL = '/uploads/'

MAX_IMAGE_SIZE = 5 * 1024 * 1024
ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']

GOOGLE_CLIENT_ID = os.environ.get('GOOGLE_CLIENT_ID', '')
GOOGLE_CLIENT_SECRET = os.environ.get('GOOGLE_CLIENT_SECRET', '')
WEBHOOK_SECRET = os.environ.get('WEBHOOK_SECRET', '')
