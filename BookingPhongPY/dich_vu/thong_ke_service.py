"""Câu đếm cho dashboard. Route chỉ việc đưa số ra template."""


def _dem(cursor, sql, params=()):
    cursor.execute(sql, params)
    return cursor.fetchone()['total'] or 0


def thong_ke_admin(cursor):
    stats = {
        'total_rooms': _dem(cursor, 'SELECT COUNT(*) as total FROM phong'),
        'available_rooms': _dem(cursor, "SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'con_trong'"),
        'rented_rooms': _dem(cursor, "SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'da_thue'"),
        'total_bookings': _dem(cursor, 'SELECT COUNT(*) as total FROM dat_phong'),
        'pending_bookings': _dem(cursor, "SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'cho_xac_nhan'"),
        'confirmed_bookings': _dem(cursor, "SELECT COUNT(*) as total FROM dat_phong WHERE trang_thai = 'da_xac_nhan'"),
        'total_nguoi_thue': _dem(cursor, "SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'nguoi_thue'"),
        'total_chu_nha': _dem(cursor, "SELECT COUNT(*) as total FROM nguoi_dung WHERE vai_tro = 'chu_nha'"),
        'total_admin': _dem(cursor, "SELECT COUNT(*) as total FROM admin WHERE trang_thai = 'active'"),
        'cho_duyet': _dem(cursor, "SELECT COUNT(*) AS total FROM phong WHERE trang_thai_duyet = 'cho_duyet'"),
        'bao_cao_moi': _dem(cursor, "SELECT COUNT(*) AS total FROM bao_cao WHERE trang_thai = 'moi'"),
    }
    cursor.execute(
        """SELECT to_char(date_trunc('month', ngay_tao), 'YYYY-MM') AS thang, COUNT(*) AS so
           FROM dat_phong
           WHERE ngay_tao >= date_trunc('month', CURRENT_DATE) - INTERVAL '5 months'
           GROUP BY 1 ORDER BY 1"""
    )
    bieu_do = [{'thang': r['thang'], 'so': int(r['so'])} for r in cursor.fetchall()]
    return stats, bieu_do


def thong_ke_chu_nha(cursor, chu_id):
    stats = {
        'total_rooms': _dem(cursor, 'SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s', (chu_id,)),
        'available_rooms': _dem(
            cursor,
            "SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s AND trang_thai = 'con_trong'",
            (chu_id,),
        ),
        'rented_rooms': _dem(
            cursor,
            "SELECT COUNT(*) as total FROM phong WHERE nguoi_dung_id = %s AND trang_thai = 'da_thue'",
            (chu_id,),
        ),
    }
    cursor.execute(
        "SELECT COALESCE(SUM(gia), 0) AS dt FROM phong WHERE nguoi_dung_id = %s AND trang_thai = 'da_thue'",
        (chu_id,),
    )
    stats['doanh_thu'] = float(cursor.fetchone()['dt'] or 0)
    stats['lich_cho'] = _dem(
        cursor,
        """SELECT COUNT(*) AS total FROM lich_hen_xem_phong lh
           JOIN phong p ON p.id = lh.phong_id
           WHERE p.nguoi_dung_id = %s AND lh.trang_thai = 'cho_xac_nhan'""",
        (chu_id,),
    )
    return stats
