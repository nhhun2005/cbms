package cbms_backend.user.service;

import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.exception.UserAlreadyInactiveException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplement service;

    @Test
    void createAccount_shouldCreateUserSuccessfully(){
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456"
        );

        when(repository.existsByPhone(request.phone()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        ApiResponse<Void> response = service.createAccount(request);

        assertNotNull(response);

        verify(repository).save(any(User.class));
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void createAccount_shouldThrowException_whenPhoneAlreadyExists(){
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456"
        );

        when(repository.existsByPhone(request.phone()))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                ()-> service.createAccount(request)
        );

        verify(repository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createAccount_shouldEncodePassword() {
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456"
        );

        when(repository.existsByPhone(request.phone()))
                .thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded-password");

        service.createAccount(request);

        verify(passwordEncoder).encode("123456");

        verify(repository).save(argThat(user ->
                user.getPassword().equals("encoded-password")
        ));
    }

    @Test
    void disableUser_shouldDisableUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setStatus(UserStatus.WORKING);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        ApiResponse<Void> response = service.disableUser(id);

        assertNotNull(response);
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(repository).save(user);
    }

    @Test
    void disableUser_shouldThrowException_whenUserNotExists() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> service.disableUser(id));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void disableUser_shouldThrowException_whenUserAlreadyInactive() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setStatus(UserStatus.INACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyInactiveException.class, () -> service.disableUser(id));
        verify(repository, never()).save(any(User.class));
    }
}