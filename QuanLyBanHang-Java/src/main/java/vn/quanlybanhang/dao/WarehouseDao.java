package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.NhapKho;
import vn.quanlybanhang.model.NhapKhoItem;
import vn.quanlybanhang.config.SchemaCache;

public class WarehouseDao extends BaseDao {

    public List<NhapKho> search(String status, String fromDate, String toDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT nk.*, ncc.ten_nhacungcap, nv.ten_nhanvien "
                        + "FROM nhapkho nk "
                        + "LEFT JOIN nhacungcap ncc ON nk.nha_cung_cap_id = ncc.id "
                        + "LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append("AND nk.trang_thai = ? ");
            params.add(status);
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append("AND DATE(nk.ngay_nhap) >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append("AND DATE(nk.ngay_nhap) <= ? ");
            params.add(Date.valueOf(toDate));
        }

        sql.append("ORDER BY nk.ngay_nhap DESC, nk.id DESC");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<NhapKho> receipts = new ArrayList<>();
                while (resultSet.next()) {
                    receipts.add(mapNhapKho(resultSet));
                }
                return receipts;
            }
        }
    }

    public Optional<NhapKho> findById(int id) throws SQLException {
        String sql = "SELECT nk.*, ncc.ten_nhacungcap, nv.ten_nhanvien "
                + "FROM nhapkho nk "
                + "LEFT JOIN nhacungcap ncc ON nk.nha_cung_cap_id = ncc.id "
                + "LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id "
                + "WHERE nk.id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    NhapKho receipt = mapNhapKho(resultSet);
                    receipt.setItems(findItemsByReceiptId(connection, id));
                    return Optional.of(receipt);
                }
                return Optional.empty();
            }
        }
    }

    public int insertReceipt(Connection connection, NhapKho receipt) throws SQLException {
        String sql = "INSERT INTO nhapkho (ma_phieu_nhap, ngay_nhap, nha_cung_cap_id, tong_tien_nhap, nhanvien_nhap, trang_thai, ghi_chu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, receipt.getMaPhieuNhap());
            if (receipt.getNgayNhap() != null) {
                statement.setDate(2, Date.valueOf(receipt.getNgayNhap().toLocalDate()));
            } else {
                statement.setDate(2, new Date(System.currentTimeMillis()));
            }
            if (receipt.getNhaCungCapId() != null && receipt.getNhaCungCapId() > 0) {
                statement.setInt(3, receipt.getNhaCungCapId());
            } else {
                statement.setObject(3, null);
            }
            statement.setBigDecimal(4, receipt.getTongTien());
            statement.setInt(5, receipt.getNhanVienNhap());
            statement.setString(6, receipt.getTrangThai());
            statement.setString(7, receipt.getGhiChu());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public int insertReceiptItem(Connection connection, int receiptId, NhapKhoItem item) throws SQLException {
        String sql = "INSERT INTO chitiet_nhapkho (nhapkho_id, nguyenlieu_id, so_luong_nhap, don_gia_nhap, thanh_tien, han_su_dung) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, receiptId);
            statement.setInt(2, item.getNguyenVatLieuId());
            statement.setBigDecimal(3, item.getSoLuongNhap());
            statement.setBigDecimal(4, item.getDonGia());
            statement.setBigDecimal(5, item.getThanhTien());
            if (item.getHanSuDung() != null) {
                statement.setDate(6, Date.valueOf(item.getHanSuDung()));
            } else {
                statement.setDate(6, null);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateStatus(Connection connection, int id, String status) throws SQLException {
        String sql = "UPDATE nhapkho SET trang_thai = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        return SchemaCache.tableExists(tableName);
    }

    /**
     * Tao bang kho_lot + expiry_alerts neu chua co (tuong duong migration PHP).
     */
    public void ensureExpiryTables() throws SQLException {
        String createKhoLot = "CREATE TABLE IF NOT EXISTS kho_lot ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "chitiet_nhapkho_id INT NULL, "
                + "nguyenlieu_id INT NOT NULL, "
                + "nhapkho_id INT NULL, "
                + "so_luong_nhap DECIMAL(10,2) DEFAULT 0, "
                + "so_luong_con_lai DECIMAL(10,2) DEFAULT 0, "
                + "don_gia_nhap DECIMAL(12,2) DEFAULT 0, "
                + "han_su_dung DATE NULL, "
                + "ngay_nhap DATE NULL, "
                + "trang_thai VARCHAR(20) DEFAULT 'con_hang', "
                + "ngay_huy DATE NULL, "
                + "ly_do_huy VARCHAR(255) NULL, "
                + "nhanvien_huy INT NULL)";
        String createExpiryAlerts = "CREATE TABLE IF NOT EXISTS expiry_alerts ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "lot_id INT NOT NULL, "
                + "nguyenlieu_id INT NOT NULL, "
                + "loai_canh_bao VARCHAR(20) NOT NULL, "
                + "ngay_canh_bao DATE NOT NULL, "
                + "da_xem TINYINT(1) DEFAULT 0)";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createKhoLot);
            statement.executeUpdate(createExpiryAlerts);
            if (!SchemaCache.tableExists("kho_lot") || !SchemaCache.tableExists("expiry_alerts")) {
                SchemaCache.refresh(connection);
            }
        }
    }

    public boolean columnExists(String tableName, String columnName) throws SQLException {
        return SchemaCache.columnExists(tableName, columnName);
    }

    public List<Map<String, Object>> getExpiryLots(String filter, String fromDate, String toDate) throws SQLException {
        if (!tableExists("kho_lot")) {
            return new ArrayList<>();
        }

        boolean hasQtyNhap = columnExists("kho_lot", "so_luong_nhap");
        boolean hasQtyBanDau = columnExists("kho_lot", "so_luong_ban_dau");
        boolean hasDonGiaNhap = columnExists("kho_lot", "don_gia_nhap");
        boolean hasNgayHuy = columnExists("kho_lot", "ngay_huy");
        boolean hasLyDoHuy = columnExists("kho_lot", "ly_do_huy");
        boolean hasNhanVienHuy = columnExists("kho_lot", "nhanvien_huy");

        String qtyOriginalColumn = hasQtyNhap ? "kl.so_luong_nhap"
                : (hasQtyBanDau ? "kl.so_luong_ban_dau" : "NULL");
        String donGiaNhapColumn = hasDonGiaNhap ? "kl.don_gia_nhap" : "NULL";
        String ngayHuyColumn = hasNgayHuy ? "kl.ngay_huy" : "NULL";
        String lyDoHuyColumn = hasLyDoHuy ? "kl.ly_do_huy" : "NULL";

        StringBuilder sql = new StringBuilder(
                "SELECT kl.id, kl.nguyenlieu_id, kl.nhapkho_id, kl.han_su_dung, kl.trang_thai, "
                        + qtyOriginalColumn + " AS so_luong_nhap, "
                        + "kl.so_luong_con_lai, "
                        + donGiaNhapColumn + " AS don_gia_nhap, "
                        + ngayHuyColumn + " AS ngay_huy, "
                        + lyDoHuyColumn + " AS ly_do_huy, "
                        + "nl.ten_nguyenlieu, nl.ma_nguyenlieu, nl.don_vi_tinh, "
                        + "DATEDIFF(kl.han_su_dung, CURDATE()) AS so_ngay_con_lai, "
                        + "nk.ma_phieu_nhap, nv.ten_nhanvien AS nhanvien_nhap "
                        + (hasNhanVienHuy ? ", nvh.ten_nhanvien AS nhanvien_huy " : ", NULL AS nhanvien_huy ")
                        + "FROM kho_lot kl "
                        + "INNER JOIN nguyenvatlieu nl ON kl.nguyenlieu_id = nl.id "
                        + "LEFT JOIN nhapkho nk ON kl.nhapkho_id = nk.id "
                        + "LEFT JOIN nhanvien nv ON nk.nhanvien_nhap = nv.id ");
        if (hasNhanVienHuy) {
            sql.append("LEFT JOIN nhanvien nvh ON kl.nhanvien_huy = nvh.id ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if ("sap_het_han".equals(filter)) {
            sql.append("AND kl.trang_thai != 'da_huy' AND kl.han_su_dung >= CURDATE() AND kl.han_su_dung <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) ");
        } else if ("het_han".equals(filter)) {
            sql.append("AND kl.trang_thai != 'da_huy' AND kl.han_su_dung < CURDATE() ");
        } else if ("da_huy".equals(filter)) {
            sql.append("AND kl.trang_thai = 'da_huy' ");
        } else {
            sql.append("AND kl.trang_thai != 'da_huy' ");
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append("AND kl.han_su_dung >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append("AND kl.han_su_dung <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY kl.han_su_dung ASC, nl.ten_nguyenlieu ASC LIMIT 500");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", resultSet.getInt("id"));
                    row.put("nguyenlieu_id", resultSet.getInt("nguyenlieu_id"));
                    row.put("nhapkho_id", resultSet.getObject("nhapkho_id"));
                    row.put("han_su_dung", resultSet.getDate("han_su_dung"));
                    row.put("trang_thai", resultSet.getString("trang_thai"));
                    row.put("so_luong_nhap", resultSet.getBigDecimal("so_luong_nhap"));
                    row.put("so_luong_con_lai", resultSet.getBigDecimal("so_luong_con_lai"));
                    row.put("don_gia_nhap", resultSet.getBigDecimal("don_gia_nhap"));
                    row.put("ngay_huy", resultSet.getTimestamp("ngay_huy"));
                    row.put("ly_do_huy", resultSet.getString("ly_do_huy"));
                    row.put("ten_nguyenlieu", resultSet.getString("ten_nguyenlieu"));
                    row.put("ma_nguyenlieu", resultSet.getString("ma_nguyenlieu"));
                    row.put("don_vi_tinh", resultSet.getString("don_vi_tinh"));
                    row.put("so_ngay_con_lai", resultSet.getInt("so_ngay_con_lai"));
                    row.put("ma_phieu_nhap", resultSet.getString("ma_phieu_nhap"));
                    row.put("nhanvien_nhap", resultSet.getString("nhanvien_nhap"));
                    row.put("nhanvien_huy", resultSet.getString("nhanvien_huy"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public Map<String, Integer> getExpiryStats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        if (!tableExists("kho_lot")) {
            stats.put("sap_het_han", 0);
            stats.put("het_han", 0);
            stats.put("da_huy", 0);
            return stats;
        }
        stats.put("sap_het_han", countExpiry("trang_thai != 'da_huy' AND han_su_dung >= CURDATE() AND han_su_dung <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)"));
        stats.put("het_han", countExpiry("trang_thai != 'da_huy' AND han_su_dung < CURDATE()"));
        stats.put("da_huy", countExpiry("trang_thai = 'da_huy'"));
        return stats;
    }

    public Optional<Map<String, Object>> findExpiryLotById(Connection connection, int lotId) throws SQLException {
        String sql = "SELECT id, nguyenlieu_id, so_luong_con_lai, trang_thai FROM kho_lot WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Map<String, Object> lot = new LinkedHashMap<>();
                    lot.put("id", resultSet.getInt("id"));
                    lot.put("nguyenlieu_id", resultSet.getInt("nguyenlieu_id"));
                    lot.put("so_luong_con_lai", resultSet.getBigDecimal("so_luong_con_lai"));
                    lot.put("trang_thai", resultSet.getString("trang_thai"));
                    return Optional.of(lot);
                }
                return Optional.empty();
            }
        }
    }

    public void markLotCancelled(Connection connection, int lotId, int employeeId, String reason) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE kho_lot SET trang_thai = 'da_huy'");
        if (columnExists("kho_lot", "ngay_huy")) {
            sql.append(", ngay_huy = NOW()");
        }
        if (columnExists("kho_lot", "nhanvien_huy")) {
            sql.append(", nhanvien_huy = ?");
        }
        if (columnExists("kho_lot", "ly_do_huy")) {
            sql.append(", ly_do_huy = ?");
        }
        sql.append(", so_luong_con_lai = 0 WHERE id = ?");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (columnExists("kho_lot", "nhanvien_huy")) {
                statement.setInt(index++, employeeId);
            }
            if (columnExists("kho_lot", "ly_do_huy")) {
                statement.setString(index++, reason);
            }
            statement.setInt(index, lotId);
            statement.executeUpdate();
        }
    }

    public void insertExpiryAlert(Connection connection, int lotId, int materialId, String type, LocalDate alertDate) throws SQLException {
        String sql = "INSERT INTO expiry_alerts (lot_id, nguyenlieu_id, loai_canh_bao, ngay_canh_bao, da_xem) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lotId);
            statement.setInt(2, materialId);
            statement.setString(3, type);
            statement.setDate(4, Date.valueOf(alertDate));
            statement.executeUpdate();
        }
    }

    public boolean expiryAlertExists(Connection connection, int lotId, String type, LocalDate alertDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expiry_alerts WHERE lot_id = ? AND loai_canh_bao = ? AND ngay_canh_bao = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lotId);
            statement.setString(2, type);
            statement.setDate(3, Date.valueOf(alertDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public List<Map<String, Object>> findLotsForAlert(Connection connection, String type, LocalDate today, LocalDate compareDate) throws SQLException {
        String condition;
        if ("7_ngay".equals(type)) {
            condition = "kl.han_su_dung >= ? AND kl.han_su_dung <= ?";
        } else if ("1_ngay".equals(type)) {
            condition = "kl.han_su_dung = ?";
        } else {
            condition = "kl.han_su_dung < ?";
        }
        String sql = "SELECT kl.id, kl.nguyenlieu_id FROM kho_lot kl WHERE kl.trang_thai != 'da_huy' AND " + condition;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if ("7_ngay".equals(type)) {
                statement.setDate(1, Date.valueOf(today));
                statement.setDate(2, Date.valueOf(compareDate));
            } else {
                statement.setDate(1, Date.valueOf(compareDate));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", resultSet.getInt("id"));
                    row.put("nguyenlieu_id", resultSet.getInt("nguyenlieu_id"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public void insertKhoLot(Connection connection, int receiptId, NhapKhoItem item) throws SQLException {
        String sql = "INSERT INTO kho_lot (chitiet_nhapkho_id, nguyenlieu_id, nhapkho_id, so_luong_nhap, so_luong_con_lai, don_gia_nhap, han_su_dung, ngay_nhap, trang_thai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'con_hang')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getId());
            statement.setInt(2, item.getNguyenVatLieuId());
            statement.setInt(3, receiptId);
            statement.setBigDecimal(4, item.getSoLuongNhap());
            statement.setBigDecimal(5, item.getSoLuongNhap());
            statement.setBigDecimal(6, item.getDonGia());
            if (item.getHanSuDung() != null) {
                statement.setDate(7, Date.valueOf(item.getHanSuDung()));
            } else {
                statement.setDate(7, null);
            }
            statement.setDate(8, new Date(System.currentTimeMillis()));
            statement.executeUpdate();
        }
    }

    private List<NhapKhoItem> findItemsByReceiptId(Connection connection, int receiptId) throws SQLException {
        String sql = "SELECT ctnk.*, nvl.ten_nguyenlieu, nvl.don_vi_tinh FROM chitiet_nhapkho ctnk "
                + "LEFT JOIN nguyenvatlieu nvl ON ctnk.nguyenlieu_id = nvl.id WHERE ctnk.nhapkho_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, receiptId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<NhapKhoItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    NhapKhoItem item = new NhapKhoItem();
                    item.setId(resultSet.getInt("id"));
                    item.setNguyenVatLieuId(resultSet.getInt("nguyenlieu_id"));
                    item.setTenNguyenVatLieu(resultSet.getString("ten_nguyenlieu"));
                    item.setSoLuongNhap(resultSet.getBigDecimal("so_luong_nhap"));
                    item.setDonViTinh(resultSet.getString("don_vi_tinh"));
                    item.setDonGia(resultSet.getBigDecimal("don_gia_nhap"));
                    item.setThanhTien(resultSet.getBigDecimal("thanh_tien"));
                    Date expiredDate = resultSet.getDate("han_su_dung");
                    if (expiredDate != null) {
                        item.setHanSuDung(expiredDate.toLocalDate());
                    }
                    items.add(item);
                }
                return items;
            }
        }
    }

    private NhapKho mapNhapKho(ResultSet resultSet) throws SQLException {
        NhapKho receipt = new NhapKho();
        receipt.setId(resultSet.getInt("id"));
        receipt.setMaPhieuNhap(resultSet.getString("ma_phieu_nhap"));
        receipt.setNhaCungCapId((Integer) resultSet.getObject("nha_cung_cap_id"));
        receipt.setTenNhaCungCap(resultSet.getString("ten_nhacungcap"));
        receipt.setNhanVienNhap(resultSet.getInt("nhanvien_nhap"));
        receipt.setTenNhanVienNhap(resultSet.getString("ten_nhanvien"));
        receipt.setTongTien(resultSet.getBigDecimal("tong_tien_nhap"));
        receipt.setTrangThai(resultSet.getString("trang_thai"));
        receipt.setGhiChu(resultSet.getString("ghi_chu"));
        Date ngayNhap = resultSet.getDate("ngay_nhap");
        if (ngayNhap != null) {
            receipt.setNgayNhap(ngayNhap.toLocalDate().atStartOfDay());
        }
        return receipt;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private int countExpiry(String whereClause) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kho_lot WHERE " + whereClause;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
