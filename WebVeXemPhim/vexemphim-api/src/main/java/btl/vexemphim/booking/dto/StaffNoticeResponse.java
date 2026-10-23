package btl.vexemphim.booking.dto;

import java.time.LocalDateTime;

public record StaffNoticeResponse(
        Long id,
        String title,
        String body,
        Long reservationId,
        LocalDateTime createdAt
) {
}
