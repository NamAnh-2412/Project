package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.model.DanhMuc;

public class DanhMucDao extends BaseDao {

    public List<DanhMuc> findActiveThucDon() throws SQLException {
        String sql = "SELECT * FROM danhmuc WHERE (mota IS NULL OR mota <> 'nhóm lựa chọn') AND dang_hoat_dong = 1 ORDER BY thu_tu ASC, id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<DanhMuc> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(mapDanhMuc(resultSet));
            }
            return categories;
        }
    }

    public List<DanhMuc> findAllThucDon() throws SQLException {
        String sql = "SELECT * FROM danhmuc WHERE (mota IS NULL OR mota <> 'nhóm lựa chọn') ORDER BY thu_tu ASC, id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<DanhMuc> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(mapDanhMuc(resultSet));
            }
            return categories;
        }
    }

    public Optional<DanhMuc> findById(int id) throws SQLException {
        String sql = "SELECT * FROM danhmuc WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapDanhMuc(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public boolean hasProducts(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sanpham WHERE danhmuc_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean hasChildren(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM danhmuc WHERE danhmuc_cha = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void insert(DanhMuc category) throws SQLException {
        String sql = "INSERT INTO danhmuc (ten_danhmuc, mota, danhmuc_cha, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillCategory(statement, category);
            statement.executeUpdate();
        }
    }

    public void update(DanhMuc category) throws SQLException {
        String sql = "UPDATE danhmuc SET ten_danhmuc = ?, mota = ?, danhmuc_cha = ?, thu_tu = ?, dang_hoat_dong = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillCategory(statement, category);
            statement.setInt(6, category.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM danhmuc WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void fillCategory(PreparedStatement statement, DanhMuc category) throws SQLException {
        statement.setString(1, category.getTenDanhMuc());
        statement.setString(2, category.getMoTa());
        if (category.getDanhMucCha() != null) {
            statement.setInt(3, category.getDanhMucCha());
        } else {
            statement.setObject(3, null);
        }
        if (category.getThuTu() != null) {
            statement.setInt(4, category.getThuTu());
        } else {
            statement.setObject(4, null);
        }
        statement.setBoolean(5, category.isDangHoatDong());
    }

    private DanhMuc mapDanhMuc(ResultSet resultSet) throws SQLException {
        DanhMuc category = new DanhMuc();
        category.setId(resultSet.getInt("id"));
        category.setTenDanhMuc(resultSet.getString("ten_danhmuc"));
        category.setMoTa(getSafeString(resultSet, "mota"));
        Integer parentId = (Integer) resultSet.getObject("danhmuc_cha");
        category.setDanhMucCha(parentId);
        Integer order = (Integer) resultSet.getObject("thu_tu");
        category.setThuTu(order);
        category.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
        return category;
    }

    private String getSafeString(ResultSet resultSet, String column) throws SQLException {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }
}
