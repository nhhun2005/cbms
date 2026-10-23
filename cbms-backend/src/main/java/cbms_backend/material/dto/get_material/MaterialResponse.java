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
        Double price,
        String imageUrl,
        Instant updatedAt
) {
    public MaterialResponse(UUID id, String name, String description, String unit, Double quantity, String imageUrl, Instant updatedAt) {
        this(id, name, description, unit, quantity, 100000.0, imageUrl, updatedAt);
    }

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
                material.getPrice(),
                material.getImageUrl(),
                material.getUpdatedAt()
        );
    }
}
