package cbms_backend.user.service;

import cbms_backend.core.exception.PermissionAlreadyExistsException;
import cbms_backend.core.exception.PermissionNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.CreatePermissionRequest;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.entity.Permission;
import cbms_backend.user.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplementTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImplement permissionService;

    @Test
    void createPermission_shouldCreatePermissionSuccessfully() {
        CreatePermissionRequest request = new CreatePermissionRequest("MANAGE_USERS");
        UUID id = UUID.randomUUID();

        when(permissionRepository.existsByName("MANAGE_USERS")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission p = invocation.getArgument(0);
            p.setId(id);
            return p;
        });

        ApiResponse<PermissionResponse> response = permissionService.createPermission(request);

        assertNotNull(response);
        assertEquals("Permission created successfully", response.getMessage());
        assertEquals(id, response.getData().id());
        assertEquals("MANAGE_USERS", response.getData().name());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void createPermission_shouldThrowException_whenPermissionAlreadyExists() {
        CreatePermissionRequest request = new CreatePermissionRequest("MANAGE_USERS");

        when(permissionRepository.existsByName("MANAGE_USERS")).thenReturn(true);

        assertThrows(PermissionAlreadyExistsException.class,
                () -> permissionService.createPermission(request));
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void getAllPermissions_shouldReturnListOfPermissions() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Permission p1 = new Permission();
        p1.setId(id1);
        p1.setName("READ");

        Permission p2 = new Permission();
        p2.setId(id2);
        p2.setName("WRITE");

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

        ApiResponse<List<PermissionResponse>> response = permissionService.getAllPermissions();

        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals("Permissions retrieved successfully", response.getMessage());
    }

    @Test
    void getPermissionById_shouldReturnPermission_whenExists() {
        UUID id = UUID.randomUUID();
        Permission p = new Permission();
        p.setId(id);
        p.setName("READ");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(p));

        ApiResponse<PermissionResponse> response = permissionService.getPermissionById(id);

        assertNotNull(response);
        assertEquals(id, response.getData().id());
        assertEquals("READ", response.getData().name());
    }

    @Test
    void getPermissionById_shouldThrowException_whenNotExists() {
        UUID id = UUID.randomUUID();

        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PermissionNotExistsException.class,
                () -> permissionService.getPermissionById(id));
    }
}
