package btl.vexemphim.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherRequest(
        @NotBlank String code,
        @NotBlank String discountType,
        @NotNull BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        @NotNull Integer usageLimit,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
