package cbms_backend.user.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
        @NotBlank(message = "Role name must not be blank")
        @Size(min = 2, max = 50, message = "Role name must contain 02-50 characters")
        String name,
        Set<UUID> permissionIds
) {
}
