package cbms_backend.material.dto.get_stock_movement;

import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID materialId,
        String materialName,
        UUID supplierId,
        String supplierName,
        UUID orderId,
        StockMovementType type,
        Double quantity,
        Double currentMaterialQuantity,
        Instant createdAt
) {
    public static StockMovementResponse fromEntity(StockMovement movement) {
        if (movement == null) {
            return null;
        }
        return new StockMovementResponse(
                movement.getId(),
                movement.getMaterial() != null ? movement.getMaterial().getId() : null,
                movement.getMaterial() != null ? movement.getMaterial().getName() : null,
                movement.getSupplier() != null ? movement.getSupplier().getId() : null,
                movement.getSupplier() != null ? movement.getSupplier().getName() : null,
                movement.getOrder() != null ? movement.getOrder().getId() : null,
                movement.getType(),
                movement.getQuantity(),
                movement.getMaterial() != null ? movement.getMaterial().getQuantity() : null,
                movement.getCreatedAt()
        );
    }
}
