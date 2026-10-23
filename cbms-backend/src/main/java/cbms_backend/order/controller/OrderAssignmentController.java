package cbms_backend.order.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.assignment.CreateAssignmentRequest;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;
import cbms_backend.order.service.OrderAssignmentService;
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
public class OrderAssignmentController {

    private final OrderAssignmentService orderAssignmentService;

    @PostMapping("/{orderId}/assignments")
    public ResponseEntity<ApiResponse<OrderAssignmentResponse>> addAssignment(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderAssignmentService.addAssignment(orderId, request));
    }

    @DeleteMapping("/{orderId}/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable UUID orderId,
            @PathVariable UUID assignmentId
    ) {
        return ResponseEntity.ok(orderAssignmentService.deleteAssignment(orderId, assignmentId));
    }

    @GetMapping("/{orderId}/assignments")
    public ResponseEntity<ApiResponse<List<OrderAssignmentResponse>>> getAssignmentsByOrderId(
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderAssignmentService.getAssignmentsByOrderId(orderId));
    }

    @GetMapping("/assignments/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderAssignmentResponse>>> getAssignmentsByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(orderAssignmentService.getAssignmentsByUserId(userId));
    }
}
