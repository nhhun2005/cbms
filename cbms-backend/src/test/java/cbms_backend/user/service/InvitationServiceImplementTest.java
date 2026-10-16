package cbms_backend.user.service;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.invitation.AcceptInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationResponse;
import cbms_backend.user.dto.invitation.VerifyInvitationResponse;
import cbms_backend.user.entity.Role;
import cbms_backend.user.entity.User;
import cbms_backend.user.model.InvitationPayload;
import cbms_backend.user.repository.RoleRepository;
import cbms_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplementTest {

    @Spy
    private InvitationTokenStore tokenStore = new InvitationTokenStore();

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InvitationServiceImplement invitationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "frontendUrl", "http://localhost:4200/set-password");
        ReflectionTestUtils.setField(invitationService, "ttlHours", 24);
        tokenStore.clear();
    }

    @Test
    void createInvitation_shouldCreateSuccessfully() {
        CreateInvitationRequest request = new CreateInvitationRequest("Huan", "0354023611", null);

        when(userRepository.existsByPhone(request.phone())).thenReturn(false);

        ApiResponse<CreateInvitationResponse> response = invitationService.createInvitation(request);

        assertNotNull(response);
        assertEquals("Invitation link created successfully", response.getMessage());
        assertNotNull(response.getData().token());
        assertTrue(response.getData().invitationUrl().contains("token=" + response.getData().token()));
        assertTrue(response.getData().expiresAt().isAfter(Instant.now()));
        assertEquals(1, tokenStore.size());
    }

    @Test
    void createInvitation_shouldRevokeOldToken_whenSamePhone() {
        CreateInvitationRequest req1 = new CreateInvitationRequest("Huan 1", "0354023611", null);
        CreateInvitationRequest req2 = new CreateInvitationRequest("Huan 2", "0354023611", null);

        when(userRepository.existsByPhone("0354023611")).thenReturn(false);

        ApiResponse<CreateInvitationResponse> res1 = invitationService.createInvitation(req1);
        ApiResponse<CreateInvitationResponse> res2 = invitationService.createInvitation(req2);

        String oldToken = res1.getData().token();
        String newToken = res2.getData().token();

        assertNotEquals(oldToken, newToken);
        assertNull(tokenStore.get(oldToken));
        assertNotNull(tokenStore.get(newToken));
        assertEquals(1, tokenStore.size());
    }

    @Test
    void createInvitation_shouldThrowException_whenPhoneAlreadyExists() {
        CreateInvitationRequest request = new CreateInvitationRequest("Huan", "0354023611", null);

        when(userRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> invitationService.createInvitation(request));
        assertEquals(0, tokenStore.size());
    }

    @Test
    void createInvitation_shouldThrowException_whenRoleNotExists() {
        UUID roleId = UUID.randomUUID();
        CreateInvitationRequest request = new CreateInvitationRequest("Huan", "0354023611", Set.of(roleId));

        when(userRepository.existsByPhone(request.phone())).thenReturn(false);
        when(roleRepository.findAllById(Set.of(roleId))).thenReturn(Collections.emptyList());

        assertThrows(RoleNotExistsException.class, () -> invitationService.createInvitation(request));
    }

    @Test
    void verifyInvitation_shouldReturnValid_whenTokenExistsAndNotExpired() {
        String token = "valid-token-123";
        InvitationPayload payload = new InvitationPayload(
                token,
                "Huan",
                "0354023611",
                null,
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        tokenStore.save(payload);

        ApiResponse<VerifyInvitationResponse> response = invitationService.verifyInvitation(token);

        assertNotNull(response);
        assertEquals("Huan", response.getData().name());
        assertEquals("0354023611", response.getData().phone());
    }

    @Test
    void verifyInvitation_shouldThrowException_whenTokenNotFound() {
        assertThrows(InvalidInvitationTokenException.class, () -> invitationService.verifyInvitation("non-existent"));
    }

    @Test
    void verifyInvitation_shouldThrowException_whenTokenExpired() {
        String token = "expired-token";
        InvitationPayload payload = new InvitationPayload(
                token,
                "Huan",
                "0354023611",
                null,
                Instant.now().minus(25, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        tokenStore.save(payload);

        assertThrows(InvitationTokenExpiredException.class, () -> invitationService.verifyInvitation(token));
        assertNull(tokenStore.get(token));
    }

    @Test
    void acceptInvitation_shouldCreateAccountAndInvalidateToken() {
        String token = "accept-token";
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("STAFF");

        InvitationPayload payload = new InvitationPayload(
                token,
                "Huan",
                "0354023611",
                Set.of(roleId),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        tokenStore.save(payload);

        when(userRepository.existsByPhone("0354023611")).thenReturn(false);
        when(passwordEncoder.encode("secretPass")).thenReturn("hashedPass");
        when(roleRepository.findAllById(Set.of(roleId))).thenReturn(List.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AcceptInvitationRequest request = new AcceptInvitationRequest(token, "secretPass");
        ApiResponse<GetUserResponse> response = invitationService.acceptInvitation(request);

        assertNotNull(response);
        assertEquals("Account created successfully", response.getMessage());
        assertEquals("Huan", response.getData().name());
        assertEquals("0354023611", response.getData().phone());
        assertTrue(response.getData().roles().contains("STAFF"));

        // Token must be invalidated immediately
        assertNull(tokenStore.get(token));

        // Reusing token must fail
        assertThrows(InvalidInvitationTokenException.class, () -> invitationService.acceptInvitation(request));
    }
}
