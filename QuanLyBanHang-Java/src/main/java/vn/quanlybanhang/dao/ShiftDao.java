package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.model.CaLam;
import vn.quanlybanhang.model.ShiftTotals;

public class ShiftDao extends BaseDao {

    public Optional<CaLam> findOpenByStaff(int staffId) throws SQLException {
        String sql = "SELECT * FROM ca_lam_viec WHERE nhanvien_id = ? AND trang_thai = 'mo' ORDER BY id DESC LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, staffId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public int insert(CaLam shift) throws SQLException {
        String sql = "INSERT INTO ca_lam_viec (nhanvien_id, ten_nhanvien, trang_thai, tien_dau_ca, mo_luc) "
                + "VALUES (?, ?, 'mo', ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, shift.getNhanVienId());
            statement.setString(2, shift.getTenNhanVien());
            statement.setBigDecimal(3, shift.getTienDauCa());
            statement.setTimestamp(4, Timestamp.valueOf(shift.getMoLuc()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public void close(CaLam shift) throws SQLException {
        String sql = "UPDATE ca_lam_viec SET trang_thai = 'dong', tien_mat = ?, tien_chuyen_khoan = ?, tien_the = ?, "
                + "tien_khac = ?, so_don = ?, tien_ky_vong = ?, tien_thuc_te = ?, chenh_lech = ?, ghi_chu = ?, dong_luc = ? "
                + "WHERE id = ? AND trang_thai = 'mo'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, shift.getTienMat());
            statement.setBigDecimal(2, shift.getTienChuyenKhoan());
            statement.setBigDecimal(3, shift.getTienThe());
            statement.setBigDecimal(4, shift.getTienKhac());
            statement.setInt(5, shift.getSoDon());
            statement.setBigDecimal(6, shift.getTienKyVong());
            statement.setBigDecimal(7, shift.getTienThucTe());
            statement.setBigDecimal(8, shift.getChenhLech());
            statement.setString(9, shift.getGhiChu());
            statement.setTimestamp(10, Timestamp.valueOf(shift.getDongLuc()));
            statement.setInt(11, shift.getId());
            statement.executeUpdate();
        }
    }

    public List<CaLam> recent(int limit) throws SQLException {
        String sql = "SELECT * FROM ca_lam_viec ORDER BY id DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CaLam> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(map(resultSet));
                }
                return rows;
            }
        }
    }

    public ShiftTotals totalsForStaff(int staffId, LocalDateTime openedAt) throws SQLException {
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = ? THEN thanh_tien ELSE 0 END), 0) AS tien_mat, "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = ? THEN thanh_tien ELSE 0 END), 0) AS chuyen_khoan, "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = ? THEN thanh_tien ELSE 0 END), 0) AS the_amount, "
                + "COALESCE(SUM(CASE WHEN phuong_thuc_thanhtoan = ? THEN thanh_tien ELSE 0 END), 0) AS app_online, "
                + "COUNT(*) AS so_don "
                + "FROM donhang WHERE trang_thai = ? AND nhanvien_id = ? AND ngay_tao >= ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, AppConstants.PAYMENT_CASH);
            statement.setString(2, AppConstants.PAYMENT_BANK);
            statement.setString(3, AppConstants.PAYMENT_CARD);
            statement.setString(4, AppConstants.PAYMENT_APP_ONLINE);
            statement.setString(5, AppConstants.ORDER_STATUS_COMPLETED);
            statement.setInt(6, staffId);
            statement.setTimestamp(7, Timestamp.valueOf(openedAt));
            try (ResultSet resultSet = statement.executeQuery()) {
                ShiftTotals totals = new ShiftTotals();
                if (resultSet.next()) {
                    totals.setTienMat(resultSet.getBigDecimal("tien_mat"));
                    totals.setTienChuyenKhoan(resultSet.getBigDecimal("chuyen_khoan"));
                    totals.setTienThe(resultSet.getBigDecimal("the_amount"));
                    totals.setTienKhac(resultSet.getBigDecimal("app_online"));
                    totals.setSoDon(resultSet.getInt("so_don"));
                }
                return totals;
            }
        }
    }

    private CaLam map(ResultSet resultSet) throws SQLException {
        CaLam shift = new CaLam();
        shift.setId(resultSet.getInt("id"));
        shift.setNhanVienId(resultSet.getInt("nhanvien_id"));
        shift.setTenNhanVien(resultSet.getString("ten_nhanvien"));
        shift.setTrangThai(resultSet.getString("trang_thai"));
        shift.setTienDauCa(resultSet.getBigDecimal("tien_dau_ca"));
        shift.setTienMat(resultSet.getBigDecimal("tien_mat"));
        shift.setTienChuyenKhoan(resultSet.getBigDecimal("tien_chuyen_khoan"));
        shift.setTienThe(resultSet.getBigDecimal("tien_the"));
        shift.setTienKhac(resultSet.getBigDecimal("tien_khac"));
        shift.setSoDon(resultSet.getInt("so_don"));
        shift.setTienKyVong(resultSet.getBigDecimal("tien_ky_vong"));
        shift.setTienThucTe(resultSet.getBigDecimal("tien_thuc_te"));
        shift.setChenhLech(resultSet.getBigDecimal("chenh_lech"));
        shift.setGhiChu(resultSet.getString("ghi_chu"));
        Timestamp opened = resultSet.getTimestamp("mo_luc");
        if (opened != null) {
            shift.setMoLuc(opened.toLocalDateTime());
        }
        Timestamp closed = resultSet.getTimestamp("dong_luc");
        if (closed != null) {
            shift.setDongLuc(closed.toLocalDateTime());
        }
        return shift;
    }
}
