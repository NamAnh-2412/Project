package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InvoiceDao extends BaseDao {

    public void insertInvoice(int orderId, String provider, String status, String note) throws SQLException {
        String sql = "INSERT INTO hoa_don_dien_tu (donhang_id, nha_cung_cap, trang_thai, ghi_chu, ngay_tao) "
                + "VALUES (?, ?, ?, ?, NOW())";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, provider);
            statement.setString(3, status);
            statement.setString(4, note);
            statement.executeUpdate();
        }
    }

    public Optional<Map<String, Object>> latestInvoice(int orderId) throws SQLException {
        String sql = "SELECT nha_cung_cap, trang_thai, ma_tra_cuu, ghi_chu, ngay_tao FROM hoa_don_dien_tu "
                + "WHERE donhang_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("nhaCungCap", resultSet.getString("nha_cung_cap"));
                row.put("trangThai", resultSet.getString("trang_thai"));
                row.put("maTraCuu", resultSet.getString("ma_tra_cuu"));
                row.put("ghiChu", resultSet.getString("ghi_chu"));
                row.put("ngayTao", resultSet.getTimestamp("ngay_tao"));
                return Optional.of(row);
            }
        }
    }
}
