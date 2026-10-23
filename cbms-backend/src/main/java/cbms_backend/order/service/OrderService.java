package cbms_backend.order.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.create_order.CreateOrderRequest;
import cbms_backend.order.dto.get_order.OrderResponse;
import cbms_backend.order.dto.update_order.UpdateOrderAddressRequest;
import cbms_backend.order.dto.update_order.UpdateOrderStatusRequest;
import cbms_backend.order.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    ApiResponse<OrderResponse> createOrder(CreateOrderRequest request);
    ApiResponse<OrderResponse> getOrderById(UUID id);
    ApiResponse<List<OrderResponse>> getAllOrders(OrderStatus status, UUID customerId, UUID userId);
    ApiResponse<OrderResponse> updateOrderStatus(UUID id, UpdateOrderStatusRequest request);
    ApiResponse<OrderResponse> updateOrderAddress(UUID id, UpdateOrderAddressRequest request);
    ApiResponse<Void> deleteOrder(UUID id);
}
