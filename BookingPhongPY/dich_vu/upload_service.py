"""Lưu ảnh phòng dạng WebP và xóa tệp khi xóa dữ liệu."""
import os
import time

from PIL import Image

from cau_hinh.cai_dat import BASE_DIR, UPLOAD_DIR, MAX_IMAGE_SIZE


def xoa_tep(duong_dan):
    if not duong_dan:
        return
    duong_dan = str(duong_dan).replace('\\', '/')
    if duong_dan.startswith('uploads/'):
        path = os.path.join(BASE_DIR, duong_dan.replace('/', os.sep))
    else:
        return
    if os.path.isfile(path):
        try:
            os.remove(path)
        except OSError:
            pass


def luu_anh_webp(file_storage, phong_id, chi_so):
    if not file_storage or not file_storage.filename:
        return None
    data = file_storage.read()
    if not data or len(data) > MAX_IMAGE_SIZE:
        return None
    file_storage.stream.seek(0)
    try:
        img = Image.open(file_storage.stream)
        img = img.convert('RGB')
        img.thumbnail((1920, 1920))
    except Exception:
        return None
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    ten = f'room_{phong_id}_{int(time.time())}_{chi_so}.webp'
    img.save(os.path.join(UPLOAD_DIR, ten), 'WEBP', quality=82)
    return f'uploads/{ten}'


def luu_anh_dai_dien(file_storage, user_id):
    if not file_storage or not file_storage.filename:
        return None
    data = file_storage.read()
    if not data or len(data) > MAX_IMAGE_SIZE:
        return None
    file_storage.stream.seek(0)
    try:
        img = Image.open(file_storage.stream)
        img = img.convert('RGB')
        img.thumbnail((800, 800))
    except Exception:
        return None
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    ten = f'avatar_{user_id}_{int(time.time())}.webp'
    img.save(os.path.join(UPLOAD_DIR, ten), 'WEBP', quality=82)
    return f'uploads/{ten}'


def them_anh_cho_phong(cursor, phong_id, files):
    cursor.execute("SELECT COUNT(*) AS c FROM anh_phong WHERE phong_id = %s", (phong_id,))
    da_co = cursor.fetchone()['c'] > 0
    luu = []
    for i, f in enumerate(files or []):
        path = luu_anh_webp(f, phong_id, i)
        if not path:
            continue
        la_chinh = 0 if da_co or i > 0 or luu else 1
        if la_chinh == 1:
            da_co = True
        cursor.execute(
            "INSERT INTO anh_phong (phong_id, duong_dan_anh, la_anh_chinh) VALUES (%s, %s, %s)",
            (phong_id, path, la_chinh),
        )
        luu.append(path)
    return luu


def xoa_phong_va_tep(cursor, phong_id):
    cursor.execute("SELECT duong_dan_anh FROM anh_phong WHERE phong_id = %s", (phong_id,))
    tep = [row['duong_dan_anh'] for row in cursor.fetchall()]
    cursor.execute("DELETE FROM phong WHERE id = %s", (phong_id,))
    for path in tep:
        xoa_tep(path)
