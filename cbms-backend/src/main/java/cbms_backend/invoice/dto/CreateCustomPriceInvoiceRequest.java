package cbms_backend.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateCustomPriceInvoiceRequest(
        @NotNull(message = "Order ID must not be null")
        UUID orderId,

        @NotNull(message = "Custom price must not be null")
        @PositiveOrZero(message = "Custom price must be greater than or equal to 0")
        Double customPrice
) {}
