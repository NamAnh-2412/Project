-- So tien: coc va tien thue la hai dong. QR sua tren trang quan tri.

ALTER TABLE giao_dich_thanh_toan ADD COLUMN IF NOT EXISTS loai VARCHAR(20) DEFAULT 'coc';
UPDATE giao_dich_thanh_toan SET loai = 'coc' WHERE loai IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_giao_dich_loai
    ON giao_dich_thanh_toan (dat_phong_id, loai)
    WHERE trang_thai <> 'da_huy' AND dat_phong_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS cau_hinh_chuyen_khoan (
    id SERIAL PRIMARY KEY,
    ten_ngan_hang VARCHAR(120),
    bank_bin VARCHAR(20) DEFAULT '970422',
    so_tai_khoan VARCHAR(40) NOT NULL,
    chu_tai_khoan VARCHAR(120),
    huong_dan TEXT,
    dang_dung BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
