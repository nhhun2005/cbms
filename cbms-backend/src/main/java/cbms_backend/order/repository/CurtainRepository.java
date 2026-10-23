package cbms_backend.order.repository;

import cbms_backend.order.entity.Curtain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CurtainRepository extends JpaRepository<Curtain, UUID> {
    List<Curtain> findByOrderId(UUID orderId);
    long countByOrderId(UUID orderId);
}
