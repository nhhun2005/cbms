package cbms_backend.user.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank(message = "Permission name must not be blank")
        @Size(min = 2, max = 100, message = "Permission name must contain 02-100 characters")
        String name
) {
}
