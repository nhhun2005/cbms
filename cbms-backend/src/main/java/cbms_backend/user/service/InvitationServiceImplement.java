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
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.model.InvitationPayload;
import cbms_backend.user.repository.RoleRepository;
import cbms_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationServiceImplement implements InvitationService {
    private final InvitationTokenStore tokenStore;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.invitation.frontend-url:http://localhost:4200/set-password}")
    private String frontendUrl;

    @Value("${app.invitation.ttl-hours:24}")
    private int ttlHours;

    @Override
    public ApiResponse<CreateInvitationResponse> createInvitation(CreateInvitationRequest request) {
        String phone = request.phone().trim();
        if (userRepository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.roleIds());
            if (roles.size() != request.roleIds().size()) {
                Set<UUID> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
                Set<UUID> missingIds = new HashSet<>(request.roleIds());
                missingIds.removeAll(foundIds);
                throw new RoleNotExistsException("Roles not found with IDs: " + missingIds);
            }
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttlHours, ChronoUnit.HOURS);

        InvitationPayload payload = new InvitationPayload(
                token,
                request.name().trim(),
                phone,
                request.roleIds(),
                now,
                expiresAt
        );

        tokenStore.save(payload);

        String delimiter = frontendUrl.contains("?") ? "&" : "?";
        String invitationUrl = frontendUrl + delimiter + "token=" + token;

        CreateInvitationResponse response = new CreateInvitationResponse(token, invitationUrl, expiresAt);
        return ApiResponse.success(response, "Invitation link created successfully");
    }

    @Override
    public ApiResponse<VerifyInvitationResponse> verifyInvitation(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidInvitationTokenException("Invitation token is required");
        }

        InvitationPayload payload = tokenStore.get(token);
        if (payload == null) {
            throw new InvalidInvitationTokenException("Invalid invitation token");
        }

        if (payload.isExpired()) {
            tokenStore.remove(token);
            throw new InvitationTokenExpiredException("Invitation link has expired");
        }

        VerifyInvitationResponse response = new VerifyInvitationResponse(
                payload.name(),
                payload.phone(),
                payload.expiresAt()
        );
        return ApiResponse.success(response, "Invitation token is valid");
    }

    @Override
    @Transactional
    public ApiResponse<GetUserResponse> acceptInvitation(AcceptInvitationRequest request) {
        String token = request.token().trim();
        InvitationPayload payload = tokenStore.remove(token);
        if (payload == null) {
            throw new InvalidInvitationTokenException("Invalid or already used invitation token");
        }

        if (payload.isExpired()) {
            throw new InvitationTokenExpiredException("Invitation link has expired");
        }

        if (userRepository.existsByPhone(payload.phone())) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        User user = new User();
        user.setName(payload.name());
        user.setPhone(payload.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.WORKING);

        if (payload.roleIds() != null && !payload.roleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(payload.roleIds());
            for (Role role : roles) {
                user.addRole(role);
            }
        }

        User saved = userRepository.save(user);
        return ApiResponse.success(GetUserResponse.fromEntity(saved), "Account created successfully");
    }
}
