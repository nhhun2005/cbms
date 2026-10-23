package cbms_backend.order.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.create_order.CreateOrderRequest;
import cbms_backend.order.dto.get_order.OrderResponse;
import cbms_backend.order.dto.update_order.UpdateOrderAddressRequest;
import cbms_backend.order.dto.update_order.UpdateOrderStatusRequest;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID userId
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(status, customerId, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderAddress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderAddressRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }
}
