"""Gunicorn cho nhiều request cùng lúc. Lịch nhắc việc không chạy trong các worker này."""
import os

bind = os.environ.get('GUNICORN_BIND', '0.0.0.0:5000')
workers = max(int(os.environ.get('WEB_WORKERS', '3')), 1)
threads = max(int(os.environ.get('WEB_THREADS', '2')), 1)
timeout = 30
graceful_timeout = 20
max_requests = 800
max_requests_jitter = 80
accesslog = '-'
errorlog = '-'
capture_output = True
