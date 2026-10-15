package cbms_backend.user.service;

import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.exception.UserAlreadyInactiveException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.entity.Role;
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

    @Test
    void getUser_shouldReturnUserSuccessfully_whenUserHasNoRoles() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setName("Huan");
        user.setPhone("0354023611");
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.WORKING);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        ApiResponse<GetUserResponse> response = service.getUser(id);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(id, response.getData().id());
        assertEquals("Huan", response.getData().name());
        assertEquals("0354023611", response.getData().phone());
        assertEquals(UserStatus.WORKING, response.getData().status());
        assertNull(response.getData().roles());
    }

    @Test
    void getUser_shouldReturnUserSuccessfully_whenUserHasRoles() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setName("Huan");
        user.setPhone("0354023611");
        user.setPassword("encoded-password");
        user.setStatus(UserStatus.WORKING);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        Role staffRole = new Role();
        staffRole.setName("STAFF");

        user.setRoles(java.util.Set.of(adminRole, staffRole));

        when(repository.findById(id)).thenReturn(Optional.of(user));

        ApiResponse<GetUserResponse> response = service.getUser(id);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(id, response.getData().id());
        assertEquals("Huan", response.getData().name());
        assertEquals("0354023611", response.getData().phone());
        assertEquals(UserStatus.WORKING, response.getData().status());
        assertNotNull(response.getData().roles());
        assertEquals(2, response.getData().roles().size());
        assertTrue(response.getData().roles().contains("ADMIN"));
        assertTrue(response.getData().roles().contains("STAFF"));
    }

    @Test
    void getUser_shouldThrowException_whenUserNotExists() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> service.getUser(id));
    }
}