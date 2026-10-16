package cbms_backend.user.service;

import cbms_backend.core.exception.PermissionNotExistsException;
import cbms_backend.core.exception.RoleAlreadyExistsException;
import cbms_backend.core.exception.RoleNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.permission.PermissionResponse;
import cbms_backend.user.dto.role.AssignPermissionsRequest;
import cbms_backend.user.dto.role.CreateRoleRequest;
import cbms_backend.user.dto.role.RoleResponse;
import cbms_backend.user.entity.Permission;
import cbms_backend.user.entity.Role;
import cbms_backend.user.repository.PermissionRepository;
import cbms_backend.user.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplementTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImplement roleService;

    @Test
    void createRole_shouldCreateRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", null);

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(roleId);
            return r;
        });

        ApiResponse<RoleResponse> response = roleService.createRole(request);

        assertNotNull(response);
        assertEquals(roleId, response.getData().id());
        assertEquals("ADMIN", response.getData().name());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_shouldThrowException_whenRoleAlreadyExists() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", null);

        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThrows(RoleAlreadyExistsException.class, () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getPermissionsByRoleId_shouldReturnPermissionsSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("MANAGER");

        Permission p1 = new Permission();
        p1.setId(UUID.randomUUID());
        p1.setName("READ");

        Permission p2 = new Permission();
        p2.setId(UUID.randomUUID());
        p2.setName("WRITE");

        role.setPermissions(new HashSet<>(Set.of(p1, p2)));

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));

        ApiResponse<Set<PermissionResponse>> response = roleService.getPermissionsByRoleId(roleId);

        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals("Permissions of role retrieved successfully", response.getMessage());
    }

    @Test
    void getPermissionsByRoleId_shouldThrowException_whenRoleNotExists() {
        UUID roleId = UUID.randomUUID();

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.empty());

        assertThrows(RoleNotExistsException.class, () -> roleService.getPermissionsByRoleId(roleId));
    }

    @Test
    void assignPermissionsToRole_shouldAssignPermissionsSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("STAFF");

        UUID pId1 = UUID.randomUUID();
        UUID pId2 = UUID.randomUUID();
        Permission p1 = new Permission();
        p1.setId(pId1);
        p1.setName("VIEW_REPORTS");

        Permission p2 = new Permission();
        p2.setId(pId2);
        p2.setName("CREATE_REPORT");

        AssignPermissionsRequest request = new AssignPermissionsRequest(Set.of(pId1, pId2), null);

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(Set.of(pId1, pId2))).thenReturn(List.of(p1, p2));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<RoleResponse> response = roleService.assignPermissionsToRole(roleId, request);

        assertNotNull(response);
        assertEquals(2, response.getData().permissions().size());
        assertEquals("Permissions assigned to role successfully", response.getMessage());
        verify(roleRepository).save(role);
    }

    @Test
    void assignPermissionsToRole_shouldThrowException_whenPermissionNotFound() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);

        UUID pId1 = UUID.randomUUID();
        AssignPermissionsRequest request = new AssignPermissionsRequest(Set.of(pId1), null);

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(Set.of(pId1))).thenReturn(Collections.emptyList());

        assertThrows(PermissionNotExistsException.class,
                () -> roleService.assignPermissionsToRole(roleId, request));
    }

    @Test
    void assignPermissionToRole_singlePath_shouldAssignSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("EDITOR");

        UUID permissionId = UUID.randomUUID();
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setName("EDIT_ARTICLE");

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<RoleResponse> response = roleService.assignPermissionToRole(roleId, permissionId);

        assertNotNull(response);
        assertEquals(1, response.getData().permissions().size());
        verify(roleRepository).save(role);
    }

    @Test
    void removePermissionFromRole_shouldRemoveSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);
        role.setName("STAFF");

        UUID permissionId = UUID.randomUUID();
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setName("DELETE_ITEM");

        role.addPermission(permission);

        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<RoleResponse> response = roleService.removePermissionFromRole(roleId, permissionId);

        assertNotNull(response);
        assertTrue(response.getData().permissions().isEmpty());
        verify(roleRepository).save(role);
    }
}
