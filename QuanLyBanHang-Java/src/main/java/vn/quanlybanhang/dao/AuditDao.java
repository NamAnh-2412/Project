package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ghi log audit tùy chọn (bảng có thể chưa tồn tại trên DB cũ).
 */
public class AuditDao extends BaseDao {

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

    public void logImportHistory(Connection connection, Integer employeeId, int materialId, BigDecimal quantity)
            throws SQLException {
        if (!tableExists("lich_su_nhap")) {
            return;
        }
        int qty = quantity == null ? 0 : quantity.intValue();
        String sql = "INSERT INTO lich_su_nhap (ngay, nhanvien_id, nguyenlieu_id, so_luong) VALUES (NOW(), ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (employeeId != null) {
                statement.setInt(1, employeeId);
            } else {
                statement.setObject(1, null);
            }
            statement.setInt(2, materialId);
            statement.setInt(3, qty);
            statement.executeUpdate();
        }
    }

    public void logDisposalHistory(Connection connection, int lotId, int materialId, BigDecimal quantity,
            int employeeId, String reason) throws SQLException {
        if (!tableExists("lich_su_huy")) {
            return;
        }
        String sql = "INSERT INTO lich_su_huy (lot_id, nguyenlieu_id, so_luong, nhanvien_id, ly_do, ngay_huy) "
                + "VALUES (?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lotId);
            statement.setInt(2, materialId);
            statement.setBigDecimal(3, quantity == null ? BigDecimal.ZERO : quantity);
            statement.setInt(4, employeeId);
            statement.setString(5, reason);
            statement.executeUpdate();
        }
    }

    public void logRoleChange(Connection connection, int employeeId, Integer changedBy, String oldRole,
            String newRole) throws SQLException {
        if (!tableExists("role_changes")) {
            return;
        }
        if (connection == null) {
            try (Connection ownConnection = getConnection()) {
                insertRoleChange(ownConnection, employeeId, changedBy, oldRole, newRole);
            }
            return;
        }
        insertRoleChange(connection, employeeId, changedBy, oldRole, newRole);
    }

    private void insertRoleChange(Connection connection, int employeeId, Integer changedBy, String oldRole,
            String newRole) throws SQLException {
        String sql = "INSERT INTO role_changes (nhanvien_id, changed_by, old_role, new_role, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            if (changedBy != null) {
                statement.setInt(2, changedBy);
            } else {
                statement.setObject(2, null);
            }
            statement.setString(3, oldRole);
            statement.setString(4, newRole);
            statement.setString(5, null);
            statement.executeUpdate();
        }
    }
}
