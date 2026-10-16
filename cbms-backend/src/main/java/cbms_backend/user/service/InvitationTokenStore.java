package cbms_backend.user.service;

import cbms_backend.user.model.InvitationPayload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvitationTokenStore {
    private final Map<String, InvitationPayload> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> phoneToToken = new ConcurrentHashMap<>();

    public void save(InvitationPayload payload) {
        // Revoke any existing active token for this phone number
        String oldToken = phoneToToken.put(payload.phone(), payload.token());
        if (oldToken != null) {
            tokenStore.remove(oldToken);
        }
        tokenStore.put(payload.token(), payload);
    }

    public InvitationPayload get(String token) {
        if (token == null) {
            return null;
        }
        return tokenStore.get(token);
    }

    public InvitationPayload remove(String token) {
        if (token == null) {
            return null;
        }
        InvitationPayload payload = tokenStore.remove(token);
        if (payload != null) {
            phoneToToken.remove(payload.phone(), token);
        }
        return payload;
    }

    @Scheduled(fixedRate = 3600000) // Cleanup every 1 hour
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenStore.entrySet().removeIf(entry -> {
            if (entry.getValue().expiresAt().isBefore(now)) {
                phoneToToken.remove(entry.getValue().phone(), entry.getKey());
                return true;
            }
            return false;
        });
    }

    public int size() {
        return tokenStore.size();
    }

    public void clear() {
        tokenStore.clear();
        phoneToToken.clear();
    }
}
