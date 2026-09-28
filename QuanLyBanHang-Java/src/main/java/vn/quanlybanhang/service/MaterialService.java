package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.NguyenVatLieuDao;
import vn.quanlybanhang.dao.SupplierDao;
import vn.quanlybanhang.model.NguyenVatLieu;

public class MaterialService {

    private static final Map<String, String> DANHMUC_NL_LABELS = createDanhMucNlLabels();
    private static final DateTimeFormatter MA_NGUYEN_LIEU_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final NguyenVatLieuDao nguyenVatLieuDao = new NguyenVatLieuDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final SettingsService settingsService = new SettingsService();

    public Map<String, String> getDanhMucNlLabels() {
        return DANHMUC_NL_LABELS;
    }

    public MaterialPageData loadPage(String keyword, String category) throws SQLException {
        return new MaterialPageData(
                nguyenVatLieuDao.search(keyword, category),
                supplierDao.search(null, null));
    }

    public Optional<NguyenVatLieu> findById(int id) throws SQLException {
        return nguyenVatLieuDao.findById(id);
    }

    public NguyenVatLieu bindMaterial(String code, String name, String category, String unit, String statusRaw,
                                       String stockRaw, String minStockRaw, String importPriceRaw, int supplierId) {
        NguyenVatLieu material = new NguyenVatLieu();
        material.setMaNguyenLieu(trimOrNull(code));
        material.setTenNguyenLieu(trimOrNull(name));
        material.setDanhMucNl(trimOrNull(category));
        material.setDonViTinh(trimOrNull(unit));
        String status = statusRaw;
        if (status == null || status.isBlank()) {
            status = "con_hang";
        }
        material.setTrangThai(status.trim());
        material.setSoLuongTon(toBigDecimal(stockRaw));
        material.setSoLuongToiThieu(toBigDecimal(minStockRaw));
        material.setGiaNhap(toBigDecimal(importPriceRaw));
        material.setNhaCungCapId(supplierId > 0 ? supplierId : null);
        return material;
    }

    public String validate(NguyenVatLieu material, int id) {
        if (material.getTenNguyenLieu() == null || material.getTenNguyenLieu().isBlank()) {
            return "Vui lòng nhập tên nguyên liệu.";
        }
        if (id > 0 && (material.getMaNguyenLieu() == null || material.getMaNguyenLieu().isBlank())) {
            return "Vui lòng nhập mã nguyên liệu.";
        }
        return null;
    }

    public Optional<String> saveMaterial(NguyenVatLieu material, int id) throws SQLException {
        if (id == 0) {
            String code = material.getMaNguyenLieu();
            if (code == null || code.isBlank()) {
                material.setMaNguyenLieu("NL" + MA_NGUYEN_LIEU_TIME.format(LocalDateTime.now()));
            }
        }

        String error = validate(material, id);
        if (error != null) {
            return Optional.of(error);
        }

        if (id > 0) {
            material.setId(id);
            nguyenVatLieuDao.update(material);
            return Optional.empty();
        }

        if (material.getSoLuongToiThieu() == null
                || material.getSoLuongToiThieu().compareTo(BigDecimal.ZERO) <= 0) {
            material.setSoLuongToiThieu(settingsService.getLowStockThreshold());
        }
        nguyenVatLieuDao.insert(material);
        return Optional.empty();
    }

    public void deleteMaterial(int id) throws SQLException {
        nguyenVatLieuDao.delete(id);
    }

    public static String buildListQuery(String keyword, String category) {
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasLoai = category != null && !category.isBlank();
        if (!hasKw && !hasLoai) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        if (hasKw) {
            sb.append("kw=").append(URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8));
        }
        if (hasLoai) {
            if (hasKw) {
                sb.append('&');
            }
            sb.append("loai=").append(URLEncoder.encode(category.trim(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static Map<String, String> createDanhMucNlLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("tra", "Trà");
        labels.put("mứt", "Mứt");
        labels.put("thạch", "Thạch");
        labels.put("sữa", "Sữa");
        labels.put("đường", "Đường");
        labels.put("topping", "Topping");
        labels.put("ly_cốc", "Ly/cốc");
        labels.put("khác", "Khác");
        return Collections.unmodifiableMap(labels);
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

    public record MaterialPageData(List<NguyenVatLieu> materials, List<?> suppliers) {
    }
}
