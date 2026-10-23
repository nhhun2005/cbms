package cbms_backend.order.service;

import cbms_backend.core.exception.CannotDeleteLastCurtainException;
import cbms_backend.core.exception.CannotModifyCurtainException;
import cbms_backend.core.exception.CurtainNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.order.dto.curtain.CreateCurtainRequest;
import cbms_backend.order.dto.curtain.UpdateCurtainRequest;
import cbms_backend.order.dto.get_order.CurtainResponse;
import cbms_backend.order.entity.Curtain;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.repository.CurtainRepository;
import cbms_backend.order.repository.OrderRepository;
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
class CurtainServiceImplementTest {

    @Mock
    private CurtainRepository curtainRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CurtainServiceImplement curtainService;

    private Order order;
    private Curtain curtain;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.ORDERED);

        curtain = new Curtain();
        curtain.setId(UUID.randomUUID());
        curtain.setOrder(order);
        curtain.setWidth(2.5);
        curtain.setHeight(3.0);
    }

    @Test
    @DisplayName("addCurtain should succeed when order is not COMPLETED")
    void addCurtain_shouldSucceed() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(curtainRepository.save(any(Curtain.class))).thenAnswer(i -> {
            Curtain c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CreateCurtainRequest request = new CreateCurtainRequest(order.getId(), 2.0, 2.8);
        ApiResponse<CurtainResponse> response = curtainService.addCurtain(request);

        assertNotNull(response);
        assertEquals("Curtain added successfully", response.getMessage());
        assertEquals(2.0, response.getData().width());
        assertEquals(2.8, response.getData().height());
        verify(curtainRepository).save(any(Curtain.class));
    }

    @Test
    @DisplayName("addCurtain should throw CannotModifyCurtainException when order is COMPLETED")
    void addCurtain_shouldThrow_whenOrderCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        CreateCurtainRequest request = new CreateCurtainRequest(order.getId(), 2.0, 2.8);
        assertThrows(CannotModifyCurtainException.class, () -> curtainService.addCurtain(request));
        verify(curtainRepository, never()).save(any());
    }

    @Test
    @DisplayName("addCurtain should throw OrderNotExistsException when order not found")
    void addCurtain_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        CreateCurtainRequest request = new CreateCurtainRequest(order.getId(), 2.0, 2.8);
        assertThrows(OrderNotExistsException.class, () -> curtainService.addCurtain(request));
    }

    @Test
    @DisplayName("updateCurtain should succeed when order is not COMPLETED")
    void updateCurtain_shouldSucceed() {
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));
        when(curtainRepository.save(any(Curtain.class))).thenAnswer(i -> i.getArgument(0));

        UpdateCurtainRequest request = new UpdateCurtainRequest(3.2, 4.0);
        ApiResponse<CurtainResponse> response = curtainService.updateCurtain(curtain.getId(), request);

        assertNotNull(response);
        assertEquals(3.2, response.getData().width());
        assertEquals(4.0, response.getData().height());
        verify(curtainRepository).save(curtain);
    }

    @Test
    @DisplayName("updateCurtain should throw CannotModifyCurtainException when order is COMPLETED")
    void updateCurtain_shouldThrow_whenOrderCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));

        UpdateCurtainRequest request = new UpdateCurtainRequest(3.2, 4.0);
        assertThrows(CannotModifyCurtainException.class, () -> curtainService.updateCurtain(curtain.getId(), request));
        verify(curtainRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCurtain should succeed when order has more than 1 curtain")
    void deleteCurtain_shouldSucceed() {
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));
        when(curtainRepository.countByOrderId(order.getId())).thenReturn(2L);

        ApiResponse<Void> response = curtainService.deleteCurtain(curtain.getId());

        assertNotNull(response);
        assertEquals("Curtain deleted successfully", response.getMessage());
        verify(curtainRepository).delete(curtain);
    }

    @Test
    @DisplayName("deleteCurtain should throw CannotDeleteLastCurtainException when order has only 1 curtain")
    void deleteCurtain_shouldThrow_whenOnlyOneCurtainLeft() {
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));
        when(curtainRepository.countByOrderId(order.getId())).thenReturn(1L);

        CannotDeleteLastCurtainException ex = assertThrows(CannotDeleteLastCurtainException.class, () -> curtainService.deleteCurtain(curtain.getId()));
        assertTrue(ex.getMessage().contains("at least one curtain"));
        verify(curtainRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCurtain should throw CannotModifyCurtainException when order is COMPLETED")
    void deleteCurtain_shouldThrow_whenOrderCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));

        assertThrows(CannotModifyCurtainException.class, () -> curtainService.deleteCurtain(curtain.getId()));
        verify(curtainRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getCurtainById should succeed when curtain exists")
    void getCurtainById_shouldSucceed() {
        when(curtainRepository.findById(curtain.getId())).thenReturn(Optional.of(curtain));

        ApiResponse<CurtainResponse> response = curtainService.getCurtainById(curtain.getId());

        assertNotNull(response);
        assertEquals(curtain.getId(), response.getData().id());
    }

    @Test
    @DisplayName("getCurtainsByOrderId should return all curtains for order")
    void getCurtainsByOrderId_shouldReturnList() {
        when(orderRepository.existsById(order.getId())).thenReturn(true);
        when(curtainRepository.findByOrderId(order.getId())).thenReturn(List.of(curtain));

        ApiResponse<List<CurtainResponse>> response = curtainService.getCurtainsByOrderId(order.getId());

        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }
}
