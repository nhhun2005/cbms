package cbms_backend.order.repository;

import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN o.assignments a
        WHERE (:status IS NULL OR o.status = :status)
          AND (:customerId IS NULL OR o.customer.id = :customerId)
          AND (:userId IS NULL OR a.user.id = :userId)
        ORDER BY o.createdAt DESC
    """)
    List<Order> findOrdersWithFilters(
            @Param("status") OrderStatus status,
            @Param("customerId") UUID customerId,
            @Param("userId") UUID userId
    );
}
