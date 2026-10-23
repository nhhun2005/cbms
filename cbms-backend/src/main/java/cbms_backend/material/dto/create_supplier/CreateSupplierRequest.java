package cbms_backend.material.dto.create_supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank(message = "Supplier name must not be blank")
        @Size(max = 100, message = "Supplier name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Contact must not be blank")
        @Size(max = 100, message = "Contact must not exceed 100 characters")
        @Pattern(
                regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|0\\d{9})$",
                message = "Contact must be a valid email or Vietnamese phone number"
        )
        String contact
) {
    public CreateSupplierRequest {
        name = name != null ? name.trim() : null;
        contact = contact != null ? contact.trim() : null;
    }
}
