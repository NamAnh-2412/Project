-- Nang cap schema Booking Phong. Chay lai an toan (IF NOT EXISTS).

ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS cccd_so VARCHAR(20);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS cccd_mat_truoc VARCHAR(500);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS cccd_mat_sau VARCHAR(500);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(255);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS reset_password_expire TIMESTAMP;
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);
ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS bi_khoa BOOLEAN DEFAULT FALSE;

ALTER TABLE phong ADD COLUMN IF NOT EXISTS loai_phong VARCHAR(30) DEFAULT 'phong_tro';
ALTER TABLE phong ADD COLUMN IF NOT EXISTS trang_thai_duyet VARCHAR(20) DEFAULT 'da_duyet';
ALTER TABLE phong ADD COLUMN IF NOT EXISTS tien_dien DECIMAL(12,2) DEFAULT 0;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS tien_nuoc DECIMAL(12,2) DEFAULT 0;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS tien_dich_vu DECIMAL(12,2) DEFAULT 0;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS tien_coc DECIMAL(12,2) DEFAULT 0;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS is_vip BOOLEAN DEFAULT FALSE;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS han_vip TIMESTAMP;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS so_luot_xem INTEGER DEFAULT 0;
ALTER TABLE phong ADD COLUMN IF NOT EXISTS an_tin BOOLEAN DEFAULT FALSE;

ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS ngay_chuyen_vao DATE;
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS thoi_han_thue_thang INTEGER;
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS so_nguoi_o INTEGER;
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS tien_coc_phai_tra DECIMAL(12,2);
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS trang_thai_thanh_toan VARCHAR(30) DEFAULT 'chua_thanh_toan';
ALTER TABLE dat_phong ADD COLUMN IF NOT EXISTS ly_do_huy TEXT;

UPDATE phong SET trang_thai_duyet = 'da_duyet' WHERE trang_thai_duyet IS NULL;
UPDATE phong SET loai_phong = 'phong_tro' WHERE loai_phong IS NULL;
UPDATE dat_phong SET trang_thai_thanh_toan = 'chua_thanh_toan' WHERE trang_thai_thanh_toan IS NULL;

CREATE TABLE IF NOT EXISTS lich_hen_xem_phong (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    thoi_gian_hen TIMESTAMP NOT NULL,
    ghi_chu TEXT,
    trang_thai VARCHAR(20) DEFAULT 'cho_xac_nhan',
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS danh_gia (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    so_sao SMALLINT NOT NULL,
    noi_dung TEXT NOT NULL,
    phan_hoi_chu_nha TEXT,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS giao_dich_thanh_toan (
    id SERIAL PRIMARY KEY,
    ma_giao_dich VARCHAR(50) UNIQUE NOT NULL,
    dat_phong_id INTEGER REFERENCES dat_phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER REFERENCES nguoi_dung(id) ON DELETE SET NULL,
    so_tien DECIMAL(12,2) NOT NULL,
    phuong_thuc VARCHAR(30),
    trang_thai VARCHAR(20) DEFAULT 'dang_cho',
    thong_tin_nhan TEXT,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hop_dong_thue (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE,
    gia_thue_hang_thang DECIMAL(12,2) NOT NULL,
    tien_dat_coc DECIMAL(12,2) NOT NULL DEFAULT 0,
    trang_thai VARCHAR(20) DEFAULT 'dang_hieu_luc',
    tep_dinh_kem VARCHAR(500),
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoa_don_thang (
    id SERIAL PRIMARY KEY,
    hop_dong_id INTEGER NOT NULL REFERENCES hop_dong_thue(id) ON DELETE CASCADE,
    thang_nam VARCHAR(7) NOT NULL,
    so_dien_cu INTEGER DEFAULT 0,
    so_dien_moi INTEGER DEFAULT 0,
    gia_dien DECIMAL(12,2) DEFAULT 0,
    so_nuoc_cu INTEGER DEFAULT 0,
    so_nuoc_moi INTEGER DEFAULT 0,
    gia_nuoc DECIMAL(12,2) DEFAULT 0,
    tien_phong DECIMAL(12,2) DEFAULT 0,
    tien_dich_vu DECIMAL(12,2) DEFAULT 0,
    tong_tien DECIMAL(12,2) NOT NULL,
    trang_thai VARCHAR(20) DEFAULT 'chua_dong',
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS thong_bao (
    id SERIAL PRIMARY KEY,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    tieu_de VARCHAR(255) NOT NULL,
    noi_dung TEXT NOT NULL,
    duong_dan_lien_ket VARCHAR(255),
    da_doc BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tin_nhan (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_gui_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    nguoi_nhan_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    noi_dung TEXT NOT NULL,
    da_doc BOOLEAN DEFAULT FALSE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoi_dap (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    cau_hoi TEXT NOT NULL,
    cau_tra_loi TEXT,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bao_cao (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER REFERENCES nguoi_dung(id) ON DELETE SET NULL,
    loai VARCHAR(50) DEFAULT 'khac',
    noi_dung TEXT NOT NULL,
    trang_thai VARCHAR(20) DEFAULT 'moi',
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_danh_gia_phong_user ON danh_gia (phong_id, nguoi_dung_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_hoa_don_thang ON hoa_don_thang (hop_dong_id, thang_nam);
CREATE INDEX IF NOT EXISTS idx_phong_gia ON phong (gia);
CREATE INDEX IF NOT EXISTS idx_phong_quan_huyen ON phong (quan_huyen);
CREATE INDEX IF NOT EXISTS idx_phong_trang_thai ON phong (trang_thai);
CREATE INDEX IF NOT EXISTS idx_phong_duyet ON phong (trang_thai_duyet);
CREATE INDEX IF NOT EXISTS idx_phong_loai ON phong (loai_phong);
CREATE INDEX IF NOT EXISTS idx_dat_phong_nguoi ON dat_phong (nguoi_dung_id);
CREATE INDEX IF NOT EXISTS idx_thong_bao_user ON thong_bao (nguoi_dung_id, da_doc);
CREATE INDEX IF NOT EXISTS idx_tin_nhan_cap ON tin_nhan (nguoi_gui_id, nguoi_nhan_id, phong_id);

INSERT INTO tien_ich (ten, icon)
SELECT v.ten, v.icon
FROM (VALUES
    ('WiFi miễn phí', '📶'),
    ('Điều hòa', '❄️'),
    ('Máy nước nóng', '🔥'),
    ('Tủ lạnh', '🧊'),
    ('Máy giặt', '🌀'),
    ('Tủ quần áo', '👔'),
    ('Giường', '🛏️'),
    ('Bàn làm việc', '🪑'),
    ('Ban công', '🌳'),
    ('Nhà vệ sinh riêng', '🚿'),
    ('Bếp', '🍳'),
    ('Chỗ đậu xe', '🚗'),
    ('Thang máy', '🛗'),
    ('Bảo vệ 24/7', '🛡️'),
    ('Camera an ninh', '📹'),
    ('Gần chợ', '🛒'),
    ('Gần trường học', '🏫'),
    ('Gần bệnh viện', '🏥'),
    ('Gần bến xe', '🚌'),
    ('Cửa sổ', '🪟'),
    ('Cho nuôi thú cưng', '🐾'),
    ('Giờ giấc tự do', '🌙')
) AS v(ten, icon)
WHERE NOT EXISTS (SELECT 1 FROM tien_ich t WHERE t.ten = v.ten);
