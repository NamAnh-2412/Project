package btl.vexemphim.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PosShiftResponse(
        Long id,
        String status,
        BigDecimal openingCash,
        BigDecimal cashSales,
        BigDecimal cardSales,
        BigDecimal snackSales,
        int ticketCount,
        BigDecimal expectedCash,
        BigDecimal actualCash,
        BigDecimal difference,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
}
