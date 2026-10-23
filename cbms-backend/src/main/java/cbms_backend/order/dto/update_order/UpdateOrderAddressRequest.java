package cbms_backend.order.dto.update_order;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateOrderAddressRequest(
        @NotNull(message = "Customer address ID must not be null")
        UUID customerAddressId
) {}
