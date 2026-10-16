package cbms_backend.user.controller;

import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.exception.InvalidInvitationTokenException;
import cbms_backend.core.exception.InvitationTokenExpiredException;
import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.invitation.AcceptInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationResponse;
import cbms_backend.user.dto.invitation.VerifyInvitationResponse;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.service.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InvitationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationController invitationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invitationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createInvitation_shouldReturn201() throws Exception {
        CreateInvitationResponse response = new CreateInvitationResponse(
                "token123",
                "http://localhost:4200/set-password?token=token123",
                Instant.now()
        );

        when(invitationService.createInvitation(any(CreateInvitationRequest.class)))
                .thenReturn(ApiResponse.success(response, "Invitation link created successfully"));

        mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Huan\", \"phone\": \"0354023611\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Invitation link created successfully"))
                .andExpect(jsonPath("$.data.token").value("token123"));
    }

    @Test
    void createInvitation_shouldReturn409_whenPhoneAlreadyExists() throws Exception {
        when(invitationService.createInvitation(any(CreateInvitationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Phone number already exists"));

        mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Huan\", \"phone\": \"0354023611\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"));
    }

    @Test
    void verifyInvitation_shouldReturn200() throws Exception {
        VerifyInvitationResponse response = new VerifyInvitationResponse("Huan", "0354023611", Instant.now());

        when(invitationService.verifyInvitation("token123"))
                .thenReturn(ApiResponse.success(response, "Invitation token is valid"));

        mockMvc.perform(get("/api/v1/invitations/verify?token=token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation token is valid"))
                .andExpect(jsonPath("$.data.name").value("Huan"));
    }

    @Test
    void verifyInvitation_shouldReturn400_whenInvalidToken() throws Exception {
        when(invitationService.verifyInvitation("invalid-token"))
                .thenThrow(new InvalidInvitationTokenException("Invalid invitation token"));

        mockMvc.perform(get("/api/v1/invitations/verify?token=invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid invitation token"));
    }

    @Test
    void verifyInvitation_shouldReturn410_whenExpiredToken() throws Exception {
        when(invitationService.verifyInvitation("expired-token"))
                .thenThrow(new InvitationTokenExpiredException("Invitation link has expired"));

        mockMvc.perform(get("/api/v1/invitations/verify?token=expired-token"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Invitation link has expired"));
    }

    @Test
    void acceptInvitation_shouldReturn201() throws Exception {
        GetUserResponse userResponse = new GetUserResponse(UUID.randomUUID(), "Huan", "0354023611", UserStatus.WORKING, Set.of("STAFF"));

        when(invitationService.acceptInvitation(any(AcceptInvitationRequest.class)))
                .thenReturn(ApiResponse.success(userResponse, "Account created successfully"));

        mockMvc.perform(post("/api/v1/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"token123\", \"password\": \"secretPass\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.data.name").value("Huan"));
    }
}
