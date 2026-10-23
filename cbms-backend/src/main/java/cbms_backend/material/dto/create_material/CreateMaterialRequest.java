package cbms_backend.material.dto.create_material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateMaterialRequest(
        @NotBlank(message = "Material name must not be blank")
        @Size(max = 100, message = "Material name must not exceed 100 characters")
        String name,

        String description,

        @NotBlank(message = "Unit must not be blank")
        @Size(max = 50, message = "Unit must not exceed 50 characters")
        String unit,

        @PositiveOrZero(message = "Quantity must be greater than or equal to 0")
        Double quantity,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl
) {
    public CreateMaterialRequest(String name, String unit) {
        this(name, null, unit, 0.0, null);
    }
}
