package cbms_backend.user.dto.role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record AssignPermissionsRequest(
        Set<UUID> permissionIds,
        UUID permissionId
) {
    public Set<UUID> extractPermissionIds() {
        Set<UUID> ids = new HashSet<>();
        if (permissionIds != null) {
            ids.addAll(permissionIds);
        }
        if (permissionId != null) {
            ids.add(permissionId);
        }
        return ids;
    }
}
