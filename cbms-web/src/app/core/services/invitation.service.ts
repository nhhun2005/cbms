import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { VerifyInvitationResponse, AcceptInvitationRequest } from '../models/invitation.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InvitationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/invitations`;

  verifyToken(token: string): Observable<ApiResponse<VerifyInvitationResponse>> {
    const params = new HttpParams().set('token', token);
    return this.http.get<ApiResponse<VerifyInvitationResponse>>(`${this.baseUrl}/verify`, { params });
  }

  acceptInvitation(request: AcceptInvitationRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/accept`, request);
  }
}
