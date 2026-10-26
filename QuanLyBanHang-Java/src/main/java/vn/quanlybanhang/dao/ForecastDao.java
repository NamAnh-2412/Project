package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForecastDao extends BaseDao {

    public List<Map<String, Object>> salesByDay(int days) throws SQLException {
        String sql = "SELECT DATE(d.ngay_tao) AS ngay, ct.sanpham_id, ct.ten_sanpham, SUM(ct.so_luong) AS sl "
                + "FROM chitiet_donhang ct JOIN donhang d ON d.id = ct.donhang_id "
                + "WHERE d.trang_thai = 'hoan_thanh' AND d.ngay_tao >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY DATE(d.ngay_tao), ct.sanpham_id, ct.ten_sanpham";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ngay", resultSet.getDate("ngay").toLocalDate());
                    row.put("sanPhamId", resultSet.getInt("sanpham_id"));
                    row.put("ten", resultSet.getString("ten_sanpham"));
                    row.put("sl", resultSet.getBigDecimal("sl"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public List<Map<String, Object>> crossSell(int productId) throws SQLException {
        String sql = "SELECT b.sanpham_id, b.ten_sanpham, COUNT(*) AS so_lan "
                + "FROM chitiet_donhang a "
                + "JOIN chitiet_donhang b ON a.donhang_id = b.donhang_id AND a.sanpham_id <> b.sanpham_id "
                + "JOIN donhang d ON d.id = a.donhang_id "
                + "WHERE a.sanpham_id = ? AND d.trang_thai = 'hoan_thanh' "
                + "GROUP BY b.sanpham_id, b.ten_sanpham ORDER BY so_lan DESC LIMIT 3";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", resultSet.getInt("sanpham_id"));
                    row.put("ten", resultSet.getString("ten_sanpham"));
                    row.put("soLan", resultSet.getInt("so_lan"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public Map<String, Object> todaySummary() throws SQLException {
        String sql = "SELECT COALESCE(SUM(thanh_tien), 0) AS tong, COUNT(*) AS so_don, "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = 'tien_mat' THEN thanh_tien ELSE 0 END), 0) AS tien_mat, "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = 'chuyen_khoan' THEN thanh_tien ELSE 0 END), 0) AS chuyen_khoan "
                + "FROM donhang WHERE trang_thai = 'hoan_thanh' AND DATE(ngay_tao) = CURDATE()";
        String yesterday = "SELECT COALESCE(SUM(thanh_tien), 0) FROM donhang "
                + "WHERE trang_thai = 'hoan_thanh' AND DATE(ngay_tao) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
        try (Connection connection = getConnection()) {
            Map<String, Object> row = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    row.put("tong", resultSet.getBigDecimal("tong"));
                    row.put("soDon", resultSet.getInt("so_don"));
                    row.put("tienMat", resultSet.getBigDecimal("tien_mat"));
                    row.put("chuyenKhoan", resultSet.getBigDecimal("chuyen_khoan"));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(yesterday);
                 ResultSet resultSet = statement.executeQuery()) {
                row.put("homQua", resultSet.next() ? resultSet.getBigDecimal(1) : BigDecimal.ZERO);
            }
            return row;
        }
    }

    public List<Map<String, Object>> topProductsToday() throws SQLException {
        String sql = "SELECT ct.ten_sanpham, SUM(ct.so_luong) AS sl, SUM(ct.thanh_tien) AS tien "
                + "FROM chitiet_donhang ct JOIN donhang d ON d.id = ct.donhang_id "
                + "WHERE d.trang_thai = 'hoan_thanh' AND DATE(d.ngay_tao) = CURDATE() "
                + "GROUP BY ct.ten_sanpham ORDER BY sl DESC LIMIT 3";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ten", resultSet.getString("ten_sanpham"));
                row.put("sl", resultSet.getBigDecimal("sl"));
                row.put("tien", resultSet.getBigDecimal("tien"));
                rows.add(row);
            }
            return rows;
        }
    }

    public List<Map<String, Object>> lowStock() throws SQLException {
        String sql = "SELECT ten_nguyenlieu, so_luong_ton, so_luong_toi_thieu, don_vi_tinh FROM nguyenvatlieu "
                + "WHERE IFNULL(so_luong_ton, 0) <= IFNULL(so_luong_toi_thieu, 0) "
                + "ORDER BY so_luong_ton LIMIT 8";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ten", resultSet.getString("ten_nguyenlieu"));
                row.put("ton", resultSet.getBigDecimal("so_luong_ton"));
                row.put("toiThieu", resultSet.getBigDecimal("so_luong_toi_thieu"));
                row.put("donVi", resultSet.getString("don_vi_tinh"));
                rows.add(row);
            }
            return rows;
        }
    }

    public Optional<Integer> findProductIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM sanpham WHERE ten_sanpham = ? AND dang_hoat_dong = 1 LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt("id"));
                }
                return Optional.empty();
            }
        }
    }
}
