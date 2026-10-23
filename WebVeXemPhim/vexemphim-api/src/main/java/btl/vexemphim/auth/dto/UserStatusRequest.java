package btl.vexemphim.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(@NotNull Boolean enabled) {
}
