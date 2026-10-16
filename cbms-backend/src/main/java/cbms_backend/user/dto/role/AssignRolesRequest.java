package cbms_backend.user.dto.role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record AssignRolesRequest(
        Set<UUID> roleIds,
        UUID roleId
) {
    public Set<UUID> extractRoleIds() {
        Set<UUID> ids = new HashSet<>();
        if (roleIds != null) {
            ids.addAll(roleIds);
        }
        if (roleId != null) {
            ids.add(roleId);
        }
        return ids;
    }
}
