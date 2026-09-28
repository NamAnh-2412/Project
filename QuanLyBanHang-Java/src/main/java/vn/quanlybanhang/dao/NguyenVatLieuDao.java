package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.model.NguyenVatLieu;

public class NguyenVatLieuDao extends BaseDao {

    /**
     * {@code true} khi bang co cot lien ket NCC (giong PHP / man xem NCC).
     */
    public boolean hasSupplierColumn() throws SQLException {
        return columnExists("nguyenvatlieu", "nha_cung_cap_id");
    }

    /**
     * Tim theo ten (LIKE) va/hoac loc {@code danhmuc_nl} — tuong tu {@code kw} + {@code loai} tren PHP.
     */
    public List<NguyenVatLieu> search(String keyword, String danhMucNl) throws SQLException {
        boolean withNcc = columnExists("nguyenvatlieu", "nha_cung_cap_id");
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasLoai = danhMucNl != null && !danhMucNl.isBlank();

        if (withNcc) {
            StringBuilder sql = new StringBuilder(
                    "SELECT nvl.*, ncc.ten_nhacungcap AS ten_nha_cung_cap FROM nguyenvatlieu nvl "
                            + "LEFT JOIN nhacungcap ncc ON nvl.nha_cung_cap_id = ncc.id WHERE 1=1");
            if (hasKw) {
                sql.append(" AND nvl.ten_nguyenlieu LIKE ?");
            }
            if (hasLoai) {
                sql.append(" AND nvl.danhmuc_nl = ?");
            }
            sql.append(" ORDER BY nvl.danhmuc_nl, nvl.ten_nguyenlieu");
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                int i = 1;
                if (hasKw) {
                    statement.setString(i++, "%" + keyword.trim() + "%");
                }
                if (hasLoai) {
                    statement.setString(i, danhMucNl.trim());
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapMaterialList(resultSet);
                }
            }
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM nguyenvatlieu WHERE 1=1");
        if (hasKw) {
            sql.append(" AND ten_nguyenlieu LIKE ?");
        }
        if (hasLoai) {
            sql.append(" AND danhmuc_nl = ?");
        }
        sql.append(" ORDER BY danhmuc_nl, ten_nguyenlieu");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasKw) {
                statement.setString(i++, "%" + keyword.trim() + "%");
            }
            if (hasLoai) {
                statement.setString(i, danhMucNl.trim());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapMaterialList(resultSet);
            }
        }
    }

    public List<NguyenVatLieu> findAll() throws SQLException {
        if (columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            String sql = "SELECT nvl.*, ncc.ten_nhacungcap AS ten_nha_cung_cap "
                    + "FROM nguyenvatlieu nvl "
                    + "LEFT JOIN nhacungcap ncc ON nvl.nha_cung_cap_id = ncc.id "
                    + "ORDER BY nvl.danhmuc_nl, nvl.ten_nguyenlieu";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                return mapMaterialList(resultSet);
            }
        }
        String sql = "SELECT * FROM nguyenvatlieu ORDER BY danhmuc_nl, ten_nguyenlieu";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapMaterialList(resultSet);
        }
    }

    public Optional<NguyenVatLieu> findById(int id) throws SQLException {
        if (columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            String sql = "SELECT nvl.*, ncc.ten_nhacungcap AS ten_nha_cung_cap "
                    + "FROM nguyenvatlieu nvl "
                    + "LEFT JOIN nhacungcap ncc ON nvl.nha_cung_cap_id = ncc.id "
                    + "WHERE nvl.id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapMaterial(resultSet));
                    }
                    return Optional.empty();
                }
            }
        }
        String sql = "SELECT * FROM nguyenvatlieu WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMaterial(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public void insert(NguyenVatLieu material) throws SQLException {
        if (columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            String sql = "INSERT INTO nguyenvatlieu (ma_nguyenlieu, ten_nguyenlieu, danhmuc_nl, don_vi_tinh, trang_thai, "
                    + "so_luong_ton, so_luong_toi_thieu, gia_nhap, nha_cung_cap_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                fillMaterial(statement, material);
                if (material.getNhaCungCapId() != null && material.getNhaCungCapId() > 0) {
                    statement.setInt(9, material.getNhaCungCapId());
                } else {
                    statement.setObject(9, null);
                }
                statement.executeUpdate();
            }
            return;
        }
        String sql = "INSERT INTO nguyenvatlieu (ma_nguyenlieu, ten_nguyenlieu, danhmuc_nl, don_vi_tinh, trang_thai, "
                + "so_luong_ton, so_luong_toi_thieu, gia_nhap) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillMaterial(statement, material);
            statement.executeUpdate();
        }
    }

    public void update(NguyenVatLieu material) throws SQLException {
        if (columnExists("nguyenvatlieu", "nha_cung_cap_id")) {
            String sql = "UPDATE nguyenvatlieu SET ma_nguyenlieu = ?, ten_nguyenlieu = ?, danhmuc_nl = ?, don_vi_tinh = ?, "
                    + "trang_thai = ?, so_luong_ton = ?, so_luong_toi_thieu = ?, gia_nhap = ?, nha_cung_cap_id = ? WHERE id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                fillMaterial(statement, material);
                if (material.getNhaCungCapId() != null && material.getNhaCungCapId() > 0) {
                    statement.setInt(9, material.getNhaCungCapId());
                } else {
                    statement.setObject(9, null);
                }
                statement.setInt(10, material.getId());
                statement.executeUpdate();
            }
            return;
        }
        String sql = "UPDATE nguyenvatlieu SET ma_nguyenlieu = ?, ten_nguyenlieu = ?, danhmuc_nl = ?, don_vi_tinh = ?, "
                + "trang_thai = ?, so_luong_ton = ?, so_luong_toi_thieu = ?, gia_nhap = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillMaterial(statement, material);
            statement.setInt(9, material.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM nguyenvatlieu WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void increaseStock(Connection connection, int id, java.math.BigDecimal amount) throws SQLException {
        String sql = "UPDATE nguyenvatlieu SET so_luong_ton = COALESCE(so_luong_ton, 0) + ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void decreaseStock(Connection connection, int id, java.math.BigDecimal amount) throws SQLException {
        String sql = "UPDATE nguyenvatlieu SET so_luong_ton = GREATEST(COALESCE(so_luong_ton, 0) - ?, 0) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void setStock(Connection connection, int id, java.math.BigDecimal amount) throws SQLException {
        String sql = "UPDATE nguyenvatlieu SET so_luong_ton = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount == null ? java.math.BigDecimal.ZERO : amount);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    /**
     * Danh sach nguyen lieu con ton (so_luong_ton &gt; 0) — phuc vu tab Ton kho.
     */
    public List<NguyenVatLieu> findInStock(String keyword, String danhMucNl) throws SQLException {
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasLoai = danhMucNl != null && !danhMucNl.isBlank();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM nguyenvatlieu WHERE IFNULL(so_luong_ton, 0) > 0");
        if (hasKw) {
            sql.append(" AND ten_nguyenlieu LIKE ?");
        }
        if (hasLoai) {
            sql.append(" AND danhmuc_nl = ?");
        }
        sql.append(" ORDER BY danhmuc_nl, ten_nguyenlieu");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasKw) {
                statement.setString(i++, "%" + keyword.trim() + "%");
            }
            if (hasLoai) {
                statement.setString(i, danhMucNl.trim());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapMaterialList(resultSet);
            }
        }
    }

    /**
     * Nguyen lieu co the kiem ke (khong o trang thai ngung_ban/ngung_nhap).
     */
    public List<NguyenVatLieu> findForAudit() throws SQLException {
        String sql = "SELECT * FROM nguyenvatlieu WHERE trang_thai NOT IN ('ngung_ban', 'ngung_nhap') "
                + "ORDER BY danhmuc_nl, ten_nguyenlieu";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapMaterialList(resultSet);
        }
    }

    private List<NguyenVatLieu> mapMaterialList(ResultSet resultSet) throws SQLException {
        List<NguyenVatLieu> materials = new ArrayList<>();
        while (resultSet.next()) {
            materials.add(mapMaterial(resultSet));
        }
        return materials;
    }

    private void fillMaterial(PreparedStatement statement, NguyenVatLieu material) throws SQLException {
        statement.setString(1, material.getMaNguyenLieu());
        statement.setString(2, material.getTenNguyenLieu());
        statement.setString(3, material.getDanhMucNl());
        statement.setString(4, material.getDonViTinh());
        statement.setString(5, material.getTrangThai());
        statement.setBigDecimal(6, material.getSoLuongTon());
        statement.setBigDecimal(7, material.getSoLuongToiThieu());
        statement.setBigDecimal(8, material.getGiaNhap());
    }

    private NguyenVatLieu mapMaterial(ResultSet resultSet) throws SQLException {
        NguyenVatLieu material = new NguyenVatLieu();
        material.setId(resultSet.getInt("id"));
        material.setMaNguyenLieu(resultSet.getString("ma_nguyenlieu"));
        material.setTenNguyenLieu(resultSet.getString("ten_nguyenlieu"));
        material.setDanhMucNl(getSafeString(resultSet, "danhmuc_nl"));
        material.setDonViTinh(getSafeString(resultSet, "don_vi_tinh"));
        material.setTrangThai(getSafeString(resultSet, "trang_thai"));
        material.setSoLuongTon(resultSet.getBigDecimal("so_luong_ton"));
        material.setSoLuongToiThieu(resultSet.getBigDecimal("so_luong_toi_thieu"));
        material.setGiaNhap(resultSet.getBigDecimal("gia_nhap"));
        material.setNhaCungCapId(getIntegerObject(resultSet, "nha_cung_cap_id"));
        material.setTenNhaCungCap(getSafeString(resultSet, "ten_nha_cung_cap"));
        return material;
    }

    private Integer getIntegerObject(ResultSet resultSet, String column) {
        try {
            int v = resultSet.getInt(column);
            return resultSet.wasNull() ? null : v;
        } catch (SQLException ex) {
            return null;
        }
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

    private String getSafeString(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }
}
