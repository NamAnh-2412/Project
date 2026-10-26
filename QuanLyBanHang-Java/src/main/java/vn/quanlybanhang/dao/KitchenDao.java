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

public class KitchenDao extends BaseDao {

    public List<Map<String, Object>> kitchenTickets() throws SQLException {
        String sql = "SELECT d.id, d.ma_donhang, d.ten_khachhang, d.ngay_tao, d.trang_thai_bep, d.nguon_don, b.ten_ban "
                + "FROM donhang d LEFT JOIN ban_an b ON b.id = d.ban_id "
                + "WHERE d.trang_thai <> 'da_huy' AND d.trang_thai_bep IN ('cho_lam', 'dang_lam') "
                + "ORDER BY d.ngay_tao";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> tickets = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> ticket = new LinkedHashMap<>();
                int orderId = resultSet.getInt("id");
                ticket.put("id", orderId);
                ticket.put("maDonHang", resultSet.getString("ma_donhang"));
                ticket.put("tenKhachHang", resultSet.getString("ten_khachhang"));
                ticket.put("trangThaiBep", resultSet.getString("trang_thai_bep"));
                ticket.put("nguonDon", resultSet.getString("nguon_don"));
                ticket.put("tenBan", resultSet.getString("ten_ban"));
                Timestamp created = resultSet.getTimestamp("ngay_tao");
                long minutes = 0;
                if (created != null) {
                    minutes = Math.max(0, java.time.Duration.between(created.toLocalDateTime(), LocalDateTime.now()).toMinutes());
                }
                ticket.put("phut", minutes);
                ticket.put("items", kitchenItems(connection, orderId));
                tickets.add(ticket);
            }
            return tickets;
        }
    }

    public void setOrderKitchen(int orderId, String status) throws SQLException {
        String sql = "UPDATE donhang SET trang_thai_bep = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void setItemKitchen(int itemId, String status) throws SQLException {
        String sql = "UPDATE chitiet_donhang SET trang_thai_bep = ? WHERE id = ?";
        String orderSql = "SELECT donhang_id FROM chitiet_donhang WHERE id = ?";
        try (Connection connection = getConnection()) {
            int orderId;
            try (PreparedStatement statement = connection.prepareStatement(orderSql)) {
                statement.setInt(1, itemId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return;
                    }
                    orderId = resultSet.getInt("donhang_id");
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setInt(2, itemId);
                statement.executeUpdate();
            }
            String pending = "SELECT COUNT(*) FROM chitiet_donhang WHERE donhang_id = ? "
                    + "AND (trang_thai_bep IS NULL OR trang_thai_bep <> 'xong')";
            try (PreparedStatement statement = connection.prepareStatement(pending)) {
                statement.setInt(1, orderId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        try (PreparedStatement done = connection.prepareStatement(
                                "UPDATE donhang SET trang_thai_bep = 'xong' WHERE id = ?")) {
                            done.setInt(1, orderId);
                            done.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public void finishTicket(int orderId) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE chitiet_donhang SET trang_thai_bep = 'xong' WHERE donhang_id = ?")) {
                statement.setInt(1, orderId);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE donhang SET trang_thai_bep = 'xong' WHERE id = ?")) {
                statement.setInt(1, orderId);
                statement.executeUpdate();
            }
        }
    }

    private List<Map<String, Object>> kitchenItems(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT id, ten_sanpham, size, so_luong, trang_thai_bep FROM chitiet_donhang WHERE donhang_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> items = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    int itemId = resultSet.getInt("id");
                    item.put("id", itemId);
                    item.put("ten", resultSet.getString("ten_sanpham"));
                    item.put("size", resultSet.getString("size"));
                    item.put("soLuong", resultSet.getInt("so_luong"));
                    item.put("trangThaiBep", resultSet.getString("trang_thai_bep"));
                    item.put("choices", choiceText(connection, itemId));
                    items.add(item);
                }
                return items;
            }
        }
    }

    private String choiceText(Connection connection, int itemId) throws SQLException {
        String sql = "SELECT ten_nhom, ten_lua_chon FROM chitiet_donhang_lua_chon WHERE chitiet_donhang_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                StringBuilder text = new StringBuilder();
                while (resultSet.next()) {
                    if (!text.isEmpty()) {
                        text.append(" · ");
                    }
                    text.append(resultSet.getString("ten_nhom")).append(": ").append(resultSet.getString("ten_lua_chon"));
                }
                return text.toString();
            }
        }
    }

}
