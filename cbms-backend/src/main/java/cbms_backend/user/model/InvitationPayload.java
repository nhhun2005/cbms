package cbms_backend.user.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record InvitationPayload(
        String token,
        String name,
        String phone,
        Set<UUID> roleIds,
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
