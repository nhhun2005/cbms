package cbms_backend.invoice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull(message = "Order ID must not be null")
        UUID orderId,

        @DecimalMin(value = "0.0", message = "Discount must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Discount must be between 0 and 100")
        Double discount
) {
    public CreateInvoiceRequest(UUID orderId) {
        this(orderId, 0.0);
    }
}
