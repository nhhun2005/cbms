package cbms_backend.order.service;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.entity.AddressType;
import cbms_backend.customer.entity.CustomerAddress;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.repository.CustomerAddressRepository;
import cbms_backend.customer.repository.CustomerInformationRepository;
import cbms_backend.invoice.repository.InvoiceRepository;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.order.dto.create_order.CreateOrderRequest;
import cbms_backend.order.dto.create_order.CurtainItemRequest;
import cbms_backend.order.dto.get_order.OrderResponse;
import cbms_backend.order.dto.update_order.UpdateOrderAddressRequest;
import cbms_backend.order.dto.update_order.UpdateOrderStatusRequest;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
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
class OrderServiceImplementTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerInformationRepository customerRepository;

    @Mock
    private CustomerAddressRepository customerAddressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private OrderServiceImplement orderService;

    private CustomerInformation customer;
    private CustomerAddress address;
    private User creator;

    @BeforeEach
    void setUp() {
        customer = new CustomerInformation();
        customer.setId(UUID.randomUUID());
        customer.setName("Nguyen Van A");
        customer.setPhone("0912345678");

        address = new CustomerAddress();
        address.setId(UUID.randomUUID());
        address.setCustomer(customer);
        address.setType(AddressType.RESIDENTIAL);
        address.setAddress("123 Duong 3/2, Can Tho");
        address.setIsActive(true);

        creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setName("Staff Creator");
        creator.setPhone("0987654321");
        creator.setStatus(UserStatus.WORKING);
    }

    @Test
    @DisplayName("createOrder should succeed and create Creator assignment and Curtains")
    void createOrder_shouldSucceed() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(
                        new CurtainItemRequest(2.5, 3.0),
                        new CurtainItemRequest(1.8, 2.2)
                )
        );

        ApiResponse<OrderResponse> response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("Order created successfully", response.getMessage());
        OrderResponse orderResponse = response.getData();
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.ORDERED, orderResponse.status());
        assertEquals(2, orderResponse.curtains().size());
        assertEquals(1, orderResponse.assignments().size());
        assertEquals(AssignmentRole.CREATOR, orderResponse.assignments().get(0).role());
        assertEquals(creator.getId(), orderResponse.assignments().get(0).userId());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder should throw CustomerNotExistsException when customer not found")
    void createOrder_shouldThrow_whenCustomerNotFound() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        assertThrows(CustomerNotExistsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("createOrder should throw CustomerAddressNotExistsException when address not found")
    void createOrder_shouldThrow_whenAddressNotFound() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        assertThrows(CustomerAddressNotExistsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("createOrder should throw IllegalArgumentException when address belongs to different customer")
    void createOrder_shouldThrow_whenAddressBelongsToOtherCustomer() {
        CustomerInformation otherCustomer = new CustomerInformation();
        otherCustomer.setId(UUID.randomUUID());
        address.setCustomer(otherCustomer);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("does not belong to the specified customer"));
    }

    @Test
    @DisplayName("createOrder should throw IllegalArgumentException when address is inactive")
    void createOrder_shouldThrow_whenAddressIsInactive() {
        address.setIsActive(false);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("createOrder should throw UserNotExistsException when creator not found")
    void createOrder_shouldThrow_whenCreatorNotFound() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        assertThrows(UserNotExistsException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("createOrder should throw IllegalArgumentException when creator user is INACTIVE")
    void createOrder_shouldThrow_whenCreatorNotWorking() {
        creator.setStatus(UserStatus.INACTIVE);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of(new CurtainItemRequest(2.0, 2.0))
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("Creator user is not currently working"));
    }

    @Test
    @DisplayName("createOrder should throw IllegalArgumentException when curtains list is empty")
    void createOrder_shouldThrow_whenCurtainsEmpty() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                address.getId(),
                creator.getId(),
                List.of()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("at least one curtain"));
    }

    @Test
    @DisplayName("getOrderById should succeed when order exists")
    void getOrderById_shouldSucceed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setCustomerAddress(address);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ApiResponse<OrderResponse> response = orderService.getOrderById(orderId);

        assertNotNull(response);
        assertEquals("Order retrieved successfully", response.getMessage());
        assertEquals(orderId, response.getData().id());
    }

    @Test
    @DisplayName("getOrderById should throw OrderNotExistsException when not found")
    void getOrderById_shouldThrow_whenNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistsException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    @DisplayName("getAllOrders should return filtered order list")
    void getAllOrders_shouldReturnList() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setCustomerAddress(address);

        when(orderRepository.findOrdersWithFilters(OrderStatus.ORDERED, customer.getId(), null))
                .thenReturn(List.of(order));

        ApiResponse<List<OrderResponse>> response = orderService.getAllOrders(OrderStatus.ORDERED, customer.getId(), null);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    @DisplayName("updateOrderStatus should succeed when order is not COMPLETED")
    void updateOrderStatus_shouldSucceed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ORDERED);
        order.setCustomer(customer);
        order.setCustomerAddress(address);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<OrderResponse> response = orderService.updateOrderStatus(orderId, new UpdateOrderStatusRequest(OrderStatus.SEWING));

        assertNotNull(response);
        assertEquals(OrderStatus.SEWING, response.getData().status());
    }

    @Test
    @DisplayName("updateOrderStatus should throw IllegalArgumentException when order is already COMPLETED")
    void updateOrderStatus_shouldThrow_whenCompleted() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELED);
        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatus(orderId, request));
    }

    @Test
    @DisplayName("updateOrderAddress should succeed when order is not COMPLETED and address is valid")
    void updateOrderAddress_shouldSucceed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.ORDERED);

        CustomerAddress newAddress = new CustomerAddress();
        newAddress.setId(UUID.randomUUID());
        newAddress.setCustomer(customer);
        newAddress.setAddress("456 Tran Phu, Can Tho");
        newAddress.setIsActive(true);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(customerAddressRepository.findById(newAddress.getId())).thenReturn(Optional.of(newAddress));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<OrderResponse> response = orderService.updateOrderAddress(orderId, new UpdateOrderAddressRequest(newAddress.getId()));

        assertNotNull(response);
        assertEquals("456 Tran Phu, Can Tho", response.getData().customerAddress().address());
    }

    @Test
    @DisplayName("deleteOrder should succeed when order is ORDERED and has no stock movements or invoice")
    void deleteOrder_shouldSucceed() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ORDERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stockMovementRepository.existsByOrderId(orderId)).thenReturn(false);
        when(invoiceRepository.existsByOrderId(orderId)).thenReturn(false);

        ApiResponse<Void> response = orderService.deleteOrder(orderId);

        assertNotNull(response);
        assertEquals("Order deleted successfully", response.getMessage());
        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("deleteOrder should throw CannotDeleteOrderException when order is not ORDERED")
    void deleteOrder_shouldThrow_whenNotOrdered() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.SEWING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CannotDeleteOrderException ex = assertThrows(CannotDeleteOrderException.class, () -> orderService.deleteOrder(orderId));
        assertTrue(ex.getMessage().contains("status is not ORDERED"));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteOrder should throw CannotDeleteOrderException when order has stock movements")
    void deleteOrder_shouldThrow_whenHasStockMovements() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ORDERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stockMovementRepository.existsByOrderId(orderId)).thenReturn(true);

        CannotDeleteOrderException ex = assertThrows(CannotDeleteOrderException.class, () -> orderService.deleteOrder(orderId));
        assertTrue(ex.getMessage().contains("stock movements"));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteOrder should throw CannotDeleteOrderException when order has invoice")
    void deleteOrder_shouldThrow_whenHasInvoice() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ORDERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stockMovementRepository.existsByOrderId(orderId)).thenReturn(false);
        when(invoiceRepository.existsByOrderId(orderId)).thenReturn(true);

        CannotDeleteOrderException ex = assertThrows(CannotDeleteOrderException.class, () -> orderService.deleteOrder(orderId));
        assertTrue(ex.getMessage().contains("invoice"));
        verify(orderRepository, never()).delete(any());
    }
}
