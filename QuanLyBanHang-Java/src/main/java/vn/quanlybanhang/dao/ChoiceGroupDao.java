package vn.quanlybanhang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;

public class ChoiceGroupDao extends BaseDao {

    public List<NhomLuaChon> findAllWithChoices() throws SQLException {
        String sql = "SELECT nlc.id AS nlc_id, nlc.ten_nhom, nlc.mota, nlc.thu_tu, nlc.dang_hoat_dong, "
                + "lc.id AS lc_id, lc.ten_lua_chon, lc.gia_tang_them, lc.thu_tu AS lc_thu_tu, lc.dang_hoat_dong AS lc_dang_hoat_dong "
                + "FROM nhom_lua_chon nlc "
                + "LEFT JOIN lua_chon lc ON lc.nhom_lua_chon_id = nlc.id "
                + "ORDER BY nlc.thu_tu ASC, nlc.id ASC, lc.thu_tu ASC, lc.id ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Integer, NhomLuaChon> groups = new LinkedHashMap<>();
            while (resultSet.next()) {
                int groupId = resultSet.getInt("nlc_id");
                NhomLuaChon group = groups.computeIfAbsent(groupId, ignored -> {
                    NhomLuaChon item = new NhomLuaChon();
                    item.setId(groupId);
                    try {
                        item.setTenNhom(resultSet.getString("ten_nhom"));
                        item.setMoTa(resultSet.getString("mota"));
                        item.setThuTu(resultSet.getInt("thu_tu"));
                        item.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                    return item;
                });

                int choiceId = resultSet.getInt("lc_id");
                if (!resultSet.wasNull()) {
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
            return new ArrayList<>(groups.values());
        }
    }

    public Optional<NhomLuaChon> findById(int id) throws SQLException {
        String sql = "SELECT * FROM nhom_lua_chon WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    NhomLuaChon group = new NhomLuaChon();
                    group.setId(resultSet.getInt("id"));
                    group.setTenNhom(resultSet.getString("ten_nhom"));
                    group.setMoTa(resultSet.getString("mota"));
                    group.setThuTu(resultSet.getInt("thu_tu"));
                    group.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
                    return Optional.of(group);
                }
                return Optional.empty();
            }
        }
    }

    public void insertGroup(NhomLuaChon group) throws SQLException {
        String sql = "INSERT INTO nhom_lua_chon (ten_nhom, mota, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, group.getTenNhom());
            statement.setString(2, group.getMoTa());
            statement.setInt(3, group.getThuTu());
            statement.setBoolean(4, group.isDangHoatDong());
            statement.executeUpdate();
        }
    }

    public void updateGroup(NhomLuaChon group) throws SQLException {
        String sql = "UPDATE nhom_lua_chon SET ten_nhom = ?, mota = ?, thu_tu = ?, dang_hoat_dong = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, group.getTenNhom());
            statement.setString(2, group.getMoTa());
            statement.setInt(3, group.getThuTu());
            statement.setBoolean(4, group.isDangHoatDong());
            statement.setInt(5, group.getId());
            statement.executeUpdate();
        }
    }

    public boolean hasActiveProductLinks(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sanpham_nhom_lua_chon splc "
                + "JOIN sanpham sp ON sp.id = splc.sanpham_id "
                + "WHERE splc.nhom_lua_chon_id = ? AND sp.dang_hoat_dong = 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean hasActiveChoices(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lua_chon WHERE nhom_lua_chon_id = ? AND dang_hoat_dong = 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public void deleteGroup(int groupId) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteLinks = connection.prepareStatement("DELETE FROM sanpham_nhom_lua_chon WHERE nhom_lua_chon_id = ?");
                 PreparedStatement deleteGroup = connection.prepareStatement("DELETE FROM nhom_lua_chon WHERE id = ?")) {
                deleteLinks.setInt(1, groupId);
                deleteLinks.executeUpdate();

                deleteGroup.setInt(1, groupId);
                deleteGroup.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void insertChoice(LuaChon choice) throws SQLException {
        String sql = "INSERT INTO lua_chon (nhom_lua_chon_id, ten_lua_chon, gia_tang_them, thu_tu, dang_hoat_dong) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, choice.getNhomLuaChonId());
            statement.setString(2, choice.getTenLuaChon());
            statement.setBigDecimal(3, choice.getGiaTangThem());
            statement.setInt(4, choice.getThuTu());
            statement.setBoolean(5, choice.isDangHoatDong());
            statement.executeUpdate();
        }
    }

    public Optional<LuaChon> findChoiceById(int id) throws SQLException {
        String sql = "SELECT * FROM lua_chon WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LuaChon choice = new LuaChon();
                    choice.setId(resultSet.getInt("id"));
                    choice.setNhomLuaChonId(resultSet.getInt("nhom_lua_chon_id"));
                    choice.setTenLuaChon(resultSet.getString("ten_lua_chon"));
                    choice.setGiaTangThem(resultSet.getBigDecimal("gia_tang_them"));
                    choice.setThuTu(resultSet.getInt("thu_tu"));
                    choice.setDangHoatDong(resultSet.getBoolean("dang_hoat_dong"));
                    return Optional.of(choice);
                }
                return Optional.empty();
            }
        }
    }

    public void updateChoice(LuaChon choice) throws SQLException {
        String sql = "UPDATE lua_chon SET ten_lua_chon = ?, gia_tang_them = ?, thu_tu = ?, dang_hoat_dong = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, choice.getTenLuaChon());
            statement.setBigDecimal(2, choice.getGiaTangThem());
            statement.setInt(3, choice.getThuTu());
            statement.setBoolean(4, choice.isDangHoatDong());
            statement.setInt(5, choice.getId());
            statement.executeUpdate();
        }
    }

    public void softDeleteChoice(int id) throws SQLException {
        String sql = "UPDATE lua_chon SET dang_hoat_dong = 0 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}
