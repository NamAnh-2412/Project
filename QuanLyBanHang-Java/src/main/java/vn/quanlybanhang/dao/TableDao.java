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

public class TableDao extends BaseDao {

    private static final SecureRandom RANDOM = new SecureRandom();

    public List<Map<String, Object>> listAreas() throws SQLException {
        String sql = "SELECT id, ten_khu_vuc, thu_tu FROM khu_vuc ORDER BY thu_tu, id";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("tenKhuVuc", resultSet.getString("ten_khu_vuc"));
                row.put("thuTu", resultSet.getInt("thu_tu"));
                rows.add(row);
            }
            return rows;
        }
    }

    public List<Map<String, Object>> listTables() throws SQLException {
        String sql = "SELECT b.id, b.khu_vuc_id, k.ten_khu_vuc, b.ten_ban, b.so_cho_ngoi, b.trang_thai, b.qr_token "
                + "FROM ban_an b JOIN khu_vuc k ON k.id = b.khu_vuc_id ORDER BY k.thu_tu, b.ten_ban";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                rows.add(mapTable(resultSet));
            }
            return rows;
        }
    }

    public int insertArea(String name) throws SQLException {
        String sql = "INSERT INTO khu_vuc (ten_khu_vuc, thu_tu) VALUES (?, 0)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public int insertTable(int areaId, String name, int seats) throws SQLException {
        String sql = "INSERT INTO ban_an (khu_vuc_id, ten_ban, so_cho_ngoi, trang_thai, qr_token) VALUES (?, ?, ?, 'trong', ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, areaId);
            statement.setString(2, name);
            statement.setInt(3, Math.max(seats, 1));
            statement.setString(4, token());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void refreshQr(int tableId) throws SQLException {
        String sql = "UPDATE ban_an SET qr_token = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token());
            statement.setInt(2, tableId);
            statement.executeUpdate();
        }
    }

    public Optional<Map<String, Object>> findTableByToken(String qrToken) throws SQLException {
        String sql = "SELECT b.id, b.khu_vuc_id, k.ten_khu_vuc, b.ten_ban, b.so_cho_ngoi, b.trang_thai, b.qr_token "
                + "FROM ban_an b JOIN khu_vuc k ON k.id = b.khu_vuc_id WHERE b.qr_token = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, qrToken);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTable(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public int openSession(int tableId) throws SQLException {
        String find = "SELECT id FROM phien_order_ban WHERE ban_id = ? AND trang_thai = 'dang_mo' ORDER BY id DESC LIMIT 1";
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(find)) {
                statement.setInt(1, tableId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }
            }
            String insert = "INSERT INTO phien_order_ban (ban_id, trang_thai) VALUES (?, 'dang_mo')";
            try (PreparedStatement statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, tableId);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        }
    }

    public void attachOrder(int orderId, int tableId, int sessionId, String source) throws SQLException {
        String sql = "UPDATE donhang SET ban_id = ?, phien_ban_id = ?, nguon_don = ? WHERE id = ?";
        String tableSql = "UPDATE ban_an SET trang_thai = 'dang_dung_bua' WHERE id = ?";
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, tableId);
                statement.setInt(2, sessionId);
                statement.setString(3, source);
                statement.setInt(4, orderId);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(tableSql)) {
                statement.setInt(1, tableId);
                statement.executeUpdate();
            }
        }
    }

    public void tagSource(int orderId, String source) throws SQLException {
        String sql = "UPDATE donhang SET nguon_don = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, source);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    private Map<String, Object> mapTable(ResultSet resultSet) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", resultSet.getInt("id"));
        row.put("khuVucId", resultSet.getInt("khu_vuc_id"));
        row.put("tenKhuVuc", resultSet.getString("ten_khu_vuc"));
        row.put("tenBan", resultSet.getString("ten_ban"));
        row.put("soCho", resultSet.getInt("so_cho_ngoi"));
        row.put("trangThai", resultSet.getString("trang_thai"));
        row.put("qrToken", resultSet.getString("qr_token"));
        return row;
    }

    private static String token() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

}
