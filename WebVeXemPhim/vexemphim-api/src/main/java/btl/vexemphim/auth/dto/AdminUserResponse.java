package btl.vexemphim.auth.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {
}
