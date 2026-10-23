package cbms_backend.material.dto.create_stock_out;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateStockOutRequest(
        @NotNull(message = "Material ID must not be null")
        UUID materialId,

        @NotNull(message = "Order ID must not be null")
        UUID orderId,

        @NotNull(message = "Quantity must not be null")
        @Positive(message = "Quantity must be greater than 0")
        Double quantity
) {}
