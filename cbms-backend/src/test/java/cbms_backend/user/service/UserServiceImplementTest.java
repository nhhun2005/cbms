package cbms_backend.user.service;

import cbms_backend.core.exception.RoleNotExistsException;
import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.exception.UserAlreadyInactiveException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.get_user.GetUserResponse;
import cbms_backend.user.dto.role.AssignRolesRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.entity.Role;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.RoleRepository;
import cbms_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementTest {

    @Mock
    private UserRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplement service;

    @Test
    void createAccount_shouldCreateUserSuccessfully() {
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456"
        );

        when(repository.existsByPhone(request.phone())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        ApiResponse<GetUserResponse> response = service.createAccount(request);

        assertNotNull(response);
        assertEquals("Account created", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("Huan", response.getData().name());
        assertEquals("0354023611", response.getData().phone());

        verify(repository).save(any(User.class));
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void createAccount_shouldThrowException_whenPhoneAlreadyExists() {
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456"
        );

        when(repository.existsByPhone(request.phone())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.createAccount(request)
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

        when(repository.existsByPhone(request.phone())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAccount(request);

        verify(passwordEncoder).encode("123456");
        verify(repository).save(argThat(user ->
                user.getPassword().equals("encoded-password")
        ));
    }

    @Test
    void createAccount_withInitialRoles_shouldAssignRoles() {
        UUID roleId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
                "Huan",
                "0354023611",
                "123456",
                Set.of(roleId)
        );

        Role role = new Role();
        role.setId(roleId);
        role.setName("ADMIN");

        when(repository.existsByPhone(request.phone())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findAllById(Set.of(roleId))).thenReturn(List.of(role));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<GetUserResponse> response = service.createAccount(request);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().roles().contains("ADMIN"));
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

        when(repository.findByIdWithRoles(id)).thenReturn(Optional.of(user));

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

        user.setRoles(Set.of(adminRole, staffRole));

        when(repository.findByIdWithRoles(id)).thenReturn(Optional.of(user));

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

        when(repository.findByIdWithRoles(id)).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> service.getUser(id));
    }

    @Test
    void getAllUsers_shouldReturnList() {
        User u1 = new User();
        u1.setId(UUID.randomUUID());
        u1.setName("User 1");
        u1.setPhone("0354023611");
        u1.setStatus(UserStatus.WORKING);

        when(repository.findAllWithRoles()).thenReturn(List.of(u1));

        ApiResponse<List<GetUserResponse>> response = service.getAllUsers();

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("User 1", response.getData().get(0).name());
    }

    @Test
    void getRolesByUserId_shouldReturnRoles() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("DEVELOPER");

        user.setRoles(Set.of(role));

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));

        ApiResponse<Set<RoleResponse>> response = service.getRolesByUserId(userId);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("DEVELOPER", response.getData().iterator().next().name());
    }

    @Test
    void assignRolesToUser_shouldAssignRolesSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setName("Huan");
        user.setPhone("0354023611");
        user.setStatus(UserStatus.WORKING);

        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("MANAGER");

        AssignRolesRequest request = new AssignRolesRequest(null, roleId);

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(Set.of(roleId))).thenReturn(List.of(role));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<GetUserResponse> response = service.assignRolesToUser(userId, request);

        assertNotNull(response);
        assertEquals("Roles assigned to user successfully", response.getMessage());
        assertTrue(response.getData().roles().contains("MANAGER"));
        verify(repository).save(user);
    }

    @Test
    void assignRolesToUser_shouldThrowException_whenUserNotExists() {
        UUID userId = UUID.randomUUID();
        AssignRolesRequest request = new AssignRolesRequest(null, UUID.randomUUID());

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> service.assignRolesToUser(userId, request));
    }

    @Test
    void assignRolesToUser_shouldThrowException_whenRoleNotFound() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UUID roleId = UUID.randomUUID();
        AssignRolesRequest request = new AssignRolesRequest(null, roleId);

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(Set.of(roleId))).thenReturn(Collections.emptyList());

        assertThrows(RoleNotExistsException.class, () -> service.assignRolesToUser(userId, request));
    }

    @Test
    void assignRoleToUser_singlePath_shouldAssignSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("TESTER");

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<GetUserResponse> response = service.assignRoleToUser(userId, roleId);

        assertNotNull(response);
        assertTrue(response.getData().roles().contains("TESTER"));
        verify(repository).save(user);
    }

    @Test
    void removeRoleFromUser_shouldRemoveSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("TESTER");

        user.addRole(role);

        when(repository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<GetUserResponse> response = service.removeRoleFromUser(userId, roleId);

        assertNotNull(response);
        assertNull(response.getData().roles());
        verify(repository).save(user);
    }
}