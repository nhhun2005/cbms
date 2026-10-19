package cbms_backend.material.repository;

import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findByMaterialId(UUID materialId);
    List<StockMovement> findByOrderId(UUID orderId);
    List<StockMovement> findBySupplierId(UUID supplierId);
    List<StockMovement> findByType(StockMovementType type);
}
