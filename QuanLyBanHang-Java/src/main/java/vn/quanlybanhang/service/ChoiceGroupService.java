package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.dao.ChoiceGroupDao;
import vn.quanlybanhang.model.LuaChon;
import vn.quanlybanhang.model.NhomLuaChon;

public class ChoiceGroupService {

    private final ChoiceGroupDao choiceGroupDao = new ChoiceGroupDao();

    public List<NhomLuaChon> listAllWithChoices() throws SQLException {
        return choiceGroupDao.findAllWithChoices();
    }

    public NhomLuaChon bindGroup(int id, String name, String description, int sortOrder, String activeRaw) {
        NhomLuaChon group = new NhomLuaChon();
        group.setId(id);
        group.setTenNhom(trimOrNull(name));
        group.setMoTa(trimOrNull(description));
        group.setThuTu(sortOrder);
        group.setDangHoatDong(!"0".equals(activeRaw));
        return group;
    }

    public LuaChon bindChoice(int id, int groupId, String name, String extraPriceRaw, int sortOrder, String activeRaw) {
        LuaChon choice = new LuaChon();
        choice.setId(id);
        choice.setNhomLuaChonId(groupId);
        choice.setTenLuaChon(trimOrNull(name));
        choice.setGiaTangThem(toBigDecimal(extraPriceRaw));
        choice.setThuTu(sortOrder);
        choice.setDangHoatDong(!"0".equals(activeRaw));
        return choice;
    }

    public Optional<String> saveGroup(NhomLuaChon group) throws SQLException {
        if (group.getTenNhom() == null || group.getTenNhom().isBlank()) {
            return Optional.of("Vui lòng nhập tên nhóm lựa chọn.");
        }
        if (group.getId() > 0) {
            choiceGroupDao.updateGroup(group);
        } else {
            choiceGroupDao.insertGroup(group);
        }
        return Optional.empty();
    }

    public Optional<String> saveChoice(LuaChon choice) throws SQLException {
        if (choice.getNhomLuaChonId() <= 0 || choice.getTenLuaChon() == null || choice.getTenLuaChon().isBlank()) {
            return Optional.of("Vui lòng nhập đầy đủ thông tin lựa chọn.");
        }
        if (choice.getId() > 0) {
            choiceGroupDao.updateChoice(choice);
        } else {
            choiceGroupDao.insertChoice(choice);
        }
        return Optional.empty();
    }

    public Optional<String> deleteGroup(int id) throws SQLException {
        if (choiceGroupDao.hasActiveProductLinks(id) || choiceGroupDao.hasActiveChoices(id)) {
            return Optional.of("Không thể xóa nhóm lựa chọn đang có sản phẩm hoặc lựa chọn hoạt động.");
        }
        choiceGroupDao.deleteGroup(id);
        return Optional.empty();
    }

    public boolean softDeleteChoice(int id) throws SQLException {
        Optional<LuaChon> choice = choiceGroupDao.findChoiceById(id);
        if (choice.isEmpty()) {
            return false;
        }
        choiceGroupDao.softDeleteChoice(id);
        return true;
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
