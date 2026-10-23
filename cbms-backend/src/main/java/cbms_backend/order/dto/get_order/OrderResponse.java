package cbms_backend.order.dto.get_order;

import cbms_backend.customer.dto.get_customer.CustomerAddressResponse;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        CustomerResponse customer,
        CustomerAddressResponse customerAddress,
        OrderStatus status,
        Instant createdAt,
        List<CurtainResponse> curtains,
        List<OrderAssignmentResponse> assignments
) {
    public static OrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(
                order.getId(),
                order.getCustomer() != null ? CustomerResponse.fromEntity(order.getCustomer()) : null,
                order.getCustomerAddress() != null ? CustomerAddressResponse.fromEntity(order.getCustomerAddress()) : null,
                order.getStatus(),
                order.getCreatedAt(),
                order.getCurtains() != null ? order.getCurtains().stream().map(CurtainResponse::fromEntity).toList() : List.of(),
                order.getAssignments() != null ? order.getAssignments().stream().map(OrderAssignmentResponse::fromEntity).toList() : List.of()
        );
    }
}
