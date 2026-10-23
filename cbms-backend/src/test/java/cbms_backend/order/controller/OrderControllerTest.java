package cbms_backend.order.controller;

import cbms_backend.core.exception.CannotDeleteOrderException;
import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.create_order.CreateOrderRequest;
import cbms_backend.order.dto.create_order.CurtainItemRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;
import cbms_backend.order.dto.get_order.OrderResponse;
import cbms_backend.order.dto.update_order.UpdateOrderAddressRequest;
import cbms_backend.order.dto.update_order.UpdateOrderStatusRequest;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 201 when request is valid")
    void createOrder_shouldReturn201() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                orderId,
                null,
                null,
                OrderStatus.ORDERED,
                Instant.now(),
                List.of(new CurtainResponse(UUID.randomUUID(), orderId, 2.5, 3.0)),
                List.of(new OrderAssignmentResponse(UUID.randomUUID(), orderId, creatorId, "Creator", "0912345678", AssignmentRole.CREATOR, Instant.now()))
        );

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(ApiResponse.success(response, "Order created successfully"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "%s",
                                    "customerAddressId": "%s",
                                    "creatorUserId": "%s",
                                    "curtains": [
                                        { "width": 2.5, "height": 3.0 }
                                    ]
                                }
                                """.formatted(customerId, addressId, creatorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ORDERED"))
                .andExpect(jsonPath("$.data.curtains[0].width").value(2.5))
                .andExpect(jsonPath("$.data.assignments[0].role").value("CREATOR"));
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 400 when validation fails")
    void createOrder_shouldReturn400_whenCurtainsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "%s",
                                    "customerAddressId": "%s",
                                    "creatorUserId": "%s",
                                    "curtains": []
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order must contain at least one curtain"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 200 when order exists")
    void getOrderById_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, null, null, OrderStatus.ORDERED, Instant.now(), List.of(), List.of());

        when(orderService.getOrderById(orderId))
                .thenReturn(ApiResponse.success(response, "Order retrieved successfully"));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 404 when order not found")
    void getOrderById_shouldReturn404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrderById(orderId))
                .thenThrow(new OrderNotExistsException("Order does not exist"));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order does not exist"));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{id}/status should return 200 when valid")
    void updateOrderStatus_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, null, null, OrderStatus.SEWING, Instant.now(), List.of(), List.of());

        when(orderService.updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class)))
                .thenReturn(ApiResponse.success(response, "Order status updated successfully"));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "SEWING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SEWING"));
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{id}/address should return 200 when valid")
    void updateOrderAddress_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID newAddressId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, null, null, OrderStatus.ORDERED, Instant.now(), List.of(), List.of());

        when(orderService.updateOrderAddress(eq(orderId), any(UpdateOrderAddressRequest.class)))
                .thenReturn(ApiResponse.success(response, "Order address updated successfully"));

        mockMvc.perform(put("/api/v1/orders/{id}/address", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerAddressId": "%s"
                                }
                                """.formatted(newAddressId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} should return 200 when deleted")
    void deleteOrder_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.deleteOrder(orderId))
                .thenReturn(ApiResponse.success("Order deleted successfully"));

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} should return 400 when cannot delete")
    void deleteOrder_shouldReturn400() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.deleteOrder(orderId))
                .thenThrow(new CannotDeleteOrderException("Cannot delete order because its status is not ORDERED"));

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete order because its status is not ORDERED"));
    }
}
