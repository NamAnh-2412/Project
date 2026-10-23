package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.VoucherOffer;
import btl.vexemphim.booking.dto.VoucherRequest;
import btl.vexemphim.booking.entity.Reservation;
import btl.vexemphim.booking.entity.Voucher;
import btl.vexemphim.booking.repository.VoucherRepository;
import btl.vexemphim.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> list() {
        return voucherRepository.findAll();
    }

    public List<VoucherOffer> available(BigDecimal orderAmount) {
        BigDecimal gross = orderAmount == null ? BigDecimal.ZERO : orderAmount;
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAll().stream()
                .filter(v -> usable(v, gross, now))
                .map(v -> new VoucherOffer(v.getCode(), v.getDiscountType(), v.getDiscountValue(),
                        v.getMinOrderAmount(), discountOf(v, gross)))
                .toList();
    }

    @Transactional
    public Voucher create(VoucherRequest req) {
        String code = req.code().trim().toUpperCase();
        String type = req.discountType().trim().toUpperCase();
        if (!"AMOUNT".equals(type) && !"PERCENT".equals(type)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Loại giảm giá phải là AMOUNT hoặc PERCENT");
        }
        if (req.usageLimit() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Số lượt phải lớn hơn 0");
        }
        if (voucherRepository.findByCode(code).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "VALIDATION", "Mã đã tồn tại");
        }
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(type);
        voucher.setDiscountValue(req.discountValue());
        voucher.setMinOrderAmount(req.minOrderAmount() == null ? BigDecimal.ZERO : req.minOrderAmount());
        voucher.setMaxDiscountAmount(req.maxDiscountAmount());
        voucher.setUsageLimit(req.usageLimit());
        voucher.setUsedCount(0);
        voucher.setStartAt(req.startAt());
        voucher.setEndAt(req.endAt());
        voucher.setStatus("ACTIVE");
        return voucherRepository.save(voucher);
    }

    public void apply(Reservation res, String rawCode, BigDecimal gross) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        if (code.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VOUCHER_INVALID", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
        }
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "VOUCHER_INVALID", "Mã giảm giá không hợp lệ hoặc đã hết hạn"));
        if (!usable(voucher, gross, LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VOUCHER_INVALID", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
        }
        res.setVoucherId(voucher.getId());
        res.setVoucherCode(voucher.getCode());
        res.setDiscountAmount(discountOf(voucher, gross));
    }

    /** Giữ mã nếu còn hợp lệ sau khi đổi bắp nước. Hết điều kiện thì bỏ mã, trả giá gốc. */
    public void reprice(Reservation res, BigDecimal gross) {
        if (res.getVoucherId() == null) {
            res.setDiscountAmount(BigDecimal.ZERO);
            res.setTotalAmount(gross);
            return;
        }
        Voucher voucher = voucherRepository.findById(res.getVoucherId()).orElse(null);
        if (voucher == null || !usable(voucher, gross, LocalDateTime.now())) {
            clear(res);
            res.setTotalAmount(gross);
            return;
        }
        BigDecimal discount = discountOf(voucher, gross);
        res.setDiscountAmount(discount);
        res.setTotalAmount(gross.subtract(discount));
    }

    public void clear(Reservation res) {
        res.setVoucherId(null);
        res.setVoucherCode(null);
        res.setDiscountAmount(BigDecimal.ZERO);
    }

    public int consume(Long voucherId) {
        if (voucherId == null) {
            return 1;
        }
        return voucherRepository.consumeIfAvailable(voucherId);
    }

    private static boolean usable(Voucher voucher, BigDecimal gross, LocalDateTime now) {
        if (!"ACTIVE".equals(voucher.getStatus())) {
            return false;
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return false;
        }
        if (voucher.getStartAt() != null && voucher.getStartAt().isAfter(now)) {
            return false;
        }
        if (voucher.getEndAt() != null && !voucher.getEndAt().isAfter(now)) {
            return false;
        }
        BigDecimal min = voucher.getMinOrderAmount() == null ? BigDecimal.ZERO : voucher.getMinOrderAmount();
        return gross.compareTo(min) >= 0;
    }

    private static BigDecimal discountOf(Voucher voucher, BigDecimal gross) {
        BigDecimal discount;
        if ("PERCENT".equals(voucher.getDiscountType())) {
            discount = gross.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            discount = voucher.getDiscountValue();
        }
        if (voucher.getMaxDiscountAmount() != null && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discount = voucher.getMaxDiscountAmount();
        }
        if (discount.compareTo(gross) > 0) {
            discount = gross;
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return discount;
    }
}
