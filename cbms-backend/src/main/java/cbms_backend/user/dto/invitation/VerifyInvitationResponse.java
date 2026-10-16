package cbms_backend.user.dto.invitation;

import java.time.Instant;

public record VerifyInvitationResponse(
        String name,
        String phone,
        Instant expiresAt
) {
}
