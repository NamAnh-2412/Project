package vn.quanlybanhang.dao;

import java.math.BigDecimal;
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

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.model.DonHang;
import vn.quanlybanhang.model.OrderChoice;
import vn.quanlybanhang.model.OrderItem;

public class OrderDao extends BaseDao {

    public List<DonHang> searchOrders(String keyword, String fromDate, String toDate, String paymentMethod, String status)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM donhang WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append("AND trang_thai = ? ");
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (ma_donhang LIKE ? OR ten_khachhang LIKE ? OR sdt_khachhang LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append("AND DATE(ngay_tao) >= ? ");
            params.add(LocalDate.parse(fromDate));
        }

        if (toDate != null && !toDate.isBlank()) {
            sql.append("AND DATE(ngay_tao) <= ? ");
            params.add(LocalDate.parse(toDate));
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append("AND phuong_thuc_thanhtoan = ? ");
            params.add(paymentMethod);
        }

        sql.append("ORDER BY ngay_tao DESC LIMIT 300");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DonHang> orders = new ArrayList<>();
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
                return orders;
            }
        }
    }

    public Optional<DonHang> findById(int id) throws SQLException {
        String sql = "SELECT * FROM donhang WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    DonHang order = mapOrder(resultSet);
                    List<OrderItem> items = findItemsByOrderId(connection, id);
                    attachChoices(connection, items);
                    order.setItems(items);
                    return Optional.of(order);
                }
                return Optional.empty();
            }
        }
    }

    public int insertOrder(Connection connection, DonHang order) throws SQLException {
        String sql = "INSERT INTO donhang (ma_donhang, khachhang_id, tong_tien, giam_gia, thanh_tien, phuong_thuc_thanhtoan, "
                + "hinh_thuc_nhan, trang_thai, sdt_khachhang, ten_khachhang, nhanvien_id, ma_giam) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, order.getMaDonHang());
            if (order.getKhachHangId() != null) {
                statement.setInt(2, order.getKhachHangId());
            } else {
                statement.setObject(2, null);
            }
            statement.setBigDecimal(3, order.getTongTien());
            statement.setBigDecimal(4, order.getGiamGia());
            statement.setBigDecimal(5, order.getThanhTien());
            statement.setString(6, order.getPhuongThucThanhToan());
            statement.setString(7, order.getHinhThucNhan());
            statement.setString(8, order.getTrangThai());
            statement.setString(9, order.getSdtKhachHang());
            statement.setString(10, order.getTenKhachHang());
            if (order.getNhanVienId() == null) {
                statement.setObject(11, null);
            } else {
                statement.setInt(11, order.getNhanVienId());
            }
            statement.setString(12, order.getMaGiam());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateKhachHangId(Connection connection, int orderId, int customerId) throws SQLException {
        String sql = "UPDATE donhang SET khachhang_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public int insertOrderItem(Connection connection, int orderId, OrderItem item) throws SQLException {
        String sql = "INSERT INTO chitiet_donhang (donhang_id, sanpham_id, ten_sanpham, size, so_luong, don_gia, thanh_tien) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, orderId);
            statement.setInt(2, item.getSanPhamId());
            statement.setString(3, item.getTenSanPham());
            statement.setString(4, item.getSize());
            statement.setInt(5, item.getSoLuong());
            statement.setBigDecimal(6, item.getDonGia());
            statement.setBigDecimal(7, item.getThanhTien());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void insertOrderChoice(Connection connection, int orderItemId, OrderChoice choice) throws SQLException {
        String sql = "INSERT INTO chitiet_donhang_lua_chon (chitiet_donhang_id, nhom_lua_chon_id, lua_chon_id, ten_nhom, ten_lua_chon, gia_tang_them) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            statement.setInt(2, choice.getNhomLuaChonId());
            statement.setInt(3, choice.getLuaChonId());
            statement.setString(4, choice.getTenNhom());
            statement.setString(5, choice.getTenLuaChon());
            statement.setBigDecimal(6, choice.getGiaTangThem());
            statement.executeUpdate();
        }
    }

    public void markForKitchen(Connection connection, int orderId) throws SQLException {
        String orderSql = "UPDATE donhang SET trang_thai_bep = 'cho_lam', "
                + "nguon_don = IFNULL(nguon_don, 'tai_quay') WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(orderSql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
        String itemSql = "UPDATE chitiet_donhang SET trang_thai_bep = 'cho_lam' "
                + "WHERE donhang_id = ? AND trang_thai_bep IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    public void stampStaffAndVoucher(Connection connection, int orderId, Integer staffId, String voucherCode)
            throws SQLException {
        String sql = "UPDATE donhang SET nhanvien_id = ?, ma_giam = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (staffId == null) {
                statement.setObject(1, null);
            } else {
                statement.setInt(1, staffId);
            }
            statement.setString(2, voucherCode);
            statement.setInt(3, orderId);
            statement.executeUpdate();
        }
    }

    public void cancelOrder(Connection connection, int id) throws SQLException {
        String sql = "UPDATE donhang SET trang_thai = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, AppConstants.ORDER_STATUS_CANCELLED);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void completePendingOrder(Connection connection, int id, String paymentMethod, String currentStatus)
            throws SQLException {
        String sql = "UPDATE donhang SET trang_thai = ?, phuong_thuc_thanhtoan = ? WHERE id = ? AND trang_thai = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, AppConstants.ORDER_STATUS_COMPLETED);
            statement.setString(2, paymentMethod);
            statement.setInt(3, id);
            statement.setString(4, currentStatus);
            statement.executeUpdate();
        }
    }

    public Optional<Integer> findCustomerIdByPhone(Connection connection, String phone) throws SQLException {
        String sql = "SELECT id FROM khachhang WHERE so_dien_thoai = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt(1));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Integer> findCustomerIdByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT id FROM khachhang WHERE ten_khachhang = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt(1));
                }
                return Optional.empty();
            }
        }
    }

    private List<OrderItem> findItemsByOrderId(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT * FROM chitiet_donhang WHERE donhang_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    OrderItem item = new OrderItem();
                    item.setChiTietId(resultSet.getInt("id"));
                    item.setSanPhamId(resultSet.getInt("sanpham_id"));
                    item.setTenSanPham(resultSet.getString("ten_sanpham"));
                    item.setSize(resultSet.getString("size"));
                    item.setSoLuong(resultSet.getInt("so_luong"));
                    item.setDonGia(resultSet.getBigDecimal("don_gia"));
                    item.setThanhTien(resultSet.getBigDecimal("thanh_tien"));
                    items.add(item);
                }
                return items;
            }
        }
    }

    private void attachChoices(Connection connection, List<OrderItem> items) throws SQLException {
        for (OrderItem item : items) {
            if (item.getChiTietId() != null && item.getChiTietId() > 0) {
                item.setLuaChonList(findChoicesByDetailId(connection, item.getChiTietId()));
            }
        }
    }

    private List<OrderChoice> findChoicesByDetailId(Connection connection, int chitietDonHangId) throws SQLException {
        String sql = "SELECT nhom_lua_chon_id, lua_chon_id, ten_nhom, ten_lua_chon, gia_tang_them "
                + "FROM chitiet_donhang_lua_chon WHERE chitiet_donhang_id = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chitietDonHangId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<OrderChoice> choices = new ArrayList<>();
                while (resultSet.next()) {
                    OrderChoice choice = new OrderChoice();
                    choice.setNhomLuaChonId(resultSet.getInt("nhom_lua_chon_id"));
                    choice.setLuaChonId(resultSet.getInt("lua_chon_id"));
                    choice.setTenNhom(resultSet.getString("ten_nhom"));
                    choice.setTenLuaChon(resultSet.getString("ten_lua_chon"));
                    choice.setGiaTangThem(resultSet.getBigDecimal("gia_tang_them"));
                    choices.add(choice);
                }
                return choices;
            }
        }
    }

    private DonHang mapOrder(ResultSet resultSet) throws SQLException {
        DonHang order = new DonHang();
        order.setId(resultSet.getInt("id"));
        order.setMaDonHang(resultSet.getString("ma_donhang"));
        order.setKhachHangId((Integer) resultSet.getObject("khachhang_id"));
        order.setTenKhachHang(resultSet.getString("ten_khachhang"));
        order.setSdtKhachHang(resultSet.getString("sdt_khachhang"));
        order.setTongTien(resultSet.getBigDecimal("tong_tien"));
        order.setGiamGia(resultSet.getBigDecimal("giam_gia"));
        order.setThanhTien(resultSet.getBigDecimal("thanh_tien"));
        order.setPhuongThucThanhToan(resultSet.getString("phuong_thuc_thanhtoan"));
        order.setHinhThucNhan(resultSet.getString("hinh_thuc_nhan"));
        order.setTrangThai(resultSet.getString("trang_thai"));
        Timestamp createdAt = resultSet.getTimestamp("ngay_tao");
        if (createdAt != null) {
            order.setNgayTao(createdAt.toLocalDateTime());
        }
        order.setNhanVienId(optionalInt(resultSet, "nhanvien_id"));
        order.setMaGiam(optionalString(resultSet, "ma_giam"));
        return order;
    }

    private String optionalString(ResultSet resultSet, String column) throws SQLException {
        var meta = resultSet.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return resultSet.getString(column);
            }
        }
        return null;
    }

    private Integer optionalInt(ResultSet resultSet, String column) throws SQLException {
        String value = optionalString(resultSet, column);
        if (value == null || value.isBlank() || resultSet.wasNull()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof LocalDate) {
                LocalDate localDate = (LocalDate) value;
                statement.setDate(i + 1, java.sql.Date.valueOf(localDate));
            } else if (value instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) value;
                statement.setBigDecimal(i + 1, bigDecimal);
            } else {
                statement.setObject(i + 1, value);
            }
        }
    }
}
