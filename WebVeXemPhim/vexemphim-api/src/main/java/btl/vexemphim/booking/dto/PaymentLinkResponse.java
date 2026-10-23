package btl.vexemphim.booking.dto;

import java.math.BigDecimal;

public record PaymentLinkResponse(
        Long paymentId,
        Long orderCode,
        BigDecimal amount,
        String checkoutUrl,
        String qrCode
) {
}
