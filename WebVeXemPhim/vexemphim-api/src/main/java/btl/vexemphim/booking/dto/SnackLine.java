package btl.vexemphim.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SnackLine(@NotNull Long productId, @NotNull @Min(1) Integer qty) {
}
