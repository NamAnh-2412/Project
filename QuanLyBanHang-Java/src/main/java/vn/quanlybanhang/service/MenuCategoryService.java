package vn.quanlybanhang.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.dao.DanhMucDao;
import vn.quanlybanhang.model.DanhMuc;

public class MenuCategoryService {

    private final DanhMucDao danhMucDao = new DanhMucDao();

    public List<DanhMuc> listAll() throws SQLException {
        return danhMucDao.findAllThucDon();
    }

    public Optional<DanhMuc> findById(int id) throws SQLException {
        return danhMucDao.findById(id);
    }

    public DanhMuc bindCategory(String name, String description, Integer parentId, Integer sortOrder, String activeRaw) {
        DanhMuc category = new DanhMuc();
        category.setTenDanhMuc(trimOrNull(name));
        category.setMoTa(trimOrNull(description));
        category.setDanhMucCha(parentId);
        category.setThuTu(sortOrder);
        category.setDangHoatDong(!"0".equals(activeRaw));
        return category;
    }

    public String validate(DanhMuc category) {
        if (category.getTenDanhMuc() == null || category.getTenDanhMuc().isBlank()) {
            return "Vui lòng nhập tên danh mục.";
        }
        return null;
    }

    public Optional<String> saveCategory(DanhMuc category, int id) throws SQLException {
        String error = validate(category);
        if (error != null) {
            return Optional.of(error);
        }
        if (id > 0) {
            category.setId(id);
            danhMucDao.update(category);
        } else {
            danhMucDao.insert(category);
        }
        return Optional.empty();
    }

    public Optional<String> deleteCategory(int id) throws SQLException {
        if (danhMucDao.hasProducts(id) || danhMucDao.hasChildren(id)) {
            return Optional.of("Không thể xóa thực đơn đang có sản phẩm hoặc danh mục con.");
        }
        danhMucDao.delete(id);
        return Optional.empty();
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
