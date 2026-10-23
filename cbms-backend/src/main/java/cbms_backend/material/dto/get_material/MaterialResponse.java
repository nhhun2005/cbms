package cbms_backend.material.dto.get_material;

import cbms_backend.material.entity.Material;

import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        String name,
        String description,
        String unit,
        Double quantity,
        String imageUrl,
        Instant updatedAt
) {
    public static MaterialResponse fromEntity(Material material) {
        if (material == null) {
            return null;
        }
        return new MaterialResponse(
                material.getId(),
                material.getName(),
                material.getDescription(),
                material.getUnit(),
                material.getQuantity(),
                material.getImageUrl(),
                material.getUpdatedAt()
        );
    }
}
