package cbms_backend.order.dto.update_order;

import cbms_backend.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status must not be null")
        OrderStatus status
) {}
