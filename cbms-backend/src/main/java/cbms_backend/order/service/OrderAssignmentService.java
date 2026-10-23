package cbms_backend.order.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.assignment.CreateAssignmentRequest;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderAssignmentService {
    ApiResponse<OrderAssignmentResponse> addAssignment(UUID orderId, CreateAssignmentRequest request);
    ApiResponse<Void> deleteAssignment(UUID orderId, UUID assignmentId);
    ApiResponse<List<OrderAssignmentResponse>> getAssignmentsByOrderId(UUID orderId);
    ApiResponse<List<OrderAssignmentResponse>> getAssignmentsByUserId(UUID userId);
}
