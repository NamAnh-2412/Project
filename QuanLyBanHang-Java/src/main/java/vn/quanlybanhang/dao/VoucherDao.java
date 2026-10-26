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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.model.MaGiamGia;

public class VoucherDao extends BaseDao {

    public List<MaGiamGia> findAll() throws SQLException {
        String sql = "SELECT * FROM ma_giam_gia ORDER BY id DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<MaGiamGia> rows = new ArrayList<>();
            while (resultSet.next()) {
                rows.add(map(resultSet));
            }
            return rows;
        }
    }

    public Optional<MaGiamGia> lockByCode(Connection connection, String code) throws SQLException {
        String sql = "SELECT * FROM ma_giam_gia WHERE ma = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<MaGiamGia> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM ma_giam_gia WHERE ma = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public void insert(MaGiamGia voucher) throws SQLException {
        String sql = "INSERT INTO ma_giam_gia (ma, loai, gia_tri, don_toi_thieu, giam_toi_da, so_lan_toi_da, "
                + "so_lan_da_dung, bat_dau, ket_thuc, trang_thai, ngay_tao) VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, 'hoat_dong', ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, voucher.getMa());
            statement.setString(2, voucher.getLoai());
            statement.setBigDecimal(3, voucher.getGiaTri());
            statement.setBigDecimal(4, voucher.getDonToiThieu());
            if (voucher.getGiamToiDa() == null) {
                statement.setObject(5, null);
            } else {
                statement.setBigDecimal(5, voucher.getGiamToiDa());
            }
            if (voucher.getSoLanToiDa() == null) {
                statement.setObject(6, null);
            } else {
                statement.setInt(6, voucher.getSoLanToiDa());
            }
            statement.setDate(7, voucher.getBatDau() == null ? null : Date.valueOf(voucher.getBatDau()));
            statement.setDate(8, voucher.getKetThuc() == null ? null : Date.valueOf(voucher.getKetThuc()));
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        }
    }

    public void incrementUsed(Connection connection, int id) throws SQLException {
        String sql = "UPDATE ma_giam_gia SET so_lan_da_dung = so_lan_da_dung + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void decrementUsed(Connection connection, String code) throws SQLException {
        String sql = "UPDATE ma_giam_gia SET so_lan_da_dung = GREATEST(so_lan_da_dung - 1, 0) WHERE ma = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.executeUpdate();
        }
    }

    public void setStatus(int id, String status) throws SQLException {
        String sql = "UPDATE ma_giam_gia SET trang_thai = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    private MaGiamGia map(ResultSet resultSet) throws SQLException {
        MaGiamGia voucher = new MaGiamGia();
        voucher.setId(resultSet.getInt("id"));
        voucher.setMa(resultSet.getString("ma"));
        voucher.setLoai(resultSet.getString("loai"));
        voucher.setGiaTri(resultSet.getBigDecimal("gia_tri"));
        voucher.setDonToiThieu(resultSet.getBigDecimal("don_toi_thieu"));
        voucher.setGiamToiDa(resultSet.getBigDecimal("giam_toi_da"));
        int maxUses = resultSet.getInt("so_lan_toi_da");
        voucher.setSoLanToiDa(resultSet.wasNull() ? null : maxUses);
        voucher.setSoLanDaDung(resultSet.getInt("so_lan_da_dung"));
        Date start = resultSet.getDate("bat_dau");
        voucher.setBatDau(start == null ? null : start.toLocalDate());
        Date end = resultSet.getDate("ket_thuc");
        voucher.setKetThuc(end == null ? null : end.toLocalDate());
        voucher.setTrangThai(resultSet.getString("trang_thai"));
        Timestamp created = resultSet.getTimestamp("ngay_tao");
        if (created != null) {
            voucher.setNgayTao(created.toLocalDateTime());
        }
        return voucher;
    }

    public static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
