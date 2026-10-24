"""Kết nối PostgreSQL dùng pool theo từng request."""
import os
from contextlib import contextmanager

import psycopg2
from psycopg2.extras import RealDictCursor
from psycopg2.pool import ThreadedConnectionPool

from cau_hinh.cai_dat import BASE_DIR
from dotenv import load_dotenv

load_dotenv(os.path.join(BASE_DIR, '.env'))

DB_CONFIG = {
    'host': os.environ.get('DB_HOST', 'localhost'),
    'port': int(os.environ.get('DB_PORT', 5432)),
    'user': os.environ.get('DB_USER', 'postgres'),
    'password': os.environ.get('DB_PASSWORD', ''),
    'dbname': os.environ.get('DB_NAME', 'bookingphong'),
    'options': '-c client_encoding=UTF8',
}

_pool = None


def _lay_pool():
    global _pool
    if _pool is None:
        try:
            toi_da = int(os.environ.get('DB_POOL_MAX', '8'))
        except ValueError:
            toi_da = 8
        toi_da = min(max(toi_da, 2), 32)
        _pool = ThreadedConnectionPool(1, toi_da, **DB_CONFIG)
    return _pool


class _KetNoiRequest:
    """Đóng giả: kết nối thật được trả về pool khi request kết thúc."""

    def __init__(self, raw):
        self._raw = raw

    def close(self):
        return None

    def __getattr__(self, name):
        return getattr(self._raw, name)


def get_db_connection():
    """Trong request thì mượn pool. Ngoài request (script) thì mở kết nối riêng."""
    try:
        from flask import g, has_request_context
    except ImportError:
        has_request_context = lambda: False
        g = None

    if has_request_context():
        proxy = getattr(g, '_db_proxy', None)
        if proxy is None:
            raw = _lay_pool().getconn()
            raw.autocommit = True
            proxy = _KetNoiRequest(raw)
            g._db_proxy = proxy
            g._db_raw = raw
        return proxy

    conn = psycopg2.connect(**DB_CONFIG)
    conn.autocommit = True
    return conn


def _raw_conn(conn):
    return getattr(conn, '_raw', conn)


@contextmanager
def giao_dich(conn):
    """Tắt autocommit để nhiều câu SQL cùng thành công hoặc cùng hủy."""
    raw = _raw_conn(conn)
    raw.autocommit = False
    try:
        yield
        raw.commit()
    except Exception:
        raw.rollback()
        raise
    finally:
        raw.autocommit = True


def giai_phong_ket_noi(exc=None):
    """Trả kết nối về pool. Gắn vào teardown_appcontext."""
    try:
        from flask import g
    except ImportError:
        return
    raw = g.pop('_db_raw', None)
    g.pop('_db_proxy', None)
    if raw is None:
        return
    try:
        if not getattr(raw, 'closed', 1):
            raw.rollback()
            raw.autocommit = True
    except Exception:
        pass
    pool = _lay_pool()
    try:
        if getattr(raw, 'closed', 1):
            pool.putconn(raw, close=True)
        else:
            pool.putconn(raw)
    except Exception:
        try:
            pool.putconn(raw, close=True)
        except Exception:
            pass


def get_cursor(conn):
    return conn.cursor(cursor_factory=RealDictCursor)
