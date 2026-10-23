package btl.vexemphim.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyVoucherRequest(@NotBlank String code) {
}
