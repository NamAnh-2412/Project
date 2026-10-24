"""Ghi tin phòng của chủ nhà. Route lo kiểm tra form, hàm này lo câu SQL."""
from dich_vu.upload_service import them_anh_cho_phong, xoa_tep


def gan_tien_ich(cursor, phong_id, tien_ich_ids, xoa_cu=False):
    if xoa_cu:
        cursor.execute('DELETE FROM phong_tien_ich WHERE phong_id = %s', (phong_id,))
    for tid in tien_ich_ids:
        if str(tid).isdigit():
            cursor.execute(
                'INSERT INTO phong_tien_ich (phong_id, tien_ich_id) VALUES (%s, %s)',
                (phong_id, int(tid)),
            )


def tao_tin_phong(cursor, chu_id, tin, anh_files):
    cursor.execute(
        """INSERT INTO phong
           (tieu_de, mo_ta, dia_chi, quan_huyen, gia, dien_tich, trang_thai, nguoi_dung_id,
            loai_phong, trang_thai_duyet, tien_dien, tien_nuoc, tien_dich_vu, tien_coc, vi_do, kinh_do)
           VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,'cho_duyet',%s,%s,%s,%s,%s,%s) RETURNING id""",
        (
            tin['tieu_de'], tin['mo_ta'] or None, tin['dia_chi'], tin['quan_huyen'] or None,
            tin['gia'], tin['dien_tich'], tin['trang_thai'], chu_id, tin['loai_phong'],
            tin['tien_dien'], tin['tien_nuoc'], tin['tien_dich_vu'], tin['tien_coc'],
            tin['vi_do'], tin['kinh_do'],
        ),
    )
    phong_id = cursor.fetchone()['id']
    them_anh_cho_phong(cursor, phong_id, anh_files)
    gan_tien_ich(cursor, phong_id, tin['tien_ich_ids'])
    return phong_id


def cap_nhat_tin_phong(cursor, room_id, chu_id, tin, anh_files, xoa_anh, anh_chinh):
    cursor.execute(
        """UPDATE phong SET tieu_de=%s, mo_ta=%s, dia_chi=%s, quan_huyen=%s, gia=%s, dien_tich=%s,
           trang_thai=%s, loai_phong=%s, tien_dien=%s, tien_nuoc=%s, tien_dich_vu=%s, tien_coc=%s,
           vi_do=%s, kinh_do=%s, an_tin=%s
           WHERE id=%s AND nguoi_dung_id=%s""",
        (
            tin['tieu_de'], tin['mo_ta'] or None, tin['dia_chi'], tin['quan_huyen'] or None,
            tin['gia'], tin['dien_tich'], tin['trang_thai'], tin['loai_phong'],
            tin['tien_dien'], tin['tien_nuoc'], tin['tien_dich_vu'], tin['tien_coc'],
            tin['vi_do'], tin['kinh_do'], tin['an_tin'], room_id, chu_id,
        ),
    )
    gan_tien_ich(cursor, room_id, tin['tien_ich_ids'], xoa_cu=True)
    for anh_id in xoa_anh:
        if str(anh_id).isdigit():
            cursor.execute(
                'SELECT duong_dan_anh FROM anh_phong WHERE id = %s AND phong_id = %s',
                (int(anh_id), room_id),
            )
            row = cursor.fetchone()
            if row:
                cursor.execute('DELETE FROM anh_phong WHERE id = %s', (int(anh_id),))
                xoa_tep(row['duong_dan_anh'])
    them_anh_cho_phong(cursor, room_id, anh_files)
    if anh_chinh:
        cursor.execute('UPDATE anh_phong SET la_anh_chinh = 0 WHERE phong_id = %s', (room_id,))
        cursor.execute(
            'UPDATE anh_phong SET la_anh_chinh = 1 WHERE id = %s AND phong_id = %s',
            (anh_chinh, room_id),
        )


def danh_sach_tien_ich(cursor):
    cursor.execute('SELECT * FROM tien_ich ORDER BY ten')
    return cursor.fetchall()
