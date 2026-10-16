package cbms_backend.user.dto.invitation;

import java.time.Instant;

public record CreateInvitationResponse(
        String token,
        String invitationUrl,
        Instant expiresAt
) {
}
