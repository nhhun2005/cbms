package cbms_backend.order.controller;

import cbms_backend.core.exception.CannotModifyCreatorAssignmentException;
import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.OrderAssignmentAlreadyExistsException;
import cbms_backend.core.exception.OrderAssignmentNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.assignment.CreateAssignmentRequest;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.service.OrderAssignmentService;
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
class OrderAssignmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderAssignmentService orderAssignmentService;

    @InjectMocks
    private OrderAssignmentController orderAssignmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderAssignmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/assignments should return 201 when valid")
    void addAssignment_shouldReturn201() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        OrderAssignmentResponse response = new OrderAssignmentResponse(
                assignmentId,
                orderId,
                userId,
                "Sewer Staff",
                "0912345678",
                AssignmentRole.SEWER,
                Instant.now()
        );

        when(orderAssignmentService.addAssignment(eq(orderId), any(CreateAssignmentRequest.class)))
                .thenReturn(ApiResponse.success(response, "Staff assigned to order successfully"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/assignments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "%s",
                                    "role": "SEWER"
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("SEWER"))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/assignments should return 400 when assigning CREATOR")
    void addAssignment_shouldReturn400_whenAssigningCreator() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(orderAssignmentService.addAssignment(eq(orderId), any(CreateAssignmentRequest.class)))
                .thenThrow(new CannotModifyCreatorAssignmentException("Cannot manually assign or add a CREATOR role"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/assignments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "%s",
                                    "role": "CREATOR"
                                }
                                """.formatted(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot manually assign or add a CREATOR role"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/assignments should return 409 when duplicate role for user")
    void addAssignment_shouldReturn409_whenDuplicate() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(orderAssignmentService.addAssignment(eq(orderId), any(CreateAssignmentRequest.class)))
                .thenThrow(new OrderAssignmentAlreadyExistsException("User already has this role in the order"));

        mockMvc.perform(post("/api/v1/orders/{orderId}/assignments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "%s",
                                    "role": "SEWER"
                                }
                                """.formatted(userId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already has this role in the order"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{orderId}/assignments/{assignmentId} should return 200 when deleted")
    void deleteAssignment_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        when(orderAssignmentService.deleteAssignment(orderId, assignmentId))
                .thenReturn(ApiResponse.success("Order assignment deleted successfully"));

        mockMvc.perform(delete("/api/v1/orders/{orderId}/assignments/{assignmentId}", orderId, assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order assignment deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{orderId}/assignments/{assignmentId} should return 400 when attempting to delete CREATOR")
    void deleteAssignment_shouldReturn400_whenDeletingCreator() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        when(orderAssignmentService.deleteAssignment(orderId, assignmentId))
                .thenThrow(new CannotModifyCreatorAssignmentException("Cannot delete or remove the CREATOR assignment"));

        mockMvc.perform(delete("/api/v1/orders/{orderId}/assignments/{assignmentId}", orderId, assignmentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete or remove the CREATOR assignment"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId}/assignments should return 200")
    void getAssignmentsByOrderId_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderAssignmentService.getAssignmentsByOrderId(orderId))
                .thenReturn(ApiResponse.success(List.of(), "Order assignments retrieved successfully"));

        mockMvc.perform(get("/api/v1/orders/{orderId}/assignments", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/orders/assignments/user/{userId} should return 200")
    void getAssignmentsByUserId_shouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();

        when(orderAssignmentService.getAssignmentsByUserId(userId))
                .thenReturn(ApiResponse.success(List.of(), "User assignments retrieved successfully"));

        mockMvc.perform(get("/api/v1/orders/assignments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
