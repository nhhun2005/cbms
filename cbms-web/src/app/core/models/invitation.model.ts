export interface VerifyInvitationResponse {
  name: string;
  phone: string;
  expiresAt: string;
}

export interface AcceptInvitationRequest {
  token: string;
  password: string;
}
