package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.dao.AuditDao;
import vn.quanlybanhang.dao.KiemKhoDao;
import vn.quanlybanhang.dao.NguyenVatLieuDao;
import vn.quanlybanhang.dao.SupplierDao;
import vn.quanlybanhang.dao.WarehouseDao;
import vn.quanlybanhang.model.KiemKho;
import vn.quanlybanhang.model.NguyenVatLieu;
import vn.quanlybanhang.model.NhapKho;
import vn.quanlybanhang.model.NhapKhoItem;
import vn.quanlybanhang.model.Supplier;

public class WarehouseService {

    private static final DateTimeFormatter RECEIPT_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final NguyenVatLieuDao nguyenVatLieuDao = new NguyenVatLieuDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final KiemKhoDao kiemKhoDao = new KiemKhoDao();
    private final MaterialService materialService = new MaterialService();
    private final AuditDao auditDao = new AuditDao();
    private final Random random = new Random();

    public WarehouseContext prepareContext() throws SQLException {
        warehouseDao.ensureExpiryTables();
        return new WarehouseContext(warehouseDao.tableExists("kho_lot"), warehouseDao.getExpiryStats());
    }

    public PhieuTabData loadPhieuTab(String status, String from, String to, String expiryFilter) throws SQLException {
        String filter = expiryFilter == null || expiryFilter.isBlank() ? "all" : expiryFilter;
        return new PhieuTabData(
                warehouseDao.search(status, from, to),
                filter,
                warehouseDao.getExpiryLots(filter, from, to));
    }

    public TonKhoTabData loadTonKhoTab(String keyword, String category) throws SQLException {
        List<NguyenVatLieu> materials = nguyenVatLieuDao.findInStock(keyword, category);
        int lowStock5 = 0;
        int lowStock10 = 0;
        for (NguyenVatLieu material : materials) {
            BigDecimal ton = material.getSoLuongTon() == null ? BigDecimal.ZERO : material.getSoLuongTon();
            if (ton.compareTo(BigDecimal.valueOf(5)) < 0) {
                lowStock5++;
            } else if (ton.compareTo(BigDecimal.valueOf(10)) < 0) {
                lowStock10++;
            }
        }
        return new TonKhoTabData(materials, lowStock5, lowStock10, materialService.getDanhMucNlLabels());
    }

    public KiemKhoTabData loadKiemKhoTab(String status, String from, String to) throws SQLException {
        kiemKhoDao.ensureTables();
        return new KiemKhoTabData(kiemKhoDao.search(status, from, to), kiemKhoDao.getStats());
    }

    public CreateReceiptPageData loadCreatePage() throws SQLException {
        return new CreateReceiptPageData(nguyenVatLieuDao.findAll(), supplierDao.search(null, "dang_hop_tac"));
    }

    public Optional<NhapKho> findReceiptById(int id) throws SQLException {
        return warehouseDao.findById(id);
    }

    public NhapKho buildReceipt(int supplierId, Integer employeeId, String note, List<NhapKhoItem> items) {
        NhapKho receipt = new NhapKho();
        receipt.setMaPhieuNhap(generateReceiptCode());
        receipt.setNhaCungCapId(supplierId > 0 ? supplierId : null);
        receipt.setNhanVienNhap(employeeId);
        receipt.setNgayNhap(LocalDate.now().atStartOfDay());
        receipt.setTrangThai("cho_duyet");
        receipt.setGhiChu(trimOrNull(note));
        receipt.setItems(items);
        receipt.setTongTien(calculateTotal(items));
        return receipt;
    }

    public List<NhapKhoItem> bindReceiptItems(String[] materialIds, String[] quantities, String[] prices,
                                              String[] expiredDates) {
        List<NhapKhoItem> items = new ArrayList<>();
        if (materialIds == null) {
            return items;
        }
        for (int i = 0; i < materialIds.length; i++) {
            int materialId = parseInt(materialIds[i]);
            BigDecimal quantity = toBigDecimal(safeValue(quantities, i));
            BigDecimal price = toBigDecimal(safeValue(prices, i));
            if (materialId <= 0 || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            NhapKhoItem item = new NhapKhoItem();
            item.setNguyenVatLieuId(materialId);
            item.setSoLuongNhap(quantity);
            item.setDonGia(price);
            item.setThanhTien(price.multiply(quantity));
            String expired = safeValue(expiredDates, i);
            if (expired != null && !expired.isBlank()) {
                item.setHanSuDung(LocalDate.parse(expired.trim()));
            }
            items.add(item);
        }
        return items;
    }

    public Optional<String> createReceiptFromForm(int supplierId, Integer employeeId, String note,
                                                  String[] materialIds, String[] quantities, String[] prices,
                                                  String[] expiredDates) throws SQLException {
        List<NhapKhoItem> items = bindReceiptItems(materialIds, quantities, prices, expiredDates);
        if (items.isEmpty()) {
            return Optional.of("Vui lòng chọn ít nhất một nguyên liệu.");
        }
        createReceipt(buildReceipt(supplierId, employeeId, note, items));
        return Optional.empty();
    }

    public String generateReceiptCode() {
        return "PN" + LocalDateTime.now().format(RECEIPT_FORMATTER) + (100 + random.nextInt(900));
    }

    public BigDecimal calculateTotal(List<NhapKhoItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (NhapKhoItem item : items) {
            BigDecimal amount = item.getThanhTien() != null
                    ? item.getThanhTien()
                    : item.getDonGia().multiply(item.getSoLuongNhap());
            total = total.add(amount);
        }
        return total;
    }

    public void createReceipt(NhapKho receipt) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int receiptId = warehouseDao.insertReceipt(connection, receipt);
                for (NhapKhoItem item : receipt.getItems()) {
                    item.setThanhTien(item.getDonGia().multiply(item.getSoLuongNhap()));
                    int detailId = warehouseDao.insertReceiptItem(connection, receiptId, item);
                    item.setId(detailId);
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void approveReceipt(NhapKho receipt, Integer employeeId) throws SQLException {
        if (!"cho_duyet".equals(receipt.getTrangThai())) {
            throw new SQLException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt.");
        }
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean hasKhoLot = warehouseDao.tableExists("kho_lot");
                boolean logImportHistory = auditDao.tableExists("lich_su_nhap");
                for (NhapKhoItem item : receipt.getItems()) {
                    if (hasKhoLot) {
                        warehouseDao.insertKhoLot(connection, receipt.getId(), item);
                    }
                    nguyenVatLieuDao.increaseStock(connection, item.getNguyenVatLieuId(), item.getSoLuongNhap());
                    if (logImportHistory) {
                        auditDao.logImportHistory(connection, employeeId, item.getNguyenVatLieuId(), item.getSoLuongNhap());
                    }
                }
                warehouseDao.updateStatus(connection, receipt.getId(), "da_duyet");
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void cancelReceipt(int receiptId) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            NhapKho receipt = warehouseDao.findById(receiptId)
                    .orElseThrow(() -> new SQLException("Không tìm thấy phiếu nhập."));
            if (!"cho_duyet".equals(receipt.getTrangThai())) {
                throw new SQLException("Chỉ có thể hủy phiếu đang ở trạng thái chờ duyệt.");
            }
            warehouseDao.updateStatus(connection, receiptId, "da_huy");
        }
    }

    public void cancelExpiredLot(int lotId, int employeeId, String reason) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                Map<String, Object> lot = warehouseDao.findExpiryLotById(connection, lotId)
                        .orElseThrow(() -> new SQLException("Không tìm thấy lô nguyên liệu hoặc lô đã bị hủy."));
                if ("da_huy".equals(lot.get("trang_thai"))) {
                    throw new SQLException("Lô nguyên liệu này đã bị hủy trước đó.");
                }
                BigDecimal remain = (BigDecimal) lot.get("so_luong_con_lai");
                if (remain == null || remain.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new SQLException("Số lượng còn lại đã bằng 0.");
                }

                nguyenVatLieuDao.decreaseStock(connection, ((Number) lot.get("nguyenlieu_id")).intValue(), remain);
                String normalizedReason = reason == null || reason.isBlank() ? "Hết hạn sử dụng" : reason.trim();
                warehouseDao.markLotCancelled(connection, lotId, employeeId, normalizedReason);
                if (auditDao.tableExists("lich_su_huy")) {
                    auditDao.logDisposalHistory(connection, lotId, ((Number) lot.get("nguyenlieu_id")).intValue(),
                            remain, employeeId, normalizedReason);
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Tao ban ghi {@code expiry_alerts} giong luong PHP {@code scripts/check_expiry_alerts.php}:
     * {@code 7_ngay} (HSD trong vong 7 ngay ke tu hom nay), {@code 1_ngay} (HSD = ngay mai),
     * {@code het_han} (HSD truoc hom nay). Cron PHP: {@code 0 8 * * * php .../check_expiry_alerts.php};
     * ben Java co the goi POST {@code /api/kho/check-expiry-alerts} sau khi dang nhap (xem checklist).
     */
    public int syncExpiryAlerts() throws SQLException {
        if (!warehouseDao.tableExists("kho_lot") || !warehouseDao.tableExists("expiry_alerts")) {
            throw new SQLException("Cần có bảng kho_lot và expiry_alerts để tạo cảnh báo.");
        }

        SettingsService settingsService = new SettingsService();
        int alertDays7 = settingsService.getExpiryAlertDays7();
        int alertDays1 = settingsService.getExpiryAlertDays1();

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(alertDays7);
        LocalDate oneDayLater = today.plusDays(alertDays1);
        int inserted = 0;

        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                inserted += insertMissingAlerts(connection, "7_ngay", today, sevenDaysLater, today);
                inserted += insertMissingAlerts(connection, "1_ngay", today, oneDayLater, today);
                inserted += insertMissingAlerts(connection, "het_han", today, today, today);
                connection.commit();
                return inserted;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private int insertMissingAlerts(Connection connection, String type, LocalDate today, LocalDate compareDate, LocalDate alertDate)
            throws SQLException {
        int inserted = 0;
        for (Map<String, Object> lot : warehouseDao.findLotsForAlert(connection, type, today, compareDate)) {
            int lotId = ((Number) lot.get("id")).intValue();
            if (!warehouseDao.expiryAlertExists(connection, lotId, type, alertDate)) {
                warehouseDao.insertExpiryAlert(connection, lotId, ((Number) lot.get("nguyenlieu_id")).intValue(), type, alertDate);
                inserted++;
            }
        }
        return inserted;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static String safeValue(String[] values, int index) {
        if (values == null || index >= values.length) {
            return null;
        }
        return values[index];
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record WarehouseContext(boolean hasKhoLotTable, Map<String, ?> expiryStats) {
    }

    public record PhieuTabData(List<NhapKho> receipts, String expiryFilter, List<Map<String, Object>> expiryLots) {
    }

    public record TonKhoTabData(List<NguyenVatLieu> tonKhoList, int lowStock5, int lowStock10,
                                Map<String, String> danhMucMap) {
    }

    public record KiemKhoTabData(List<KiemKho> kiemKhoList, Map<String, ?> kiemKhoStats) {
    }

    public record CreateReceiptPageData(List<NguyenVatLieu> materials, List<Supplier> suppliers) {
    }
}
