package cbms_backend.material.repository;

import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findByMaterialId(UUID materialId);
    List<StockMovement> findByOrderId(UUID orderId);
    List<StockMovement> findBySupplierId(UUID supplierId);
    List<StockMovement> findByType(StockMovementType type);
    boolean existsByMaterialId(UUID materialId);
    boolean existsBySupplierId(UUID supplierId);
    boolean existsByOrderId(UUID orderId);

    @Query("SELECT sm FROM StockMovement sm LEFT JOIN FETCH sm.material LEFT JOIN FETCH sm.supplier LEFT JOIN FETCH sm.order WHERE sm.id = :id")
    Optional<StockMovement> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT sm FROM StockMovement sm LEFT JOIN FETCH sm.material LEFT JOIN FETCH sm.supplier LEFT JOIN FETCH sm.order ORDER BY sm.createdAt DESC")
    List<StockMovement> findAllWithDetails();

    @Query("SELECT sm FROM StockMovement sm LEFT JOIN FETCH sm.material LEFT JOIN FETCH sm.supplier LEFT JOIN FETCH sm.order WHERE sm.material.id = :materialId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByMaterialIdWithDetails(@Param("materialId") UUID materialId);

    @Query("SELECT sm FROM StockMovement sm LEFT JOIN FETCH sm.material LEFT JOIN FETCH sm.supplier LEFT JOIN FETCH sm.order WHERE sm.supplier.id = :supplierId ORDER BY sm.createdAt DESC")
    List<StockMovement> findBySupplierIdWithDetails(@Param("supplierId") UUID supplierId);

    @Query("""
        SELECT sm FROM StockMovement sm
        LEFT JOIN FETCH sm.material
        LEFT JOIN FETCH sm.supplier
        LEFT JOIN FETCH sm.order
        WHERE (:type IS NULL OR sm.type = :type)
          AND (:materialId IS NULL OR sm.material.id = :materialId)
          AND (:supplierId IS NULL OR sm.supplier.id = :supplierId)
        ORDER BY sm.createdAt DESC
    """)
    List<StockMovement> findWarehouseHistory(
            @Param("type") StockMovementType type,
            @Param("materialId") UUID materialId,
            @Param("supplierId") UUID supplierId
    );
}
