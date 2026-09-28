package vn.quanlybanhang.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.model.KhachHang;

public class KhachHangDao extends BaseDao {

    public List<KhachHang> findAll() throws SQLException {
        String sql = "SELECT * FROM khachhang ORDER BY ngay_tao DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<KhachHang> customers = new ArrayList<>();
            while (resultSet.next()) {
                customers.add(mapKhachHang(resultSet));
            }
            return customers;
        }
    }

    public Optional<KhachHang> findById(int id) throws SQLException {
        String sql = "SELECT * FROM khachhang WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapKhachHang(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<KhachHang> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM khachhang WHERE so_dien_thoai = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapKhachHang(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<KhachHang> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM khachhang WHERE ten_khachhang = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapKhachHang(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public int insert(KhachHang customer) throws SQLException {
        String sql = "INSERT INTO khachhang (ten_khachhang, so_dien_thoai, email, dia_chi, ngay_sinh, ghi_chu, tong_donhang, tong_tien_da_chi, loai_thanh_vien) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getTenKhachHang());
            statement.setString(2, customer.getSoDienThoai());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getDiaChi());
            if (customer.getNgaySinh() != null) {
                statement.setDate(5, Date.valueOf(customer.getNgaySinh()));
            } else {
                statement.setNull(5, java.sql.Types.DATE);
            }
            statement.setString(6, customer.getGhiChu());
            statement.setInt(7, customer.getTongDonHang());
            statement.setBigDecimal(8, customer.getTongTienDaChi() == null ? BigDecimal.ZERO : customer.getTongTienDaChi());
            statement.setString(9, customer.getLoaiThanhVien());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public int insert(Connection connection, KhachHang customer) throws SQLException {
        String sql = "INSERT INTO khachhang (ten_khachhang, so_dien_thoai, email, dia_chi, ngay_sinh, ghi_chu, tong_donhang, tong_tien_da_chi, loai_thanh_vien) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getTenKhachHang());
            statement.setString(2, customer.getSoDienThoai());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getDiaChi());
            if (customer.getNgaySinh() != null) {
                statement.setDate(5, Date.valueOf(customer.getNgaySinh()));
            } else {
                statement.setNull(5, java.sql.Types.DATE);
            }
            statement.setString(6, customer.getGhiChu());
            statement.setInt(7, customer.getTongDonHang());
            statement.setBigDecimal(8, customer.getTongTienDaChi() == null ? BigDecimal.ZERO : customer.getTongTienDaChi());
            statement.setString(9, customer.getLoaiThanhVien());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void update(KhachHang customer) throws SQLException {
        String sql = "UPDATE khachhang SET ten_khachhang = ?, so_dien_thoai = ?, email = ?, dia_chi = ?, ngay_sinh = ?, ghi_chu = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getTenKhachHang());
            statement.setString(2, customer.getSoDienThoai());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getDiaChi());
            if (customer.getNgaySinh() != null) {
                statement.setDate(5, Date.valueOf(customer.getNgaySinh()));
            } else {
                statement.setNull(5, java.sql.Types.DATE);
            }
            statement.setString(6, customer.getGhiChu());
            statement.setInt(7, customer.getId());
            statement.executeUpdate();
        }
    }

    public List<KhachHang> searchCustomers(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, ten_khachhang, so_dien_thoai, email, dia_chi, ngay_sinh, ghi_chu, loai_thanh_vien, "
                + "COALESCE(tong_tien_da_chi, 0) AS tong_tien_da_chi, COALESCE(tong_donhang, 0) AS tong_donhang, ngay_tao "
                + "FROM khachhang WHERE ten_khachhang LIKE ? OR so_dien_thoai LIKE ? "
                + "ORDER BY COALESCE(tong_tien_da_chi, 0) DESC, ten_khachhang ASC LIMIT 15";
        String searchTerm = "%" + keyword.trim() + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchTerm);
            statement.setString(2, searchTerm);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<KhachHang> customers = new ArrayList<>();
                while (resultSet.next()) {
                    customers.add(mapKhachHang(resultSet));
                }
                return customers;
            }
        }
    }

    public List<DonHang> findRecentOrdersByCustomerId(int customerId, int limit) throws SQLException {
        String sql = "SELECT id, ma_donhang, thanh_tien, trang_thai, ngay_tao, phuong_thuc_thanhtoan "
                + "FROM donhang WHERE khachhang_id = ? ORDER BY ngay_tao DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DonHang> orders = new ArrayList<>();
                while (resultSet.next()) {
                    DonHang order = new DonHang();
                    order.setId(resultSet.getInt("id"));
                    order.setMaDonHang(resultSet.getString("ma_donhang"));
                    order.setThanhTien(getBigDecimalSafely(resultSet, "thanh_tien"));
                    order.setTrangThai(getStringSafely(resultSet, "trang_thai"));
                    order.setPhuongThucThanhToan(getStringSafely(resultSet, "phuong_thuc_thanhtoan"));
                    Timestamp createdAt = getTimestampSafely(resultSet, "ngay_tao");
                    if (createdAt != null) {
                        order.setNgayTao(createdAt.toLocalDateTime());
                    }
                    orders.add(order);
                }
                return orders;
            }
        }
    }

    public CustomerStats getCustomerStats(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_orders, "
                + "COALESCE(SUM(CASE WHEN trang_thai NOT IN ('da_huy') THEN thanh_tien ELSE 0 END), 0) AS total_spent, "
                + "COALESCE(AVG(CASE WHEN trang_thai NOT IN ('da_huy') THEN thanh_tien END), 0) AS avg_order "
                + "FROM donhang WHERE khachhang_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CustomerStats stats = new CustomerStats();
                    stats.setTotalOrders(resultSet.getInt("total_orders"));
                    stats.setTotalSpent(getBigDecimalSafely(resultSet, "total_spent"));
                    stats.setAvgOrder(getBigDecimalSafely(resultSet, "avg_order"));
                    return stats;
                }
            }
        }
        return new CustomerStats();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM khachhang WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void incrementTongDonHang(int id) throws SQLException {
        String sql = "UPDATE khachhang SET tong_donhang = tong_donhang + 1 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void incrementTongDonHang(Connection connection, int id) throws SQLException {
        String sql = "UPDATE khachhang SET tong_donhang = tong_donhang + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void decrementTongDonHang(int id) throws SQLException {
        String sql = "UPDATE khachhang SET tong_donhang = GREATEST(tong_donhang - 1, 0) WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void decrementTongDonHang(Connection connection, int id) throws SQLException {
        String sql = "UPDATE khachhang SET tong_donhang = GREATEST(tong_donhang - 1, 0) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void incrementSpendingAndOrderCount(Connection connection, int id, BigDecimal amount) throws SQLException {
        String sql = "UPDATE khachhang "
                + "SET tong_donhang = tong_donhang + 1, "
                + "tong_tien_da_chi = COALESCE(tong_tien_da_chi, 0) + ? "
                + "WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount == null ? BigDecimal.ZERO : amount);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void reverseCustomerSpendingOnCancel(Connection connection, int id, BigDecimal amount) throws SQLException {
        String sql = "UPDATE khachhang "
                + "SET tong_donhang = GREATEST(tong_donhang - 1, 0), "
                + "tong_tien_da_chi = GREATEST(COALESCE(tong_tien_da_chi, 0) - ?, 0) "
                + "WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount == null ? BigDecimal.ZERO : amount);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void recalculateCustomerRank(Connection connection, int id) throws SQLException {
        String sql = "UPDATE khachhang SET loai_thanh_vien = CASE "
                + "WHEN COALESCE(tong_tien_da_chi, 0) >= 5000000 THEN 'vip' "
                + "WHEN COALESCE(tong_tien_da_chi, 0) >= 2000000 THEN 'vang' "
                + "WHEN COALESCE(tong_tien_da_chi, 0) >= 500000 THEN 'bac' "
                + "ELSE 'thuong' "
                + "END WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private KhachHang mapKhachHang(ResultSet resultSet) throws SQLException {
        KhachHang customer = new KhachHang();
        customer.setId(resultSet.getInt("id"));
        customer.setTenKhachHang(resultSet.getString("ten_khachhang"));
        customer.setSoDienThoai(getStringSafely(resultSet, "so_dien_thoai"));
        customer.setEmail(getStringSafely(resultSet, "email"));
        customer.setDiaChi(getStringSafely(resultSet, "dia_chi"));
        customer.setNgaySinh(getLocalDateSafely(resultSet, "ngay_sinh"));
        customer.setGhiChu(getStringSafely(resultSet, "ghi_chu"));
        customer.setLoaiThanhVien(getStringSafely(resultSet, "loai_thanh_vien"));
        customer.setTongDonHang(getIntSafely(resultSet, "tong_donhang"));
        customer.setTongTienDaChi(getBigDecimalSafely(resultSet, "tong_tien_da_chi"));
        Timestamp createdAt = getTimestampSafely(resultSet, "ngay_tao");
        if (createdAt != null) {
            customer.setNgayTao(createdAt.toLocalDateTime());
        }
        return customer;
    }

    private String getStringSafely(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private int getIntSafely(ResultSet resultSet, String column) {
        try {
            return resultSet.getInt(column);
        } catch (SQLException ex) {
            return 0;
        }
    }

    private BigDecimal getBigDecimalSafely(ResultSet resultSet, String column) {
        try {
            BigDecimal value = resultSet.getBigDecimal(column);
            return value == null ? BigDecimal.ZERO : value;
        } catch (SQLException ex) {
            return BigDecimal.ZERO;
        }
    }

    private Timestamp getTimestampSafely(ResultSet resultSet, String column) {
        try {
            return resultSet.getTimestamp(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private LocalDate getLocalDateSafely(ResultSet resultSet, String column) {
        try {
            Date value = resultSet.getDate(column);
            return value == null ? null : value.toLocalDate();
        } catch (SQLException ex) {
            return null;
        }
    }

    public static class CustomerStats {
        private int totalOrders;
        private BigDecimal totalSpent = BigDecimal.ZERO;
        private BigDecimal avgOrder = BigDecimal.ZERO;

        public int getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent == null ? BigDecimal.ZERO : totalSpent;
        }

        public BigDecimal getAvgOrder() {
            return avgOrder;
        }

        public void setAvgOrder(BigDecimal avgOrder) {
            this.avgOrder = avgOrder == null ? BigDecimal.ZERO : avgOrder;
        }
    }
}
