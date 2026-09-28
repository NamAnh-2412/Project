-- PostgreSQL Schema - Trống (chỉ cấu trúc bảng, không có dữ liệu)
-- Dùng để tự nhập dữ liệu test
--
-- Cách chạy:
-- 1. Tạo database: psql -U postgres -c "CREATE DATABASE bookingphong;"
-- 2. Import schema: psql -U postgres -d bookingphong -f co_so_du_lieu/schema_postgresql_trong.sql
--
-- Hoặc trong pgAdmin: mở file và chạy (đảm bảo đã tạo database bookingphong trước)

-- Xóa bảng cũ nếu có (theo thứ tự phụ thuộc)
DROP TABLE IF EXISTS yeu_thich CASCADE;
DROP TABLE IF EXISTS phong_tien_ich CASCADE;
DROP TABLE IF EXISTS dat_phong CASCADE;
DROP TABLE IF EXISTS anh_phong CASCADE;
DROP TABLE IF EXISTS phong CASCADE;
DROP TABLE IF EXISTS admin_log CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS nguoi_dung CASCADE;
DROP TABLE IF EXISTS tien_ich CASCADE;

-- Bảng admin
CREATE TABLE admin (
    id SERIAL PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(255) DEFAULT NULL,
    vai_tro VARCHAR(20) DEFAULT 'admin' CHECK (vai_tro IN ('super_admin', 'admin')),
    trang_thai VARCHAR(20) DEFAULT 'active' CHECK (trang_thai IN ('active', 'inactive')),
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lan_dang_nhap_cuoi TIMESTAMP DEFAULT NULL
);

-- Bảng admin_log
CREATE TABLE admin_log (
    id SERIAL PRIMARY KEY,
    admin_id INTEGER NOT NULL REFERENCES admin(id) ON DELETE CASCADE,
    hanh_dong VARCHAR(100) NOT NULL,
    chi_tiet TEXT DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng nguoi_dung
CREATE TABLE nguoi_dung (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(100) DEFAULT NULL,
    so_dien_thoai VARCHAR(20) DEFAULT NULL,
    anh_dai_dien VARCHAR(500) DEFAULT NULL,
    vai_tro VARCHAR(20) DEFAULT 'nguoi_thue' CHECK (vai_tro IN ('nguoi_thue', 'chu_nha')),
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng tien_ich
CREATE TABLE tien_ich (
    id SERIAL PRIMARY KEY,
    ten VARCHAR(100) NOT NULL,
    icon VARCHAR(100) DEFAULT NULL
);

-- Bảng phong
CREATE TABLE phong (
    id SERIAL PRIMARY KEY,
    tieu_de VARCHAR(255) NOT NULL,
    mo_ta TEXT DEFAULT NULL,
    gia DECIMAL(10,2) NOT NULL,
    dia_chi TEXT NOT NULL,
    quan_huyen VARCHAR(100) DEFAULT NULL,
    dien_tich DECIMAL(5,2) DEFAULT NULL,
    vi_do DECIMAL(10,8) DEFAULT NULL,
    kinh_do DECIMAL(11,8) DEFAULT NULL,
    trang_thai VARCHAR(20) DEFAULT 'con_trong' CHECK (trang_thai IN ('con_trong', 'da_thue')),
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trigger cập nhật ngay_cap_nhat
CREATE OR REPLACE FUNCTION update_ngay_cap_nhat()
RETURNS TRIGGER AS $$
BEGIN
    NEW.ngay_cap_nhat = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_phong_update
    BEFORE UPDATE ON phong
    FOR EACH ROW
    EXECUTE PROCEDURE update_ngay_cap_nhat();

-- Bảng anh_phong
CREATE TABLE anh_phong (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    duong_dan_anh VARCHAR(500) NOT NULL,
    la_anh_chinh SMALLINT DEFAULT 0,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng phong_tien_ich
CREATE TABLE phong_tien_ich (
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    tien_ich_id INTEGER NOT NULL REFERENCES tien_ich(id) ON DELETE CASCADE,
    PRIMARY KEY (phong_id, tien_ich_id)
);

-- Bảng dat_phong
CREATE TABLE dat_phong (
    id SERIAL PRIMARY KEY,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    trang_thai VARCHAR(20) DEFAULT 'cho_xac_nhan' CHECK (trang_thai IN ('cho_xac_nhan', 'da_xac_nhan', 'da_huy')),
    tin_nhan TEXT DEFAULT NULL,
    so_dien_thoai_lien_he VARCHAR(20) DEFAULT NULL,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng yeu_thich
CREATE TABLE yeu_thich (
    nguoi_dung_id INTEGER NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,
    phong_id INTEGER NOT NULL REFERENCES phong(id) ON DELETE CASCADE,
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nguoi_dung_id, phong_id)
);

-- ============================================
-- Thứ tự nhập dữ liệu (nếu tự thêm):
-- 1. admin
-- 2. nguoi_dung
-- 3. tien_ich
-- 4. phong (cần nguoi_dung_id)
-- 5. anh_phong (cần phong_id)
-- 6. phong_tien_ich (cần phong_id, tien_ich_id)
-- 7. dat_phong (cần phong_id, nguoi_dung_id)
-- 8. yeu_thich (cần nguoi_dung_id, phong_id)
--
-- Gợi ý: Chạy python kich_ban/tao_admin.py để tạo admin (admin/admin123)
--         Đăng ký người dùng qua web hoặc INSERT vào nguoi_dung
-- ============================================
