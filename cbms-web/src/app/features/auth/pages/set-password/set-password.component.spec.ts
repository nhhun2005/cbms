import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SetPasswordComponent } from './set-password.component';
import { InvitationService } from '../../../../core/services/invitation.service';

describe('SetPasswordComponent', () => {
  let component: SetPasswordComponent;
  let fixture: ComponentFixture<SetPasswordComponent>;
  let invitationServiceMock: {
    verifyToken: ReturnType<typeof vi.fn>;
    acceptInvitation: ReturnType<typeof vi.fn>;
  };

  const setupTest = async (tokenValue: string | null = 'valid-token') => {
    invitationServiceMock = {
      verifyToken: vi.fn(),
      acceptInvitation: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [SetPasswordComponent],
      providers: [
        { provide: InvitationService, useValue: invitationServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(tokenValue ? { token: tokenValue } : {}),
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SetPasswordComponent);
    component = fixture.componentInstance;
  };

  it('should show error when token is missing', async () => {
    await setupTest(null);
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.verifyingError()).toContain('Không tìm thấy mã xác thực');
    expect(invitationServiceMock.verifyToken).not.toHaveBeenCalled();
  });

  it('should verify token successfully and populate user info', async () => {
    await setupTest('test-token-123');
    invitationServiceMock.verifyToken.mockReturnValue(
      of({
        message: 'Success',
        data: {
          name: 'Nguyen Van A',
          phone: '0987654321',
          expiresAt: '2026-10-17T06:00:00Z',
        },
      })
    );

    fixture.detectChanges();

    expect(invitationServiceMock.verifyToken).toHaveBeenCalledWith('test-token-123');
    expect(component.isLoading()).toBe(false);
    expect(component.userInfo()?.name).toBe('Nguyen Van A');
    expect(component.userInfo()?.phone).toBe('0987654321');
  });

  it('should show expired error when token verification returns 410', async () => {
    await setupTest('expired-token');
    invitationServiceMock.verifyToken.mockReturnValue(
      throwError(() => ({ status: 410 }))
    );

    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.verifyingError()).toContain('đã hết hạn');
  });

  it('should validate password mismatch', async () => {
    await setupTest('test-token-123');
    invitationServiceMock.verifyToken.mockReturnValue(
      of({
        message: 'Success',
        data: { name: 'Nguyen Van A', phone: '0987654321', expiresAt: '' },
      })
    );

    fixture.detectChanges();

    component.form.controls.password.setValue('123456');
    component.form.controls.confirmPassword.setValue('654321');
    component.form.updateValueAndValidity();

    expect(component.form.errors?.['passwordMismatch']).toBe(true);
    expect(component.form.invalid).toBe(true);
  });

  it('should call acceptInvitation on submit with valid password', async () => {
    await setupTest('test-token-123');
    invitationServiceMock.verifyToken.mockReturnValue(
      of({
        message: 'Success',
        data: { name: 'Nguyen Van A', phone: '0987654321', expiresAt: '' },
      })
    );
    invitationServiceMock.acceptInvitation.mockReturnValue(
      of({ message: 'Success', data: null })
    );

    fixture.detectChanges();

    component.form.controls.password.setValue('password123');
    component.form.controls.confirmPassword.setValue('password123');

    component.onSubmit();

    expect(invitationServiceMock.acceptInvitation).toHaveBeenCalledWith({
      token: 'test-token-123',
      password: 'password123',
    });
    expect(component.submitSuccess()).toBe(true);
  });
});
