-- Banner trang chu va ma giam chi tru tien thue thang dau.

ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS ma_giam_id INTEGER;
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS giam_thang_dau DECIMAL(12,2) DEFAULT 0;
ALTER TABLE dat_phong DROP CONSTRAINT IF EXISTS dat_phong_ma_giam_id_fkey;

CREATE TABLE IF NOT EXISTS ma_giam (
    id SERIAL PRIMARY KEY,
    ma VARCHAR(40) UNIQUE NOT NULL,
    loai VARCHAR(20) NOT NULL,
    gia_tri DECIMAL(12,2) NOT NULL,
    tran_giam DECIMAL(12,2),
    het_han DATE,
    so_lan_toi_da INTEGER,
    so_lan_da_dung INTEGER DEFAULT 0,
    dang_dung BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE dat_phong ADD CONSTRAINT dat_phong_ma_giam_id_fkey
    FOREIGN KEY (ma_giam_id) REFERENCES ma_giam(id);

CREATE TABLE IF NOT EXISTS banner_trang_chu (
    id SERIAL PRIMARY KEY,
    tieu_de VARCHAR(255) NOT NULL,
    anh VARCHAR(500),
    lien_ket VARCHAR(500),
    thu_tu INTEGER DEFAULT 0,
    dang_hien BOOLEAN DEFAULT TRUE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
