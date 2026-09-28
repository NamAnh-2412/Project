package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.dao.KiemKhoDao;
import vn.quanlybanhang.dao.NguyenVatLieuDao;
import vn.quanlybanhang.model.KiemKho;
import vn.quanlybanhang.model.KiemKhoChiTiet;
import vn.quanlybanhang.model.NguyenVatLieu;

public class KiemKhoService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final KiemKhoDao kiemKhoDao = new KiemKhoDao();
    private final NguyenVatLieuDao nguyenVatLieuDao = new NguyenVatLieuDao();

    public Optional<KiemKho> findById(int id) throws SQLException {
        kiemKhoDao.ensureTables();
        return kiemKhoDao.findById(id);
    }

    public List<NguyenVatLieu> listMaterialsForAudit() throws SQLException {
        kiemKhoDao.ensureTables();
        return nguyenVatLieuDao.findForAudit();
    }

    public KiemKho bindPhieu(int employeeId, String note, String[] materialIds, String[] actualQty, String[] reasons) {
        KiemKho phieu = new KiemKho();
        phieu.setNhanVienKiem(employeeId);
        phieu.setGhiChu(trimOrNull(note));
        phieu.setItems(bindItems(materialIds, actualQty, reasons));
        return phieu;
    }

    public List<KiemKhoChiTiet> bindItems(String[] materialIds, String[] actualQty, String[] reasons) {
        List<KiemKhoChiTiet> items = new ArrayList<>();
        if (materialIds == null) {
            return items;
        }
        for (int i = 0; i < materialIds.length; i++) {
            int materialId = parseInt(materialIds[i]);
            if (materialId <= 0) {
                continue;
            }
            String qtyValue = safeValue(actualQty, i);
            if (qtyValue == null || qtyValue.isBlank()) {
                continue;
            }
            KiemKhoChiTiet item = new KiemKhoChiTiet();
            item.setNguyenLieuId(materialId);
            item.setTonThucTe(toBigDecimal(qtyValue));
            item.setLyDo(safeValue(reasons, i));
            items.add(item);
        }
        return items;
    }

    public String generateCode() {
        return "PK" + LocalDateTime.now().format(CODE_FORMATTER);
    }

    public void createPhieu(KiemKho phieu) throws SQLException {
        if (phieu.getItems() == null || phieu.getItems().isEmpty()) {
            throw new SQLException("Vui lòng nhập ít nhất một mặt hàng kiểm kho.");
        }
        phieu.setMaPhieuKiem(generateCode());
        phieu.setNgayKiem(LocalDate.now());

        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int kiemKhoId = kiemKhoDao.insertPhieu(connection, phieu);
                for (KiemKhoChiTiet item : phieu.getItems()) {
                    BigDecimal tonHeThong = kiemKhoDao.getCurrentStock(connection, item.getNguyenLieuId());
                    BigDecimal tonThucTe = item.getTonThucTe() == null ? BigDecimal.ZERO : item.getTonThucTe();
                    item.setTonHeThong(tonHeThong);
                    item.setChenhLech(tonThucTe.subtract(tonHeThong));
                    kiemKhoDao.insertChiTiet(connection, kiemKhoId, item);
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

    public void approvePhieu(KiemKho phieu) throws SQLException {
        if (!"cho_duyet".equals(phieu.getTrangThai())) {
            throw new SQLException("Phiếu này không ở trạng thái chờ duyệt.");
        }
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (KiemKhoChiTiet item : phieu.getItems()) {
                    nguyenVatLieuDao.setStock(connection, item.getNguyenLieuId(), item.getTonThucTe());
                }
                kiemKhoDao.updateStatus(connection, phieu.getId(), "da_duyet");
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void cancelPhieu(KiemKho phieu) throws SQLException {
        if (!"cho_duyet".equals(phieu.getTrangThai())) {
            throw new SQLException("Chỉ có thể hủy phiếu đang chờ duyệt.");
        }
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
            kiemKhoDao.updateStatus(connection, phieu.getId(), "da_huy");
        }
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
}
