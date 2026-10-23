package btl.vexemphim.booking.dto;

import java.math.BigDecimal;

public record VoucherOffer(
        String code,
        String discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal discountAmount
) {
}
