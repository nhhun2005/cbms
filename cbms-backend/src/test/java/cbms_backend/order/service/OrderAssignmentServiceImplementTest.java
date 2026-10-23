package cbms_backend.order.service;

import cbms_backend.core.exception.CannotModifyCreatorAssignmentException;
import cbms_backend.core.exception.OrderAssignmentAlreadyExistsException;
import cbms_backend.core.exception.OrderAssignmentNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.assignment.CreateAssignmentRequest;
import cbms_backend.order.dto.get_order.OrderAssignmentResponse;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderAssignment;
import cbms_backend.order.repository.OrderAssignmentRepository;
import cbms_backend.order.repository.OrderRepository;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class OrderAssignmentServiceImplementTest {

    @Mock
    private OrderAssignmentRepository orderAssignmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderAssignmentServiceImplement orderAssignmentService;

    private Order order;
    private User sewerUser;
    private User installerUser;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());

        sewerUser = new User();
        sewerUser.setId(UUID.randomUUID());
        sewerUser.setName("Sewer Staff");
        sewerUser.setStatus(UserStatus.WORKING);

        installerUser = new User();
        installerUser.setId(UUID.randomUUID());
        installerUser.setName("Installer Staff");
        installerUser.setStatus(UserStatus.WORKING);
    }

    @Test
    @DisplayName("addAssignment should succeed when adding SEWER or INSTALLER")
    void addAssignment_shouldSucceed() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(sewerUser.getId())).thenReturn(Optional.of(sewerUser));
        when(orderAssignmentRepository.existsByOrderIdAndUserIdAndRole(order.getId(), sewerUser.getId(), AssignmentRole.SEWER))
                .thenReturn(false);
        when(orderAssignmentRepository.save(any(OrderAssignment.class))).thenAnswer(i -> {
            OrderAssignment oa = i.getArgument(0);
            oa.setId(UUID.randomUUID());
            return oa;
        });

        CreateAssignmentRequest request = new CreateAssignmentRequest(sewerUser.getId(), AssignmentRole.SEWER);
        ApiResponse<OrderAssignmentResponse> response = orderAssignmentService.addAssignment(order.getId(), request);

        assertNotNull(response);
        assertEquals("Staff assigned to order successfully", response.getMessage());
        assertEquals(AssignmentRole.SEWER, response.getData().role());
        assertEquals(sewerUser.getId(), response.getData().userId());
        verify(orderAssignmentRepository).save(any(OrderAssignment.class));
    }

    @Test
    @DisplayName("addAssignment should throw CannotModifyCreatorAssignmentException when role is CREATOR")
    void addAssignment_shouldThrow_whenRoleIsCreator() {
        CreateAssignmentRequest request = new CreateAssignmentRequest(sewerUser.getId(), AssignmentRole.CREATOR);

        CannotModifyCreatorAssignmentException ex = assertThrows(
                CannotModifyCreatorAssignmentException.class,
                () -> orderAssignmentService.addAssignment(order.getId(), request)
        );
        assertTrue(ex.getMessage().contains("Cannot manually assign or add a CREATOR role"));
        verify(orderAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("addAssignment should throw OrderAssignmentAlreadyExistsException when already assigned same role")
    void addAssignment_shouldThrow_whenAlreadyAssigned() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(sewerUser.getId())).thenReturn(Optional.of(sewerUser));
        when(orderAssignmentRepository.existsByOrderIdAndUserIdAndRole(order.getId(), sewerUser.getId(), AssignmentRole.SEWER))
                .thenReturn(true);

        CreateAssignmentRequest request = new CreateAssignmentRequest(sewerUser.getId(), AssignmentRole.SEWER);
        assertThrows(OrderAssignmentAlreadyExistsException.class, () -> orderAssignmentService.addAssignment(order.getId(), request));
        verify(orderAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("addAssignment should throw IllegalArgumentException when user is INACTIVE")
    void addAssignment_shouldThrow_whenUserNotWorking() {
        sewerUser.setStatus(UserStatus.INACTIVE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(sewerUser.getId())).thenReturn(Optional.of(sewerUser));

        CreateAssignmentRequest request = new CreateAssignmentRequest(sewerUser.getId(), AssignmentRole.SEWER);
        assertThrows(IllegalArgumentException.class, () -> orderAssignmentService.addAssignment(order.getId(), request));
        verify(orderAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteAssignment should succeed for non-CREATOR assignment")
    void deleteAssignment_shouldSucceed() {
        UUID assignmentId = UUID.randomUUID();
        OrderAssignment assignment = new OrderAssignment();
        assignment.setId(assignmentId);
        assignment.setOrder(order);
        assignment.setUser(sewerUser);
        assignment.setRole(AssignmentRole.SEWER);

        when(orderAssignmentRepository.findByIdAndOrderId(assignmentId, order.getId()))
                .thenReturn(Optional.of(assignment));

        ApiResponse<Void> response = orderAssignmentService.deleteAssignment(order.getId(), assignmentId);

        assertNotNull(response);
        assertEquals("Order assignment deleted successfully", response.getMessage());
        verify(orderAssignmentRepository).delete(assignment);
    }

    @Test
    @DisplayName("deleteAssignment should throw CannotModifyCreatorAssignmentException when role is CREATOR")
    void deleteAssignment_shouldThrow_whenRoleIsCreator() {
        UUID assignmentId = UUID.randomUUID();
        OrderAssignment creatorAssignment = new OrderAssignment();
        creatorAssignment.setId(assignmentId);
        creatorAssignment.setOrder(order);
        creatorAssignment.setUser(sewerUser);
        creatorAssignment.setRole(AssignmentRole.CREATOR);

        when(orderAssignmentRepository.findByIdAndOrderId(assignmentId, order.getId()))
                .thenReturn(Optional.of(creatorAssignment));

        CannotModifyCreatorAssignmentException ex = assertThrows(
                CannotModifyCreatorAssignmentException.class,
                () -> orderAssignmentService.deleteAssignment(order.getId(), assignmentId)
        );
        assertTrue(ex.getMessage().contains("Cannot delete or remove the CREATOR assignment"));
        verify(orderAssignmentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getAssignmentsByOrderId should return list when order exists")
    void getAssignmentsByOrderId_shouldReturnList() {
        when(orderRepository.existsById(order.getId())).thenReturn(true);
        when(orderAssignmentRepository.findByOrderId(order.getId())).thenReturn(List.of());

        ApiResponse<List<OrderAssignmentResponse>> response = orderAssignmentService.getAssignmentsByOrderId(order.getId());

        assertNotNull(response);
        assertEquals(0, response.getData().size());
    }

    @Test
    @DisplayName("getAssignmentsByUserId should return list when user exists")
    void getAssignmentsByUserId_shouldReturnList() {
        when(userRepository.existsById(sewerUser.getId())).thenReturn(true);
        when(orderAssignmentRepository.findByUserId(sewerUser.getId())).thenReturn(List.of());

        ApiResponse<List<OrderAssignmentResponse>> response = orderAssignmentService.getAssignmentsByUserId(sewerUser.getId());

        assertNotNull(response);
        assertEquals(0, response.getData().size());
    }
}
