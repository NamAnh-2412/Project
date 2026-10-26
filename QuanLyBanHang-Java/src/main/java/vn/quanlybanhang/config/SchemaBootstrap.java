package vn.quanlybanhang.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaBootstrap {

    private SchemaBootstrap() {
    }

    public static void ensure() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS ca_lam_viec (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            nhanvien_id INT NOT NULL,
                            ten_nhanvien VARCHAR(150) NULL,
                            trang_thai VARCHAR(20) NOT NULL,
                            tien_dau_ca DECIMAL(15,2) NOT NULL DEFAULT 0,
                            tien_mat DECIMAL(15,2) NOT NULL DEFAULT 0,
                            tien_chuyen_khoan DECIMAL(15,2) NOT NULL DEFAULT 0,
                            tien_the DECIMAL(15,2) NOT NULL DEFAULT 0,
                            tien_khac DECIMAL(15,2) NOT NULL DEFAULT 0,
                            so_don INT NOT NULL DEFAULT 0,
                            tien_ky_vong DECIMAL(15,2) NOT NULL DEFAULT 0,
                            tien_thuc_te DECIMAL(15,2) NULL,
                            chenh_lech DECIMAL(15,2) NULL,
                            ghi_chu VARCHAR(500) NULL,
                            mo_luc DATETIME NOT NULL,
                            dong_luc DATETIME NULL
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS ma_giam_gia (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            ma VARCHAR(40) NOT NULL,
                            loai VARCHAR(20) NOT NULL,
                            gia_tri DECIMAL(15,2) NOT NULL,
                            don_toi_thieu DECIMAL(15,2) NOT NULL DEFAULT 0,
                            giam_toi_da DECIMAL(15,2) NULL,
                            so_lan_toi_da INT NULL,
                            so_lan_da_dung INT NOT NULL DEFAULT 0,
                            bat_dau DATE NULL,
                            ket_thuc DATE NULL,
                            trang_thai VARCHAR(20) NOT NULL DEFAULT 'hoat_dong',
                            ngay_tao DATETIME NOT NULL,
                            UNIQUE KEY uk_ma_giam_gia (ma)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);
            }
            addColumn(connection, "donhang", "nhanvien_id", "INT NULL");
            addColumn(connection, "donhang", "ma_giam", "VARCHAR(40) NULL");
            EnterpriseSchema.ensure(connection);
            SchemaCache.refresh(connection);
        }
    }

    private static void addColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}
