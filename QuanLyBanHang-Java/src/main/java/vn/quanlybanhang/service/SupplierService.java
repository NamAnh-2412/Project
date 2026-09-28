package vn.quanlybanhang.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.SupplierDao;
import vn.quanlybanhang.model.Supplier;
import vn.quanlybanhang.util.ValidationUtil;

public class SupplierService {

    private final SupplierDao supplierDao = new SupplierDao();

    public List<Supplier> search(String keyword, String status) throws SQLException {
        return supplierDao.search(keyword, status);
    }

    public Optional<Supplier> findById(int id) throws SQLException {
        return supplierDao.findById(id);
    }

    public Optional<SupplierDetailView> getSupplierDetail(int id) throws SQLException {
        Optional<Supplier> supplierOpt = supplierDao.findById(id);
        if (supplierOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SupplierDetailView(
                supplierOpt.get(),
                supplierDao.findMaterialsBySupplierId(id),
                supplierDao.findImportHistoryBySupplierId(id),
                supplierDao.getSupplierViewStats(id)));
    }

    public Supplier bindSupplier(String name, String phone, String email, String address, String taxCode,
                                 String contactPerson, String status, String note) {
        Supplier supplier = new Supplier();
        supplier.setTenNhaCungCap(trimOrNull(name));
        supplier.setSoDienThoai(trimOrNull(phone));
        supplier.setEmail(trimOrNull(email));
        supplier.setDiaChi(trimOrNull(address));
        supplier.setMaSoThue(trimOrNull(taxCode));
        supplier.setNguoiLienHe(trimOrNull(contactPerson));
        supplier.setTrangThai(status == null || status.isBlank() ? "dang_hop_tac" : status.trim());
        supplier.setGhiChu(trimOrNull(note));
        return supplier;
    }

    public String validate(Supplier supplier) {
        if (supplier.getTenNhaCungCap() == null || supplier.getTenNhaCungCap().isBlank()) {
            return "Vui lòng nhập tên nhà cung cấp.";
        }
        if (supplier.getSoDienThoai() != null && !supplier.getSoDienThoai().isBlank()
                && !ValidationUtil.isValidPhone(supplier.getSoDienThoai())) {
            return "Số điện thoại không hợp lệ.";
        }
        if (supplier.getEmail() != null && !supplier.getEmail().isBlank()
                && !ValidationUtil.isValidEmail(supplier.getEmail())) {
            return "Email không hợp lệ.";
        }
        return null;
    }

    public Optional<String> createSupplier(Supplier supplier) throws SQLException {
        String error = validate(supplier);
        if (error != null) {
            return Optional.of(error);
        }
        supplierDao.insert(supplier);
        return Optional.empty();
    }

    public Optional<String> updateSupplier(Supplier supplier) throws SQLException {
        String error = validate(supplier);
        if (error != null) {
            return Optional.of(error);
        }
        supplierDao.update(supplier);
        return Optional.empty();
    }

    public String deleteSupplier(int id) throws SQLException {
        if (supplierDao.hasNhapKho(id)) {
            supplierDao.softDelete(id);
            return "Nhà cung cấp đã được chuyển sang trạng thái ngừng hợp tác.";
        }
        supplierDao.delete(id);
        return "Đã xóa nhà cung cấp.";
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record SupplierDetailView(
            Supplier supplier,
            List<Map<String, Object>> materials,
            List<Map<String, Object>> importHistory,
            Map<String, Object> stats) {
    }
}
