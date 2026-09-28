package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.quanlybanhang.constants.AppConstants;

public class DashboardDao extends BaseDao {

    public Map<String, Object> getTodayStats() throws SQLException {
        String sql = "SELECT COUNT(*) AS total_orders, COALESCE(SUM(thanh_tien), 0) AS total_revenue "
                + "FROM donhang WHERE DATE(ngay_tao) = CURRENT_DATE() AND trang_thai = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, AppConstants.ORDER_STATUS_COMPLETED);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, Object> stats = new HashMap<>();
                if (resultSet.next()) {
                    stats.put("totalOrders", resultSet.getInt("total_orders"));
                    stats.put("totalRevenue", resultSet.getBigDecimal("total_revenue"));
                } else {
                    stats.put("totalOrders", 0);
                    stats.put("totalRevenue", BigDecimal.ZERO);
                }
                return stats;
            }
        }
    }

    public List<Map<String, Object>> getTopProductsToday(int limit) throws SQLException {
        String sql = "SELECT sp.ten_sanpham, SUM(ctd.so_luong) AS tong_sl "
                + "FROM donhang dh "
                + "JOIN chitiet_donhang ctd ON dh.id = ctd.donhang_id "
                + "JOIN sanpham sp ON ctd.sanpham_id = sp.id "
                + "WHERE DATE(dh.ngay_tao) = CURRENT_DATE() AND dh.trang_thai = ? "
                + "GROUP BY ctd.sanpham_id, sp.ten_sanpham "
                + "ORDER BY tong_sl DESC, sp.ten_sanpham ASC "
                + "LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, AppConstants.ORDER_STATUS_COMPLETED);
            statement.setInt(2, Math.max(limit, 1));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> products = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tenSanPham", resultSet.getString("ten_sanpham"));
                    row.put("tongSoLuong", resultSet.getInt("tong_sl"));
                    products.add(row);
                }
                return products;
            }
        }
    }

    /** So don hang hoan thanh theo ngay trong tuan hien tai (T2 -> CN). */
    public List<Integer> getWeeklyOrderCounts() throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Map<LocalDate, Integer> countsByDate = new HashMap<>();
        String sql = "SELECT DATE(ngay_tao) AS ngay, COUNT(*) AS tong "
                + "FROM donhang "
                + "WHERE DATE(ngay_tao) BETWEEN ? AND ? AND trang_thai = ? "
                + "GROUP BY DATE(ngay_tao)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, monday);
            statement.setObject(2, sunday);
            statement.setString(3, AppConstants.ORDER_STATUS_COMPLETED);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    countsByDate.put(resultSet.getObject("ngay", LocalDate.class), resultSet.getInt("tong"));
                }
            }
        }

        List<Integer> counts = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            counts.add(countsByDate.getOrDefault(monday.plusDays(i), 0));
        }
        return counts;
    }
}
