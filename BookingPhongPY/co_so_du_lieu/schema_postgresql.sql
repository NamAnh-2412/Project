-- PostgreSQL Schema - Chuyển đổi từ MySQL (bookingphong)
--
-- Cách chạy:
-- 1. Tạo database: psql -U postgres -c "CREATE DATABASE bookingphong;"
-- 2. Import schema: psql -U postgres -d bookingphong -f co_so_du_lieu/schema_postgresql.sql
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

-- Dữ liệu mẫu: admin
INSERT INTO admin (id, ten_dang_nhap, mat_khau, ho_ten, email, vai_tro, trang_thai, ngay_tao, lan_dang_nhap_cuoi) VALUES
(1, 'admin', '$2y$10$YQC2pAUfx7BjjGnQ.dAlweLj/8cUs6BHaRZ4fOrD8p3H7DJhjFqNm', 'Administrator', 'admin@booking.com', 'super_admin', 'active', '2025-12-04 13:22:31', '2025-12-31 16:07:11');

-- Dữ liệu mẫu: nguoi_dung
INSERT INTO nguoi_dung (id, email, mat_khau, ho_ten, so_dien_thoai, anh_dai_dien, vai_tro, ngay_tao) VALUES
(1, 'namanh@gmail.com', '$2y$10$MoVWSx67j3dIIhEKZeDTluDn6AeLWmA2XV2BNBgZJULJun0ko2X0q', 'nam anh', '0368304634', NULL, 'chu_nha', '2025-11-15 17:56:00'),
(2, 'thao@gmail.com', '$2y$10$dZMmlrZzj1/JcHPRShrRBedWVJDWkdZBW5CmJ1/k1s9.u/NTrQEG6', 'Nguyễn Phương Thảo', '0965743482', NULL, 'nguoi_thue', '2025-11-19 07:58:41');

-- Dữ liệu mẫu: tien_ich
INSERT INTO tien_ich (id, ten, icon) VALUES
(1, 'WiFi miễn phí', '📶'),
(2, 'Điều hòa', '❄️'),
(3, 'Máy nước nóng', '🔥'),
(4, 'Tủ lạnh', '🧊'),
(5, 'Máy giặt', '🌀'),
(6, 'Tủ quần áo', '👔'),
(7, 'Giường', '🛏️'),
(8, 'Bàn làm việc', '🪑'),
(9, 'Ban công', '🌳'),
(10, 'Nhà vệ sinh riêng', '🚿'),
(11, 'Bếp', '🍳'),
(12, 'Chỗ đậu xe', '🚗'),
(13, 'Thang máy', '🛗'),
(14, 'Bảo vệ 24/7', '🛡️'),
(15, 'Camera an ninh', '📹'),
(16, 'Gần chợ', '🛒'),
(17, 'Gần trường học', '🏫'),
(18, 'Gần bệnh viện', '🏥'),
(19, 'Gần bến xe', '🚌'),
(20, 'Cửa sổ', '🪟');

-- Dữ liệu mẫu: phong
INSERT INTO phong (id, tieu_de, mo_ta, gia, dia_chi, quan_huyen, dien_tich, vi_do, kinh_do, trang_thai, nguoi_dung_id, ngay_tao, ngay_cap_nhat) VALUES
(1, 'Phòng 1', 'xịn', 21000000.00, 'Mai Động, Hoàng Mai, Hà Nội', 'Hoàng Mai', 20.00, NULL, NULL, 'con_trong', 1, '2025-11-17 01:33:51', '2025-12-04 12:42:17'),
(2, 'Phòng 2', 'Đẹp, Xịn, Sang', 2100000.00, 'Số nhà 3, ngõ 139/49/14, Tam Trinh, Mai Động, Hoàng Mai, Hà Nội', 'Hoàng Mai', 35.00, NULL, NULL, 'da_thue', 1, '2025-11-19 08:05:34', '2025-11-19 12:00:11'),
(3, 'phòng 3', 'đẹp', 99999999.99, 'lĩnh nam', 'hoàng mai', 40.00, NULL, NULL, 'da_thue', 1, '2025-11-19 13:16:06', '2025-11-19 13:21:18'),
(4, 'Phòng 4', 'xịn, đẹp, sang', 99999999.99, 'hoàng mai, hà nội', 'Hoàng Mai', 45.00, NULL, NULL, 'con_trong', 1, '2025-11-25 07:22:19', '2025-11-30 17:34:43'),
(5, 'phòng 1', 'xịn, wifi 5g', 1500000.00, 'hồ hoàn kiếm', 'hoàn kiếm', 35.00, NULL, NULL, 'da_thue', 2, '2025-11-25 07:26:37', '2025-11-25 07:28:18'),
(6, 'phòng 5', 'xịn vc', 15000000.00, 'hoàng mai, hà nội', 'hoàng mai', 50.00, NULL, NULL, 'con_trong', 1, '2025-12-04 12:38:15', '2025-12-04 12:38:15');

-- Dữ liệu mẫu: anh_phong
INSERT INTO anh_phong (id, phong_id, duong_dan_anh, la_anh_chinh, ngay_tao) VALUES
(3, 2, 'uploads/room_2_1763539534_0.webp', 1, '2025-11-19 08:05:34'),
(4, 3, 'uploads/room_3_1763558166_0.jpg', 1, '2025-11-19 13:16:06'),
(5, 1, 'uploads/room_1_1763562815_0.jpg', 1, '2025-11-19 14:33:35'),
(6, 4, 'uploads/room_4_1764055339_0.webp', 1, '2025-11-25 07:22:19'),
(7, 5, 'uploads/room_5_1764055654_0.jpg', 1, '2025-11-25 07:27:34'),
(8, 6, 'uploads/room_6_1764851895_0.jpg', 1, '2025-12-04 12:38:15');

-- Dữ liệu mẫu: dat_phong
INSERT INTO dat_phong (id, phong_id, nguoi_dung_id, trang_thai, tin_nhan, so_dien_thoai_lien_he, ngay_tao) VALUES
(1, 1, 2, 'da_huy', NULL, '0965743482', '2025-11-19 07:59:27'),
(2, 2, 2, 'da_xac_nhan', NULL, '0965743482', '2025-11-19 11:58:16'),
(3, 3, 2, 'da_xac_nhan', NULL, '0965743482', '2025-11-19 13:20:56'),
(4, 4, 2, 'da_huy', NULL, '0965743482', '2025-11-25 07:22:41'),
(5, 5, 1, 'da_xac_nhan', NULL, '0368304634', '2025-11-25 07:27:56');

-- Dữ liệu mẫu: yeu_thich
INSERT INTO yeu_thich (nguoi_dung_id, phong_id, ngay_tao) VALUES
(1, 5, '2025-11-25 18:13:25');

-- Reset sequences
SELECT setval('admin_id_seq', (SELECT MAX(id) FROM admin));
SELECT setval('nguoi_dung_id_seq', (SELECT MAX(id) FROM nguoi_dung));
SELECT setval('phong_id_seq', (SELECT MAX(id) FROM phong));
SELECT setval('anh_phong_id_seq', (SELECT MAX(id) FROM anh_phong));
SELECT setval('dat_phong_id_seq', (SELECT MAX(id) FROM dat_phong));
SELECT setval('tien_ich_id_seq', (SELECT MAX(id) FROM tien_ich));
