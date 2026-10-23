package cbms_backend.invoice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInvoiceRequest(
        @DecimalMin(value = "0.0", message = "Discount must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Discount must be between 0 and 100")
        Double discount,

        @PositiveOrZero(message = "Custom price must be greater than or equal to 0")
        Double customPrice
) {
    public UpdateInvoiceRequest(Double discount) {
        this(discount, null);
    }
}
