package btl.vexemphim.booking.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long movieId,
        String username,
        int rating,
        String comment,
        LocalDateTime createdAt,
        boolean mine
) {
}
