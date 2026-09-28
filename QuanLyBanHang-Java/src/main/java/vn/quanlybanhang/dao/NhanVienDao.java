package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.model.NhanVien;

public class NhanVienDao extends BaseDao {

    public Optional<NhanVien> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, ma_nhanvien, ten_dang_nhap, mat_khau, ten_nhanvien, chuc_vu, so_dien_thoai, email, trang_thai, ngay_sinh, ngay_vao_lam "
                + "FROM nhanvien WHERE ten_dang_nhap = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapNhanVien(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<NhanVien> findAll() throws SQLException {
        String sql = "SELECT id, ma_nhanvien, ten_dang_nhap, ten_nhanvien, chuc_vu, so_dien_thoai, email, trang_thai, ngay_sinh, ngay_vao_lam "
                + "FROM nhanvien ORDER BY id DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<NhanVien> employees = new ArrayList<>();
            while (resultSet.next()) {
                employees.add(mapNhanVien(resultSet));
            }
            return employees;
        }
    }

    public Optional<NhanVien> findById(int id) throws SQLException {
        String sql = "SELECT id, ma_nhanvien, ten_dang_nhap, mat_khau, ten_nhanvien, chuc_vu, so_dien_thoai, email, trang_thai, ngay_sinh, ngay_vao_lam "
                + "FROM nhanvien WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapNhanVien(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public boolean existsUsername(String username, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhanvien WHERE ten_dang_nhap = ?"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            if (excludeId != null) {
                statement.setInt(2, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean existsEmail(String email, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhanvien WHERE email = ?"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            if (excludeId != null) {
                statement.setInt(2, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean existsMaNhanVien(String maNhanVien, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhanvien WHERE ma_nhanvien = ?"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNhanVien);
            if (excludeId != null) {
                statement.setInt(2, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean existsSameNgayVaoLam(Date ngayVaoLam) throws SQLException {
        String sql = "SELECT COUNT(*) FROM nhanvien WHERE ngay_vao_lam = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, ngayVaoLam);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void insert(NhanVien employee) throws SQLException {
        String sql = "INSERT INTO nhanvien (ma_nhanvien, ten_dang_nhap, mat_khau, ten_nhanvien, chuc_vu, so_dien_thoai, email, trang_thai, ngay_sinh, ngay_vao_lam) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, employee.getMaNhanVien());
            statement.setString(2, employee.getTenDangNhap());
            statement.setString(3, employee.getMatKhau());
            statement.setString(4, employee.getTenNhanVien());
            statement.setString(5, employee.getChucVu());
            statement.setString(6, employee.getSoDienThoai());
            statement.setString(7, employee.getEmail());
            statement.setString(8, employee.getTrangThai());
            if (employee.getNgaySinh() != null) {
                statement.setDate(9, Date.valueOf(employee.getNgaySinh()));
            } else {
                statement.setDate(9, null);
            }
            if (employee.getNgayVaoLam() != null) {
                statement.setDate(10, Date.valueOf(employee.getNgayVaoLam()));
            } else {
                statement.setDate(10, null);
            }
            statement.executeUpdate();
        }
    }

    public void update(NhanVien employee) throws SQLException {
        String sql = "UPDATE nhanvien SET ma_nhanvien = ?, ten_dang_nhap = ?, ten_nhanvien = ?, chuc_vu = ?, so_dien_thoai = ?, "
                + "email = ?, trang_thai = ?, ngay_sinh = ?, ngay_vao_lam = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getMaNhanVien());
            statement.setString(2, employee.getTenDangNhap());
            statement.setString(3, employee.getTenNhanVien());
            statement.setString(4, employee.getChucVu());
            statement.setString(5, employee.getSoDienThoai());
            statement.setString(6, employee.getEmail());
            statement.setString(7, employee.getTrangThai());
            if (employee.getNgaySinh() != null) {
                statement.setDate(8, Date.valueOf(employee.getNgaySinh()));
            } else {
                statement.setDate(8, null);
            }
            if (employee.getNgayVaoLam() != null) {
                statement.setDate(9, Date.valueOf(employee.getNgayVaoLam()));
            } else {
                statement.setDate(9, null);
            }
            statement.setInt(10, employee.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM nhanvien WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void updateRole(int id, String role) throws SQLException {
        String sql = "UPDATE nhanvien SET chuc_vu = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void updatePassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE nhanvien SET mat_khau = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void insertLoginHistory(Integer nhanVienId, String username, String ip, String status) throws SQLException {
        String sql = "INSERT INTO login_history (nhanvien_id, ten_dang_nhap, ip, trang_thai) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (nhanVienId != null) {
                statement.setInt(1, nhanVienId);
            } else {
                statement.setObject(1, null);
            }
            statement.setString(2, username);
            statement.setString(3, ip);
            statement.setString(4, status);
            statement.executeUpdate();
        }
    }

    private NhanVien mapNhanVien(ResultSet resultSet) throws SQLException {
        NhanVien employee = new NhanVien();
        employee.setId(resultSet.getInt("id"));
        employee.setMaNhanVien(getOptionalString(resultSet, "ma_nhanvien"));
        employee.setTenDangNhap(getOptionalString(resultSet, "ten_dang_nhap"));
        employee.setMatKhau(getOptionalString(resultSet, "mat_khau"));
        employee.setTenNhanVien(getOptionalString(resultSet, "ten_nhanvien"));
        employee.setChucVu(getOptionalString(resultSet, "chuc_vu"));
        employee.setSoDienThoai(getOptionalString(resultSet, "so_dien_thoai"));
        employee.setEmail(getOptionalString(resultSet, "email"));
        employee.setTrangThai(getOptionalString(resultSet, "trang_thai"));
        Date ngaySinh = resultSet.getDate("ngay_sinh");
        if (ngaySinh != null) {
            employee.setNgaySinh(ngaySinh.toLocalDate());
        }
        Date ngayVaoLam = resultSet.getDate("ngay_vao_lam");
        if (ngayVaoLam != null) {
            employee.setNgayVaoLam(ngayVaoLam.toLocalDate());
        }
        return employee;
    }

    private String getOptionalString(ResultSet resultSet, String column) throws SQLException {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            return null;
        }
    }
}
