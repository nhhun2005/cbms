package cbms_backend.order.repository;

import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.OrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, UUID> {
    List<OrderAssignment> findByOrderId(UUID orderId);
    List<OrderAssignment> findByUserId(UUID userId);
    boolean existsByOrderIdAndUserIdAndRole(UUID orderId, UUID userId, AssignmentRole role);
    Optional<OrderAssignment> findByIdAndOrderId(UUID id, UUID orderId);
    long countByOrderIdAndRole(UUID orderId, AssignmentRole role);
}
