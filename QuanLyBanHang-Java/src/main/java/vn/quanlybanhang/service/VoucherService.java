package vn.quanlybanhang.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import vn.quanlybanhang.dao.VoucherDao;
import vn.quanlybanhang.model.MaGiamGia;

public class VoucherService {

    public static final String TYPE_AMOUNT = "tien";
    public static final String TYPE_PERCENT = "phan_tram";
    public static final String STATUS_ACTIVE = "hoat_dong";
    public static final String STATUS_STOPPED = "ngung";

    private final VoucherDao voucherDao = new VoucherDao();

    public record Quote(String code, BigDecimal discount) {
    }

    public List<MaGiamGia> list() throws SQLException {
        return voucherDao.findAll();
    }

    public Quote preview(String rawCode, BigDecimal gross) throws SQLException {
        String code = normalizeCode(rawCode);
        if (code.isBlank()) {
            return new Quote("", BigDecimal.ZERO);
        }
        MaGiamGia voucher = voucherDao.findByCode(code)
                .orElseThrow(() -> new SQLException("Mã giảm giá không tồn tại."));
        return new Quote(voucher.getMa(), discountOf(voucher, gross, LocalDate.now()));
    }

    public BigDecimal consume(Connection connection, String rawCode, BigDecimal gross) throws SQLException {
        String code = normalizeCode(rawCode);
        if (code.isBlank()) {
            return BigDecimal.ZERO;
        }
        MaGiamGia voucher = voucherDao.lockByCode(connection, code)
                .orElseThrow(() -> new SQLException("Mã giảm giá không tồn tại."));
        BigDecimal discount = discountOf(voucher, gross, LocalDate.now());
        voucherDao.incrementUsed(connection, voucher.getId());
        return discount;
    }

    public void release(Connection connection, String rawCode) throws SQLException {
        String code = normalizeCode(rawCode);
        if (!code.isBlank()) {
            voucherDao.decrementUsed(connection, code);
        }
    }

    public Optional<String> create(String rawCode, String type, BigDecimal value, BigDecimal minOrder,
            BigDecimal maxDiscount, Integer maxUses, String start, String end) throws SQLException {
        String code = normalizeCode(rawCode);
        if (code.length() < 3 || code.length() > 40) {
            return Optional.of("Mã giảm giá cần từ 3 đến 40 ký tự.");
        }
        if (!TYPE_AMOUNT.equals(type) && !TYPE_PERCENT.equals(type)) {
            return Optional.of("Loại giảm giá không hợp lệ.");
        }
        if (value == null || value.signum() <= 0) {
            return Optional.of("Giá trị giảm phải lớn hơn 0.");
        }
        if (TYPE_PERCENT.equals(type) && value.compareTo(new BigDecimal("100")) > 0) {
            return Optional.of("Phần trăm giảm không được quá 100.");
        }
        if (voucherDao.findByCode(code).isPresent()) {
            return Optional.of("Mã giảm giá đã tồn tại.");
        }
        MaGiamGia voucher = new MaGiamGia();
        voucher.setMa(code);
        voucher.setLoai(type);
        voucher.setGiaTri(value);
        voucher.setDonToiThieu(minOrder == null || minOrder.signum() < 0 ? BigDecimal.ZERO : minOrder);
        voucher.setGiamToiDa(maxDiscount != null && maxDiscount.signum() > 0 ? maxDiscount : null);
        voucher.setSoLanToiDa(maxUses != null && maxUses > 0 ? maxUses : null);
        voucher.setBatDau(VoucherDao.parseDate(start));
        voucher.setKetThuc(VoucherDao.parseDate(end));
        if (voucher.getBatDau() != null && voucher.getKetThuc() != null
                && voucher.getKetThuc().isBefore(voucher.getBatDau())) {
            return Optional.of("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        voucherDao.insert(voucher);
        return Optional.empty();
    }

    public void toggle(int id, String currentStatus) throws SQLException {
        voucherDao.setStatus(id, STATUS_ACTIVE.equals(currentStatus) ? STATUS_STOPPED : STATUS_ACTIVE);
    }

    public static String normalizeCode(String rawCode) {
        if (rawCode == null) {
            return "";
        }
        return rawCode.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private BigDecimal discountOf(MaGiamGia voucher, BigDecimal gross, LocalDate today) throws SQLException {
        BigDecimal total = gross == null ? BigDecimal.ZERO : gross;
        if (!STATUS_ACTIVE.equals(voucher.getTrangThai())) {
            throw new SQLException("Mã giảm giá đã ngừng dùng.");
        }
        if (voucher.getBatDau() != null && today.isBefore(voucher.getBatDau())) {
            throw new SQLException("Mã giảm giá chưa đến ngày dùng.");
        }
        if (voucher.getKetThuc() != null && today.isAfter(voucher.getKetThuc())) {
            throw new SQLException("Mã giảm giá đã hết hạn.");
        }
        if (voucher.getSoLanToiDa() != null && voucher.getSoLanDaDung() >= voucher.getSoLanToiDa()) {
            throw new SQLException("Mã giảm giá đã hết lượt dùng.");
        }
        BigDecimal minimum = voucher.getDonToiThieu() == null ? BigDecimal.ZERO : voucher.getDonToiThieu();
        if (total.compareTo(minimum) < 0) {
            throw new SQLException("Đơn chưa đạt mức tối thiểu " + minimum.toPlainString() + "đ.");
        }
        BigDecimal discount;
        if (TYPE_PERCENT.equals(voucher.getLoai())) {
            discount = total.multiply(voucher.getGiaTri()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            if (voucher.getGiamToiDa() != null && discount.compareTo(voucher.getGiamToiDa()) > 0) {
                discount = voucher.getGiamToiDa();
            }
        } else {
            discount = voucher.getGiaTri();
        }
        if (discount.compareTo(total) > 0) {
            discount = total;
        }
        return discount.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
    }
}
