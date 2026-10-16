package cbms_backend.user.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.invitation.AcceptInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationRequest;
import cbms_backend.user.dto.invitation.CreateInvitationResponse;
import cbms_backend.user.dto.invitation.VerifyInvitationResponse;

public interface InvitationService {
    ApiResponse<CreateInvitationResponse> createInvitation(CreateInvitationRequest request);
    ApiResponse<VerifyInvitationResponse> verifyInvitation(String token);
    ApiResponse<GetUserResponse> acceptInvitation(AcceptInvitationRequest request);
}
