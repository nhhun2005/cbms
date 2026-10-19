import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { InvitationService } from '../../../../core/services/invitation.service';
import { VerifyInvitationResponse } from '../../../../core/models/invitation.model';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (password && confirmPassword && password !== confirmPassword) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-set-password',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './set-password.component.html',
  styleUrl: './set-password.component.css',
})
export class SetPasswordComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly invitationService = inject(InvitationService);

  readonly token = signal<string>('');
  readonly isLoading = signal<boolean>(true);
  readonly verifyingError = signal<string | null>(null);
  readonly userInfo = signal<VerifyInvitationResponse | null>(null);

  readonly showPassword = signal<boolean>(false);
  readonly showConfirmPassword = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly submitSuccess = signal<boolean>(false);
  readonly submitError = signal<string | null>(null);

  readonly form = new FormGroup(
    {
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(6), Validators.maxLength(50)],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    { validators: [passwordMatchValidator] }
  );

  ngOnInit(): void {
    const rawToken = this.route.snapshot.queryParamMap.get('token');
    const token = rawToken?.trim();

    if (!token) {
      this.isLoading.set(false);
      this.verifyingError.set('Liên kết không hợp lệ: Không tìm thấy mã xác thực (token).');
      return;
    }

    this.token.set(token);
    this.invitationService.verifyToken(token).subscribe({
      next: (response) => {
        this.userInfo.set(response.data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 410) {
          this.verifyingError.set(
            'Liên kết kích hoạt đã hết hạn (sau 24 giờ). Vui lòng liên hệ quản trị viên để nhận liên kết mới.'
          );
        } else if (err.status === 400) {
          this.verifyingError.set(
            err.error?.message ||
              'Liên kết không hợp lệ hoặc tài khoản đã được kích hoạt trước đó.'
          );
        } else {
          this.verifyingError.set(
            'Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại kết nối mạng hoặc thử lại sau.'
          );
        }
      },
    });
  }

  toggleShowPassword(): void {
    this.showPassword.update((prev) => !prev);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((prev) => !prev);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.submitError.set(null);

    const { password } = this.form.getRawValue();

    this.invitationService
      .acceptInvitation({
        token: this.token(),
        password,
      })
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.submitSuccess.set(true);
        },
        error: (err) => {
          this.isSubmitting.set(false);
          if (err.status === 410) {
            this.submitError.set(
              'Liên kết kích hoạt đã hết hạn. Vui lòng liên hệ quản trị viên để được cấp liên kết mới.'
            );
          } else if (err.status === 400) {
            this.submitError.set(
              err.error?.message ||
                'Yêu cầu không hợp lệ hoặc liên kết này đã được sử dụng.'
            );
          } else {
            this.submitError.set(
              'Có lỗi xảy ra trong quá trình tạo tài khoản. Vui lòng thử lại sau.'
            );
          }
        },
      });
  }
}
