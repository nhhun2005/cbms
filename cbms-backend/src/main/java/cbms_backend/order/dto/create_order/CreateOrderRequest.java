package cbms_backend.order.dto.create_order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Customer ID must not be null")
        UUID customerId,

        @NotNull(message = "Customer address ID must not be null")
        UUID customerAddressId,

        @NotNull(message = "Creator user ID must not be null")
        UUID creatorUserId,

        @NotEmpty(message = "Order must contain at least one curtain")
        @Valid
        List<CurtainItemRequest> curtains
) {}
