package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BranchDao extends BaseDao {

    public List<Map<String, Object>> listBranches() throws SQLException {
        String sql = "SELECT id, ma_chi_nhanh, ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai, la_kho_tong "
                + "FROM chi_nhanh ORDER BY id";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("ma", resultSet.getString("ma_chi_nhanh"));
                row.put("ten", resultSet.getString("ten_chi_nhanh"));
                row.put("diaChi", resultSet.getString("dia_chi"));
                row.put("sdt", resultSet.getString("so_dien_thoai"));
                row.put("trangThai", resultSet.getString("trang_thai"));
                row.put("khoTong", resultSet.getInt("la_kho_tong") == 1);
                rows.add(row);
            }
            return rows;
        }
    }

    public void insertBranch(String code, String name, String address, String phone) throws SQLException {
        String sql = "INSERT INTO chi_nhanh (ma_chi_nhanh, ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai, la_kho_tong) "
                + "VALUES (?, ?, ?, ?, 'hoat_dong', 0)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, address == null ? "" : address);
            statement.setString(4, phone == null ? "" : phone);
            statement.executeUpdate();
        }
    }

    public void transferStock(int fromId, int toId, int materialId, BigDecimal quantity, int staffId, String note)
            throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureStockRow(connection, fromId, materialId);
                ensureStockRow(connection, toId, materialId);
                String take = "UPDATE kho_chi_nhanh SET so_luong_ton = so_luong_ton - ? "
                        + "WHERE chi_nhanh_id = ? AND nguyenlieu_id = ? AND so_luong_ton >= ?";
                try (PreparedStatement statement = connection.prepareStatement(take)) {
                    statement.setBigDecimal(1, quantity);
                    statement.setInt(2, fromId);
                    statement.setInt(3, materialId);
                    statement.setBigDecimal(4, quantity);
                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("Kho nguồn không đủ số lượng.");
                    }
                }
                String give = "UPDATE kho_chi_nhanh SET so_luong_ton = so_luong_ton + ? WHERE chi_nhanh_id = ? AND nguyenlieu_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(give)) {
                    statement.setBigDecimal(1, quantity);
                    statement.setInt(2, toId);
                    statement.setInt(3, materialId);
                    statement.executeUpdate();
                }
                String slip = "INSERT INTO phieu_chuyen_kho (ma_phieu, tu_chi_nhanh_id, den_chi_nhanh_id, nguoi_tao_id, "
                        + "nguyenlieu_id, so_luong, trang_thai, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, 'da_nhan', ?)";
                try (PreparedStatement statement = connection.prepareStatement(slip)) {
                    statement.setString(1, "CK" + System.currentTimeMillis());
                    statement.setInt(2, fromId);
                    statement.setInt(3, toId);
                    statement.setInt(4, staffId);
                    statement.setInt(5, materialId);
                    statement.setBigDecimal(6, quantity);
                    statement.setString(7, note == null ? "" : note);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void seedBranchStockFromShop() throws SQLException {
        String sql = "INSERT INTO kho_chi_nhanh (chi_nhanh_id, nguyenlieu_id, so_luong_ton, ton_toi_thieu) "
                + "SELECT c.id, n.id, IFNULL(n.so_luong_ton, 0), IFNULL(n.so_luong_toi_thieu, 0) "
                + "FROM chi_nhanh c CROSS JOIN nguyenvatlieu n "
                + "LEFT JOIN kho_chi_nhanh k ON k.chi_nhanh_id = c.id AND k.nguyenlieu_id = n.id "
                + "WHERE k.id IS NULL";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    public List<Map<String, Object>> branchStock() throws SQLException {
        String sql = "SELECT c.ten_chi_nhanh, n.ten_nguyenlieu, k.so_luong_ton, k.ton_toi_thieu, n.don_vi_tinh, "
                + "k.chi_nhanh_id, k.nguyenlieu_id "
                + "FROM kho_chi_nhanh k "
                + "JOIN chi_nhanh c ON c.id = k.chi_nhanh_id "
                + "JOIN nguyenvatlieu n ON n.id = k.nguyenlieu_id "
                + "ORDER BY c.id, n.ten_nguyenlieu";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("chiNhanh", resultSet.getString("ten_chi_nhanh"));
                row.put("chiNhanhId", resultSet.getInt("chi_nhanh_id"));
                row.put("nguyenLieu", resultSet.getString("ten_nguyenlieu"));
                row.put("nguyenLieuId", resultSet.getInt("nguyenlieu_id"));
                row.put("ton", resultSet.getBigDecimal("so_luong_ton"));
                row.put("toiThieu", resultSet.getBigDecimal("ton_toi_thieu"));
                row.put("donVi", resultSet.getString("don_vi_tinh"));
                rows.add(row);
            }
            return rows;
        }
    }

    private void ensureStockRow(Connection connection, int branchId, int materialId) throws SQLException {
        String sql = "INSERT INTO kho_chi_nhanh (chi_nhanh_id, nguyenlieu_id, so_luong_ton, ton_toi_thieu) "
                + "SELECT ?, id, IFNULL(so_luong_ton, 0), IFNULL(so_luong_toi_thieu, 0) FROM nguyenvatlieu WHERE id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM kho_chi_nhanh WHERE chi_nhanh_id = ? AND nguyenlieu_id = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, materialId);
            statement.setInt(3, branchId);
            statement.setInt(4, materialId);
            statement.executeUpdate();
        }
    }

}
