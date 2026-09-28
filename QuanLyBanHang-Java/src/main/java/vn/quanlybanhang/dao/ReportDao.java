package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.quanlybanhang.constants.AppConstants;

public class ReportDao extends BaseDao {

    public List<Map<String, Object>> getTongHop(String from, String to) throws SQLException {
        String sql = "SELECT DATE(ngay_tao) AS ngay, SUM(thanh_tien) AS tong_tien, COUNT(*) AS so_don "
                + "FROM donhang WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ? "
                + "GROUP BY DATE(ngay_tao) ORDER BY ngay DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from);
            statement.setString(2, to);
            statement.setString(3, AppConstants.ORDER_STATUS_COMPLETED);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ngay", resultSet.getDate("ngay"));
                    row.put("tong_tien", resultSet.getBigDecimal("tong_tien"));
                    row.put("so_don", resultSet.getInt("so_don"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public List<Map<String, Object>> getHourly(String from, String to) throws SQLException {
        String sql = "SELECT HOUR(ngay_tao) AS gio, SUM(thanh_tien) AS doanh_thu, COUNT(*) AS so_don "
                + "FROM donhang WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ? "
                + "GROUP BY HOUR(ngay_tao) ORDER BY gio ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from);
            statement.setString(2, to);
            statement.setString(3, AppConstants.ORDER_STATUS_COMPLETED);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("gio", resultSet.getInt("gio"));
                    row.put("doanh_thu", resultSet.getBigDecimal("doanh_thu"));
                    row.put("so_don", resultSet.getInt("so_don"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public List<Map<String, Object>> getPaymentDetails(String from, String to) throws SQLException {
        boolean hasAppOnline = hasPaymentMethod("app_online");
        String sql = "SELECT DATE(ngay_tao) AS ngay, "
                + "SUM(CASE WHEN phuong_thuc_thanhtoan = 'tien_mat' THEN thanh_tien ELSE 0 END) AS tien_mat, "
                + "SUM(CASE WHEN phuong_thuc_thanhtoan = 'chuyen_khoan' THEN thanh_tien ELSE 0 END) AS chuyen_khoan, "
                + "SUM(CASE WHEN phuong_thuc_thanhtoan = 'the' THEN thanh_tien ELSE 0 END) AS the_amount, "
                + (hasAppOnline
                ? "SUM(CASE WHEN phuong_thuc_thanhtoan = 'app_online' THEN thanh_tien ELSE 0 END) AS app_online, "
                : "0 AS app_online, ")
                + "SUM(thanh_tien) AS tong_tien, COUNT(*) AS so_don "
                + "FROM donhang WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ? "
                + "GROUP BY DATE(ngay_tao) ORDER BY ngay DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from);
            statement.setString(2, to);
            statement.setString(3, AppConstants.ORDER_STATUS_COMPLETED);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ngay", resultSet.getDate("ngay"));
                    row.put("tien_mat", resultSet.getBigDecimal("tien_mat"));
                    row.put("chuyen_khoan", resultSet.getBigDecimal("chuyen_khoan"));
                    row.put("the", resultSet.getBigDecimal("the_amount"));
                    row.put("app_online", resultSet.getBigDecimal("app_online"));
                    row.put("tong_tien", resultSet.getBigDecimal("tong_tien"));
                    row.put("so_don", resultSet.getInt("so_don"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public List<Map<String, Object>> getDailyRevenue(String from, String to) throws SQLException {
        if (!tableExists("daily_revenue")) {
            return new ArrayList<>();
        }
        boolean hasUpdatedAt = columnExists("daily_revenue", "updated_at");
        String sql = "SELECT day_date, total_amount, order_count"
                + (hasUpdatedAt ? ", updated_at " : ", NULL AS updated_at ")
                + "FROM daily_revenue WHERE day_date BETWEEN ? AND ? ORDER BY day_date DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from);
            statement.setString(2, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("day_date", resultSet.getDate("day_date"));
                    row.put("total_amount", resultSet.getBigDecimal("total_amount"));
                    row.put("order_count", resultSet.getInt("order_count"));
                    row.put("updated_at", resultSet.getTimestamp("updated_at"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public int rebuildDailyRevenue() throws SQLException {
        if (!tableExists("daily_revenue")) {
            throw new SQLException("Bang daily_revenue khong ton tai.");
        }
        boolean hasUpdatedAt = columnExists("daily_revenue", "updated_at");
        String deleteSql = "DELETE FROM daily_revenue";
        String insertSql = "INSERT INTO daily_revenue (day_date, total_amount, order_count"
                + (hasUpdatedAt ? ", updated_at" : "")
                + ") SELECT DATE(ngay_tao), COALESCE(SUM(thanh_tien), 0), COUNT(*)"
                + (hasUpdatedAt ? ", NOW()" : "")
                + " "
                + "FROM donhang WHERE trang_thai = ? GROUP BY DATE(ngay_tao)";
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement(deleteSql);
                 PreparedStatement insert = connection.prepareStatement(insertSql)) {
                delete.executeUpdate();
                insert.setString(1, AppConstants.ORDER_STATUS_COMPLETED);
                int inserted = insert.executeUpdate();
                connection.commit();
                return inserted;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean hasPaymentMethod(String method) throws SQLException {
        String sql = "SELECT COLUMN_TYPE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'donhang' AND COLUMN_NAME = 'phuong_thuc_thanhtoan'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String columnType = resultSet.getString("COLUMN_TYPE");
                return columnType != null && columnType.contains("'" + method + "'");
            }
            return false;
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}
