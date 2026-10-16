package cbms_backend.user.dto.role;

import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.entity.Role;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record RoleResponse(
        UUID id,
        String name,
        Set<PermissionResponse> permissions
) {
    public static RoleResponse fromEntity(Role role) {
        if (role == null) {
            return null;
        }
        Set<PermissionResponse> permissionResponses = null;
        if (role.getPermissions() != null) {
            permissionResponses = role.getPermissions().stream()
                    .map(PermissionResponse::fromEntity)
                    .collect(Collectors.toSet());
        }
        return new RoleResponse(
                role.getId(),
                role.getName(),
                permissionResponses
        );
    }
}
