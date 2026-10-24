"""Chạy file SQL nâng cấp. Dùng: python kich_ban/migrate.py"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from cau_hinh.co_so_du_lieu import get_db_connection


def _tach_lenh(sql):
    lenh = []
    buf = []
    for line in sql.splitlines():
        if line.strip().startswith('--'):
            continue
        buf.append(line)
        if line.rstrip().endswith(';'):
            cau = '\n'.join(buf).strip()
            if cau:
                lenh.append(cau)
            buf = []
    return lenh


def _chay_tep(cur, path):
    with open(path, encoding='utf-8') as f:
        cac_lenh = _tach_lenh(f.read())
    for cau in cac_lenh:
        cur.execute(cau)
    print(f'{os.path.basename(path)}: {len(cac_lenh)} lenh')
    return len(cac_lenh)


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT to_regclass('public.nguoi_dung')")
    if cur.fetchone()[0] is None:
        nen = os.path.join(root, 'co_so_du_lieu', 'schema_postgresql_trong.sql')
        _chay_tep(cur, nen)
        conn.commit()
    thu_muc = os.path.join(root, 'co_so_du_lieu', 'migrations')
    tep = sorted(
        os.path.join(thu_muc, ten)
        for ten in os.listdir(thu_muc)
        if ten.endswith('.sql')
    )
    tong = 0
    for path in tep:
        tong += _chay_tep(cur, path)
    conn.commit()
    conn.close()
    print(f'Da chay {tong} lenh migration.')


if __name__ == '__main__':
    main()
