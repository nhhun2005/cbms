package cbms_backend.material.dto.create_stock_in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateStockInRequest(
        @NotNull(message = "Material ID must not be null")
        UUID materialId,

        UUID supplierId,

        @NotNull(message = "Quantity must not be null")
        @Positive(message = "Quantity must be greater than 0")
        Double quantity
) {
    public CreateStockInRequest(UUID materialId, Double quantity) {
        this(materialId, null, quantity);
    }
}
