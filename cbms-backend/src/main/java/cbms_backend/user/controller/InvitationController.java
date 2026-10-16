package cbms_backend.user.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.invitation.AcceptInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationResponse;
import cbms_backend.user.dto.invitation.VerifyInvitationResponse;
import cbms_backend.user.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateInvitationResponse>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitationService.createInvitation(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyInvitationResponse>> verifyInvitation(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(invitationService.verifyInvitation(token));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<GetUserResponse>> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitationService.acceptInvitation(request));
    }
}
