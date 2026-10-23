package cbms_backend.order.dto.get_order;

import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.OrderAssignment;

import java.time.Instant;
import java.util.UUID;

public record OrderAssignmentResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        String userName,
        String userPhone,
        AssignmentRole role,
        Instant assignedAt
) {
    public static OrderAssignmentResponse fromEntity(OrderAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return new OrderAssignmentResponse(
                assignment.getId(),
                assignment.getOrder() != null ? assignment.getOrder().getId() : null,
                assignment.getUser() != null ? assignment.getUser().getId() : null,
                assignment.getUser() != null ? assignment.getUser().getName() : null,
                assignment.getUser() != null ? assignment.getUser().getPhone() : null,
                assignment.getRole(),
                assignment.getAssignedAt()
        );
    }
}
