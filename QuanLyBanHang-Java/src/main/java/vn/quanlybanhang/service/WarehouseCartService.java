package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import vn.quanlybanhang.dao.NguyenVatLieuDao;
import vn.quanlybanhang.model.NguyenVatLieu;
import vn.quanlybanhang.model.NhapKho;
import vn.quanlybanhang.model.NhapKhoItem;

import jakarta.servlet.http.HttpSession;

public class WarehouseCartService {

    public static final String SESSION_KEY = "kho_cart";

    private final NguyenVatLieuDao nguyenVatLieuDao = new NguyenVatLieuDao();
    private final WarehouseService warehouseService = new WarehouseService();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCart(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (value instanceof List<?> list) {
            List<Map<String, Object>> cart = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    cart.add((Map<String, Object>) map);
                }
            }
            return cart;
        }
        return new ArrayList<>();
    }

    public void saveCart(HttpSession session, List<Map<String, Object>> cart) {
        session.setAttribute(SESSION_KEY, cart);
    }

    public Map<String, Object> addItem(HttpSession session, int materialId, BigDecimal qty, BigDecimal donGia, LocalDate hanSuDung)
            throws Exception {
        if (materialId <= 0 || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }

        Optional<NguyenVatLieu> materialOpt = nguyenVatLieuDao.findById(materialId);
        if (materialOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy mặt hàng");
        }
        NguyenVatLieu material = materialOpt.get();

        BigDecimal unitPrice = donGia == null || donGia.compareTo(BigDecimal.ZERO) <= 0
                ? (material.getGiaNhap() == null ? BigDecimal.ZERO : material.getGiaNhap())
                : donGia;

        List<Map<String, Object>> cart = getCart(session);
        boolean found = false;
        for (Map<String, Object> existing : cart) {
            int existingId = ((Number) existing.get("id")).intValue();
            BigDecimal existingPrice = toBigDecimal(existing.get("don_gia"));
            String existingExpiry = existing.get("han_su_dung") == null ? null : String.valueOf(existing.get("han_su_dung"));
            String newExpiry = hanSuDung == null ? null : hanSuDung.toString();
            if (existingId == materialId
                    && existingPrice.compareTo(unitPrice) == 0
                    && java.util.Objects.equals(existingExpiry, newExpiry)) {
                BigDecimal currentQty = toBigDecimal(existing.get("qty"));
                existing.put("qty", currentQty.add(qty));
                found = true;
                break;
            }
        }

        if (!found) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", material.getId());
            item.put("name", material.getTenNguyenLieu());
            item.put("don_vi", material.getDonViTinh());
            item.put("qty", qty);
            item.put("don_gia", unitPrice);
            item.put("han_su_dung", hanSuDung == null ? null : hanSuDung.toString());
            item.put("current", material.getSoLuongTon() == null ? BigDecimal.ZERO : material.getSoLuongTon());
            cart.add(item);
        }

        saveCart(session, cart);
        return Map.of("success", true, "cart", cart);
    }

    public Map<String, Object> removeItem(HttpSession session, int materialId) {
        List<Map<String, Object>> cart = getCart(session);
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : cart) {
            if (((Number) item.get("id")).intValue() != materialId) {
                filtered.add(item);
            }
        }
        saveCart(session, filtered);
        return Map.of("success", true, "cart", filtered);
    }

    public Map<String, Object> commitCart(HttpSession session, Integer supplierId, String ghiChu, Integer employeeId)
            throws Exception {
        List<Map<String, Object>> cart = getCart(session);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }
        if (employeeId == null) {
            throw new IllegalStateException("Không xác định được nhân viên");
        }

        NhapKho receipt = new NhapKho();
        receipt.setMaPhieuNhap(warehouseService.generateReceiptCode());
        receipt.setNhaCungCapId(supplierId);
        receipt.setNhanVienNhap(employeeId);
        receipt.setNgayNhap(LocalDate.now().atStartOfDay());
        receipt.setTrangThai("cho_duyet");
        receipt.setGhiChu(ghiChu);
        receipt.setItems(toReceiptItems(cart));
        receipt.setTongTien(warehouseService.calculateTotal(receipt.getItems()));

        warehouseService.createReceipt(receipt);
        saveCart(session, new ArrayList<>());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("ma_phieu_nhap", receipt.getMaPhieuNhap());
        return result;
    }

    private List<NhapKhoItem> toReceiptItems(List<Map<String, Object>> cart) {
        List<NhapKhoItem> items = new ArrayList<>();
        for (Map<String, Object> entry : cart) {
            NhapKhoItem item = new NhapKhoItem();
            item.setNguyenVatLieuId(((Number) entry.get("id")).intValue());
            item.setTenNguyenVatLieu(String.valueOf(entry.get("name")));
            item.setSoLuongNhap(toBigDecimal(entry.get("qty")));
            item.setDonGia(toBigDecimal(entry.get("don_gia")));
            item.setDonViTinh(entry.get("don_vi") == null ? null : String.valueOf(entry.get("don_vi")));
            Object expiry = entry.get("han_su_dung");
            if (expiry != null && !String.valueOf(expiry).isBlank()) {
                item.setHanSuDung(LocalDate.parse(String.valueOf(expiry)));
            }
            item.setThanhTien(item.getDonGia().multiply(item.getSoLuongNhap()));
            items.add(item);
        }
        return items;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
