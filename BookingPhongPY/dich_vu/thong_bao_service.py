"""Thông báo trong ứng dụng."""


def tao_thong_bao(cursor, nguoi_dung_id, tieu_de, noi_dung, duong_dan=None):
    if not nguoi_dung_id:
        return False
    try:
        cursor.execute(
            """INSERT INTO thong_bao (nguoi_dung_id, tieu_de, noi_dung, duong_dan_lien_ket)
               VALUES (%s, %s, %s, %s)""",
            (nguoi_dung_id, tieu_de[:255], noi_dung, duong_dan),
        )
        return True
    except Exception:
        return False


def dem_chua_doc(cursor, nguoi_dung_id):
    cursor.execute(
        "SELECT COUNT(*) AS c FROM thong_bao WHERE nguoi_dung_id = %s AND da_doc = FALSE",
        (nguoi_dung_id,),
    )
    return int(cursor.fetchone()['c'] or 0)
