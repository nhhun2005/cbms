package cbms_backend.order.dto.curtain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCurtainRequest(
        @NotNull(message = "Width must not be null")
        @Positive(message = "Width must be greater than 0")
        Double width,

        @NotNull(message = "Height must not be null")
        @Positive(message = "Height must be greater than 0")
        Double height
) {}
