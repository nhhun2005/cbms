package cbms_backend.user.dto.permission;

import cbms_backend.user.entity.Permission;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name
) {
    public static PermissionResponse fromEntity(Permission permission) {
        if (permission == null) {
            return null;
        }
        return new PermissionResponse(
                permission.getId(),
                permission.getName()
        );
    }
}
