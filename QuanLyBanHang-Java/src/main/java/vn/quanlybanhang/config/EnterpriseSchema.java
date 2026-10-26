package vn.quanlybanhang.config;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;

/**
 * Bảng cho gọi món tại bàn, màn bếp, màn hình khách, chi nhánh và hóa đơn điện tử.
 */
public final class EnterpriseSchema {

    private static final SecureRandom RANDOM = new SecureRandom();

    private EnterpriseSchema() {
    }

    public static void ensure(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS khu_vuc (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ten_khu_vuc VARCHAR(100) NOT NULL,
                        thu_tu INT DEFAULT 0
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS ban_an (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        khu_vuc_id INT NOT NULL,
                        ten_ban VARCHAR(50) NOT NULL,
                        so_cho_ngoi INT DEFAULT 4,
                        trang_thai VARCHAR(30) NOT NULL DEFAULT 'trong',
                        qr_token VARCHAR(64) NOT NULL,
                        UNIQUE KEY uk_ban_qr (qr_token),
                        CONSTRAINT fk_ban_khu_vuc FOREIGN KEY (khu_vuc_id) REFERENCES khu_vuc(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS phien_order_ban (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ban_id INT NOT NULL,
                        gio_mo DATETIME DEFAULT CURRENT_TIMESTAMP,
                        gio_dong DATETIME NULL,
                        tong_tien DECIMAL(15,2) DEFAULT 0,
                        trang_thai VARCHAR(30) NOT NULL DEFAULT 'dang_mo',
                        donhang_id INT NULL,
                        CONSTRAINT fk_phien_ban FOREIGN KEY (ban_id) REFERENCES ban_an(id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS cfd_hien_thi (
                        id INT PRIMARY KEY,
                        noi_dung MEDIUMTEXT NULL,
                        cap_nhat DATETIME NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS chi_nhanh (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ma_chi_nhanh VARCHAR(20) NOT NULL,
                        ten_chi_nhanh VARCHAR(150) NOT NULL,
                        dia_chi VARCHAR(255) NOT NULL,
                        so_dien_thoai VARCHAR(20) NOT NULL,
                        trang_thai VARCHAR(20) NOT NULL DEFAULT 'hoat_dong',
                        la_kho_tong TINYINT(1) NOT NULL DEFAULT 0,
                        ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_ma_chi_nhanh (ma_chi_nhanh)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS kho_chi_nhanh (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        chi_nhanh_id INT NOT NULL,
                        nguyenlieu_id INT NOT NULL,
                        so_luong_ton DECIMAL(12,4) DEFAULT 0,
                        ton_toi_thieu DECIMAL(12,4) DEFAULT 5,
                        UNIQUE KEY uk_kho_chinhanh (chi_nhanh_id, nguyenlieu_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS phieu_chuyen_kho (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ma_phieu VARCHAR(40) NOT NULL,
                        tu_chi_nhanh_id INT NOT NULL,
                        den_chi_nhanh_id INT NOT NULL,
                        nguoi_tao_id INT NOT NULL,
                        nguyenlieu_id INT NOT NULL,
                        so_luong DECIMAL(12,4) NOT NULL,
                        ngay_chuyen DATETIME DEFAULT CURRENT_TIMESTAMP,
                        trang_thai VARCHAR(30) NOT NULL DEFAULT 'da_nhan',
                        ghi_chu VARCHAR(255) NULL,
                        UNIQUE KEY uk_ma_phieu_chuyen (ma_phieu)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS hoa_don_dien_tu (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        donhang_id INT NOT NULL,
                        nha_cung_cap VARCHAR(40) NOT NULL,
                        trang_thai VARCHAR(30) NOT NULL,
                        ma_tra_cuu VARCHAR(80) NULL,
                        ghi_chu VARCHAR(255) NULL,
                        ngay_tao DATETIME NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            statement.execute("""
                    INSERT INTO chi_nhanh (ma_chi_nhanh, ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai, la_kho_tong)
                    SELECT 'CN01', 'Chi nhánh chính', '', '', 'hoat_dong', 1
                    FROM DUAL
                    WHERE NOT EXISTS (SELECT 1 FROM chi_nhanh)
                    """);
            statement.execute("""
                    INSERT INTO cfd_hien_thi (id, noi_dung, cap_nhat)
                    SELECT 1, '{}', NOW() FROM DUAL
                    WHERE NOT EXISTS (SELECT 1 FROM cfd_hien_thi WHERE id = 1)
                    """);
        }
        addColumn(connection, "donhang", "ban_id", "INT NULL");
        addColumn(connection, "donhang", "phien_ban_id", "INT NULL");
        addColumn(connection, "donhang", "nguon_don", "VARCHAR(30) NULL");
        addColumn(connection, "donhang", "chi_nhanh_id", "INT NULL");
        addColumn(connection, "donhang", "trang_thai_bep", "VARCHAR(20) NULL");
        addColumn(connection, "chitiet_donhang", "trang_thai_bep", "VARCHAR(20) NULL");
        ensureSetting(connection, "cfd_token", token());
    }

    private static void ensureSetting(Connection connection, String key, String value) throws SQLException {
        String sql = "INSERT INTO settings (`key`, value) SELECT ?, ? FROM DUAL "
                + "WHERE NOT EXISTS (SELECT 1 FROM settings WHERE `key` = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.setString(3, key);
            statement.executeUpdate();
        }
    }

    private static String token() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static void addColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return;
                }
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
