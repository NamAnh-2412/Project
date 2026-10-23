package btl.vexemphim.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SnackSaleResponse(
        Long id,
        BigDecimal totalAmount,
        String detail,
        LocalDateTime createdAt
) {
}
