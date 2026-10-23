package cbms_backend.order.service;

import cbms_backend.core.exception.CannotModifyCreatorAssignmentException;
import cbms_backend.core.exception.OrderAssignmentAlreadyExistsException;
import cbms_backend.core.exception.OrderAssignmentNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.assignment.CreateAssignmentRequest;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderAssignment;
import cbms_backend.order.repository.OrderAssignmentRepository;
import cbms_backend.order.repository.OrderRepository;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderAssignmentServiceImplement implements OrderAssignmentService {

    private final OrderAssignmentRepository orderAssignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<OrderAssignmentResponse> addAssignment(UUID orderId, CreateAssignmentRequest request) {
        if (request.role() == AssignmentRole.CREATOR) {
            throw new CannotModifyCreatorAssignmentException("Cannot manually assign or add a CREATOR role");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        if (user.getStatus() != UserStatus.WORKING) {
            throw new IllegalArgumentException("User is not currently working");
        }

        if (orderAssignmentRepository.existsByOrderIdAndUserIdAndRole(orderId, request.userId(), request.role())) {
            throw new OrderAssignmentAlreadyExistsException("User already has this role in the order");
        }

        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrder(order);
        assignment.setUser(user);
        assignment.setRole(request.role());
        assignment.setAssignedAt(Instant.now());

        OrderAssignment saved = orderAssignmentRepository.save(assignment);
        return ApiResponse.success(OrderAssignmentResponse.fromEntity(saved), "Staff assigned to order successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteAssignment(UUID orderId, UUID assignmentId) {
        OrderAssignment assignment = orderAssignmentRepository.findByIdAndOrderId(assignmentId, orderId)
                .orElseThrow(() -> new OrderAssignmentNotExistsException("Order assignment does not exist in this order"));

        if (assignment.getRole() == AssignmentRole.CREATOR) {
            throw new CannotModifyCreatorAssignmentException("Cannot delete or remove the CREATOR assignment");
        }

        orderAssignmentRepository.delete(assignment);
        return ApiResponse.success("Order assignment deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderAssignmentResponse>> getAssignmentsByOrderId(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotExistsException("Order does not exist");
        }

        List<OrderAssignmentResponse> list = orderAssignmentRepository.findByOrderId(orderId).stream()
                .map(OrderAssignmentResponse::fromEntity)
                .toList();

        return ApiResponse.success(list, "Order assignments retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderAssignmentResponse>> getAssignmentsByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotExistsException("User does not exist");
        }

        List<OrderAssignmentResponse> list = orderAssignmentRepository.findByUserId(userId).stream()
                .map(OrderAssignmentResponse::fromEntity)
                .toList();

        return ApiResponse.success(list, "User assignments retrieved successfully");
    }
}
