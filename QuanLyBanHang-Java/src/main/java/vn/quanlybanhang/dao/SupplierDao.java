package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.Supplier;

public class SupplierDao extends BaseDao {

    public List<Supplier> search(String keyword, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM nhacungcap WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (ten_nhacungcap LIKE ? OR so_dien_thoai LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND trang_thai = ?");
            params.add(status);
        }

        sql.append(" ORDER BY ngay_tao DESC");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Supplier> suppliers = new ArrayList<>();
                while (resultSet.next()) {
                    suppliers.add(mapSupplier(resultSet));
                }
                return suppliers;
            }
        }
    }

    public Optional<Supplier> findById(int id) throws SQLException {
        String sql = "SELECT * FROM nhacungcap WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapSupplier(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public void insert(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO nhacungcap (ten_nhacungcap, so_dien_thoai, email, dia_chi, ma_so_thue, nguoi_lien_he, trang_thai, ghi_chu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillSupplier(statement, supplier);
            statement.executeUpdate();
        }
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = "UPDATE nhacungcap SET ten_nhacungcap = ?, so_dien_thoai = ?, email = ?, dia_chi = ?, ma_so_thue = ?, "
                + "nguoi_lien_he = ?, trang_thai = ?, ghi_chu = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillSupplier(statement, supplier);
            statement.setInt(9, supplier.getId());
            statement.executeUpdate();
        }
    }

    public boolean hasNhapKho(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhapkho WHERE nha_cung_cap_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE nhacungcap SET trang_thai = 'ngung_hop_tac' WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM nhacungcap WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Nguyen lieu gan voi NCC — bang voi PHP {@code nhacungcap/view.php}.
     */
    public List<Map<String, Object>> findMaterialsBySupplierId(int supplierId) throws SQLException {
        if (!columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, ma_nguyenlieu, ten_nguyenlieu, don_vi_tinh, gia_nhap, trang_thai "
                + "FROM nguyenvatlieu WHERE nha_cung_cap_id = ? ORDER BY ten_nguyenlieu";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapMaterialRows(resultSet);
            }
        }
    }

    public List<Map<String, Object>> findImportHistoryBySupplierId(int supplierId) throws SQLException {
        String sql = "SELECT nk.id, nk.ma_phieu_nhap, nk.ngay_nhap, nk.tong_tien_nhap, nk.trang_thai, nv.ten_nhanvien "
                + "FROM nhapkho nk LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id "
                + "WHERE nk.nha_cung_cap_id = ? ORDER BY nk.ngay_nhap DESC LIMIT 50";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", resultSet.getInt("id"));
                    row.put("ma_phieu_nhap", resultSet.getString("ma_phieu_nhap"));
                    row.put("ngay_nhap", resultSet.getTimestamp("ngay_nhap"));
                    row.put("tong_tien_nhap", resultSet.getBigDecimal("tong_tien_nhap"));
                    row.put("trang_thai", resultSet.getString("trang_thai"));
                    row.put("ten_nhanvien", resultSet.getString("ten_nhanvien"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public Map<String, Object> getSupplierViewStats(int supplierId) throws SQLException {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_materials", countMaterialsForSupplier(supplierId));
        stats.put("total_imports", countImportsForSupplier(supplierId));
        stats.put("total_value", sumApprovedImportValueForSupplier(supplierId));
        return stats;
    }

    private int countMaterialsForSupplier(int supplierId) throws SQLException {
        if (!columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM nguyenvatlieu WHERE nha_cung_cap_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private int countImportsForSupplier(int supplierId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhapkho WHERE nha_cung_cap_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private BigDecimal sumApprovedImportValueForSupplier(int supplierId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tong_tien_nhap), 0) AS total FROM nhapkho "
                + "WHERE nha_cung_cap_id = ? AND trang_thai = 'da_duyet'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("total");
                }
                return BigDecimal.ZERO;
            }
        }
    }

    private List<Map<String, Object>> mapMaterialRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", resultSet.getInt("id"));
            row.put("ma_nguyenlieu", resultSet.getString("ma_nguyenlieu"));
            row.put("ten_nguyenlieu", resultSet.getString("ten_nguyenlieu"));
            row.put("don_vi_tinh", resultSet.getString("don_vi_tinh"));
            row.put("gia_nhap", resultSet.getBigDecimal("gia_nhap"));
            row.put("trang_thai", resultSet.getString("trang_thai"));
            rows.add(row);
        }
        return rows;
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                + "AND table_name = ? AND column_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private void fillSupplier(PreparedStatement statement, Supplier supplier) throws SQLException {
        statement.setString(1, supplier.getTenNhaCungCap());
        statement.setString(2, supplier.getSoDienThoai());
        statement.setString(3, supplier.getEmail());
        statement.setString(4, supplier.getDiaChi());
        statement.setString(5, supplier.getMaSoThue());
        statement.setString(6, supplier.getNguoiLienHe());
        statement.setString(7, supplier.getTrangThai());
        statement.setString(8, supplier.getGhiChu());
    }

    private Supplier mapSupplier(ResultSet resultSet) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(resultSet.getInt("id"));
        supplier.setTenNhaCungCap(resultSet.getString("ten_nhacungcap"));
        supplier.setSoDienThoai(getSafeString(resultSet, "so_dien_thoai"));
        supplier.setEmail(getSafeString(resultSet, "email"));
        supplier.setDiaChi(getSafeString(resultSet, "dia_chi"));
        supplier.setMaSoThue(getSafeString(resultSet, "ma_so_thue"));
        supplier.setNguoiLienHe(getSafeString(resultSet, "nguoi_lien_he"));
        supplier.setTrangThai(getSafeString(resultSet, "trang_thai"));
        supplier.setGhiChu(getSafeString(resultSet, "ghi_chu"));
        Timestamp ngayTao = getTimestampSafely(resultSet, "ngay_tao");
        if (ngayTao != null) {
            supplier.setNgayTao(ngayTao.toLocalDateTime());
        }
        return supplier;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private String getSafeString(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private Timestamp getTimestampSafely(ResultSet resultSet, String column) {
        try {
            return resultSet.getTimestamp(column);
        } catch (SQLException ex) {
            return null;
        }
    }
}
