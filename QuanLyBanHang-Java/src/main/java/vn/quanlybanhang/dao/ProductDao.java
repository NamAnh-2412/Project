package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;
import vn.quanlybanhang.model.Product;
import vn.quanlybanhang.model.ProductSize;

public class ProductDao extends BaseDao {

    public List<Product> search(String keyword, String categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT sp.*, dm.ten_danhmuc FROM sanpham sp LEFT JOIN danhmuc dm ON sp.danhmuc_id = dm.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (sp.ten_sanpham LIKE ? OR sp.ma_sanpham LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            sql.append(" AND sp.danhmuc_id = ?");
            params.add(Integer.parseInt(categoryId));
        }
        sql.append(" ORDER BY sp.ngay_tao DESC, sp.id DESC");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
                return products;
            }
        }
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT sp.*, dm.ten_danhmuc FROM sanpham sp LEFT JOIN danhmuc dm ON sp.danhmuc_id = dm.id WHERE sp.id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProduct(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Product> findActiveByCategory(Integer categoryId) throws SQLException {
        String sql = "SELECT sp.*, dm.ten_danhmuc FROM sanpham sp LEFT JOIN danhmuc dm ON sp.danhmuc_id = dm.id "
                + "WHERE sp.dang_hoat_dong = 1 "
                + (categoryId != null ? "AND sp.danhmuc_id = ? " : "")
                + "ORDER BY sp.ten_sanpham";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (categoryId != null) {
                statement.setInt(1, categoryId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
                return products;
            }
        }
    }

    public List<ProductSize> findSizesByProductId(int productId) throws SQLException {
        String sql = "SELECT * FROM kichthuoc_sanpham WHERE sanpham_id = ? ORDER BY gia ASC, id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductSize> sizes = new ArrayList<>();
                while (resultSet.next()) {
                    sizes.add(mapSize(resultSet));
                }
                return sizes;
            }
        }
    }

    public List<ProductSize> findActiveSizesByProductId(int productId) throws SQLException {
        String sql = "SELECT * FROM kichthuoc_sanpham WHERE sanpham_id = ? AND dang_hoat_dong = 1 ORDER BY gia ASC, id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductSize> sizes = new ArrayList<>();
                while (resultSet.next()) {
                    sizes.add(mapSize(resultSet));
                }
                return sizes;
            }
        }
    }

    public Optional<ProductSize> findSizeById(int sizeId, int productId) throws SQLException {
        String sql = "SELECT * FROM kichthuoc_sanpham WHERE id = ? AND sanpham_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sizeId);
            statement.setInt(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapSize(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<NhomLuaChon> findChoiceGroupsByProductId(int productId) throws SQLException {
        String sql = "SELECT nlc.id AS nlc_id, nlc.ten_nhom, nlc.mota, nlc.thu_tu, nlc.dang_hoat_dong, "
                + "splc.bat_buoc, lc.id AS lc_id, lc.ten_lua_chon, lc.gia_tang_them, lc.thu_tu AS lc_thu_tu, lc.dang_hoat_dong AS lc_dang_hoat_dong "
                + "FROM sanpham_nhom_lua_chon splc "
                + "JOIN nhom_lua_chon nlc ON splc.nhom_lua_chon_id = nlc.id "
                + "LEFT JOIN lua_chon lc ON lc.nhom_lua_chon_id = nlc.id "
                + "WHERE splc.sanpham_id = ? AND nlc.dang_hoat_dong = 1 "
                + "ORDER BY nlc.thu_tu ASC, lc.thu_tu ASC, lc.id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Integer, NhomLuaChon> grouped = new HashMap<>();
                while (resultSet.next()) {
                    int groupId = resultSet.getInt("nlc_id");
                    NhomLuaChon group = grouped.computeIfAbsent(groupId, ignored -> {
                        NhomLuaChon item = new NhomLuaChon();
                        item.setId(groupId);
                        try {
                            item.setTenNhom(resultSet.getString("ten_nhom"));
                            item.setMoTa(resultSet.getString("mota"));
                            item.setThuTu(resultSet.getInt("thu_tu"));
                            item.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
                            item.setBatBuoc(resultSet.getBoolean("bat_buoc"));
                        } catch (SQLException ex) {
                            throw new IllegalStateException(ex);
                        }
                        return item;
                    });

                    int choiceId = resultSet.getInt("lc_id");
                    if (!resultSet.wasNull() && resultSet.getBoolean("lc_dang_hoat_dong")) {
                        LuaChon choice = new LuaChon();
                        choice.setId(choiceId);
                        choice.setNhomLuaChonId(groupId);
                        choice.setTenLuaChon(resultSet.getString("ten_lua_chon"));
                        choice.setGiaTangThem(resultSet.getBigDecimal("gia_tang_them"));
                        choice.setThuTu(resultSet.getInt("lc_thu_tu"));
                        choice.setDangHoatDong(resultSet.getBoolean("lc_dang_hoat_dong"));
                        group.getLuaChonList().add(choice);
                    }
                }
                return new ArrayList<>(grouped.values());
            }
        }
    }

    public boolean productExistsInOrders(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM chitiet_donhang WHERE sanpham_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public int insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO sanpham (ma_sanpham, ten_sanpham, danhmuc_id, gia_ban, dang_hoat_dong, anh_url, mota) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillProduct(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE sanpham SET ma_sanpham = ?, ten_sanpham = ?, danhmuc_id = ?, gia_ban = ?, dang_hoat_dong = ?, anh_url = ?, mota = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillProduct(statement, product);
            statement.setInt(8, product.getId());
            statement.executeUpdate();
        }
    }

    public void insertSize(int productId, ProductSize size) throws SQLException {
        String sql = "INSERT INTO kichthuoc_sanpham (sanpham_id, ten_size, gia, dang_hoat_dong) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, size.getTenSize());
            statement.setBigDecimal(3, size.getGia());
            statement.setBoolean(4, size.isDangHoatDong());
            statement.executeUpdate();
        }
    }

    public void updateSize(ProductSize size) throws SQLException {
        String sql = "UPDATE kichthuoc_sanpham SET ten_size = ?, gia = ?, dang_hoat_dong = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, size.getTenSize());
            statement.setBigDecimal(2, size.getGia());
            statement.setBoolean(3, size.isDangHoatDong());
            statement.setInt(4, size.getId());
            statement.executeUpdate();
        }
    }

    public boolean sizeNameExists(int productId, String tenSize, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kichthuoc_sanpham WHERE sanpham_id = ? AND ten_size = ? AND dang_hoat_dong = 1"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, tenSize);
            if (excludeId != null) {
                statement.setInt(3, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean sizeExistsInOrders(int productId, String tenSize) throws SQLException {
        String sql = "SELECT COUNT(*) FROM chitiet_donhang WHERE sanpham_id = ? AND size = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, tenSize);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void softDeleteSize(int id) throws SQLException {
        String sql = "UPDATE kichthuoc_sanpham SET dang_hoat_dong = 0 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void deleteSize(int id) throws SQLException {
        String sql = "DELETE FROM kichthuoc_sanpham WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<Integer> findLinkedChoiceGroupIds(int productId) throws SQLException {
        String sql = "SELECT nhom_lua_chon_id FROM sanpham_nhom_lua_chon WHERE sanpham_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (resultSet.next()) {
                    ids.add(resultSet.getInt(1));
                }
                return ids;
            }
        }
    }

    public List<Integer> findRequiredChoiceGroupIds(int productId) throws SQLException {
        String sql = "SELECT nhom_lua_chon_id FROM sanpham_nhom_lua_chon WHERE sanpham_id = ? AND bat_buoc = 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (resultSet.next()) {
                    ids.add(resultSet.getInt(1));
                }
                return ids;
            }
        }
    }

    public void replaceChoiceLinks(int productId, List<Integer> groupIds, List<Integer> requiredGroupIds) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM sanpham_nhom_lua_chon WHERE sanpham_id = ?");
                 PreparedStatement insert = connection.prepareStatement(
                         "INSERT INTO sanpham_nhom_lua_chon (sanpham_id, nhom_lua_chon_id, bat_buoc) VALUES (?, ?, ?)")) {
                delete.setInt(1, productId);
                delete.executeUpdate();

                for (Integer groupId : groupIds) {
                    insert.setInt(1, productId);
                    insert.setInt(2, groupId);
                    insert.setInt(3, requiredGroupIds.contains(groupId) ? 1 : 0);
                    insert.addBatch();
                }
                insert.executeBatch();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void deactivateProduct(int id) throws SQLException {
        String sql = "UPDATE sanpham SET dang_hoat_dong = 0 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void deleteChoiceLinks(int productId) throws SQLException {
        String sql = "DELETE FROM sanpham_nhom_lua_chon WHERE sanpham_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM sanpham WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getInt("id"));
        product.setMaSanPham(getString(resultSet, "ma_sanpham"));
        product.setTenSanPham(getString(resultSet, "ten_sanpham"));
        product.setDanhMucId(resultSet.getInt("danhmuc_id"));
        product.setTenDanhMuc(getString(resultSet, "ten_danhmuc"));
        product.setGiaBan(resultSet.getBigDecimal("gia_ban"));
        product.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
        product.setAnhUrl(getString(resultSet, "anh_url"));
        product.setMoTa(getString(resultSet, "mota"));
        Timestamp createdAt = getTimestamp(resultSet, "ngay_tao");
        if (createdAt != null) {
            product.setNgayTao(createdAt.toLocalDateTime());
        }
        return product;
    }

    private void fillProduct(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getMaSanPham());
        statement.setString(2, product.getTenSanPham());
        statement.setInt(3, product.getDanhMucId());
        statement.setBigDecimal(4, product.getGiaBan());
        statement.setBoolean(5, product.isDangHoatDong());
        statement.setString(6, product.getAnhUrl());
        statement.setString(7, product.getMoTa());
    }

    private ProductSize mapSize(ResultSet resultSet) throws SQLException {
        ProductSize size = new ProductSize();
        size.setId(resultSet.getInt("id"));
        size.setSanPhamId(resultSet.getInt("sanpham_id"));
        size.setTenSize(resultSet.getString("ten_size"));
        size.setGia(resultSet.getBigDecimal("gia"));
        size.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
        return size;
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private String getString(ResultSet resultSet, String column) throws SQLException {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }

    private Timestamp getTimestamp(ResultSet resultSet, String column) {
        try {
            return resultSet.getTimestamp(column);
        } catch (SQLException ex) {
            return null;
        }
    }
}
