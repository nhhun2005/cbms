package cbms_backend.order.service;

import cbms_backend.core.exception.*;
import cbms_backend.core.util.ApiResponse;
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
import cbms_backend.order.entity.Curtain;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderAssignment;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.repository.OrderRepository;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImplement implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerInformationRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public ApiResponse<OrderResponse> createOrder(CreateOrderRequest request) {
        CustomerInformation customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        CustomerAddress address = customerAddressRepository.findById(request.customerAddressId())
                .orElseThrow(() -> new CustomerAddressNotExistsException("Customer address does not exist"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Customer address does not belong to the specified customer");
        }

        if (Boolean.FALSE.equals(address.getIsActive())) {
            throw new IllegalArgumentException("Customer address is inactive");
        }

        User creator = userRepository.findById(request.creatorUserId())
                .orElseThrow(() -> new UserNotExistsException("Creator user does not exist"));

        if (creator.getStatus() != UserStatus.WORKING) {
            throw new IllegalArgumentException("Creator user is not currently working");
        }

        if (request.curtains() == null || request.curtains().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one curtain");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setCustomerAddress(address);
        order.setStatus(OrderStatus.ORDERED);
        order.setCreatedAt(Instant.now());

        // Create Creator assignment
        OrderAssignment assignment = new OrderAssignment();
        assignment.setUser(creator);
        assignment.setRole(AssignmentRole.CREATOR);
        assignment.setAssignedAt(Instant.now());
        order.addAssignment(assignment);

        // Create Curtains
        for (CurtainItemRequest item : request.curtains()) {
            Curtain curtain = new Curtain();
            curtain.setWidth(item.width());
            curtain.setHeight(item.height());
            order.addCurtain(curtain);
        }

        Order saved = orderRepository.save(order);
        return ApiResponse.success(OrderResponse.fromEntity(saved), "Order created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrderResponse> getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        return ApiResponse.success(OrderResponse.fromEntity(order), "Order retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponse>> getAllOrders(OrderStatus status, UUID customerId, UUID userId) {
        List<Order> orders = orderRepository.findOrdersWithFilters(status, customerId, userId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();

        return ApiResponse.success(responses, "Orders retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot update status of a completed order");
        }

        order.setStatus(request.status());
        Order saved = orderRepository.save(order);
        return ApiResponse.success(OrderResponse.fromEntity(saved), "Order status updated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateOrderAddress(UUID id, UpdateOrderAddressRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot update address of a completed order");
        }

        CustomerAddress address = customerAddressRepository.findById(request.customerAddressId())
                .orElseThrow(() -> new CustomerAddressNotExistsException("Customer address does not exist"));

        if (!address.getCustomer().getId().equals(order.getCustomer().getId())) {
            throw new IllegalArgumentException("Customer address does not belong to the order's customer");
        }

        if (Boolean.FALSE.equals(address.getIsActive())) {
            throw new IllegalArgumentException("Customer address is inactive");
        }

        order.setCustomerAddress(address);
        Order saved = orderRepository.save(order);
        return ApiResponse.success(OrderResponse.fromEntity(saved), "Order address updated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotExistsException("Order does not exist"));

        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new CannotDeleteOrderException("Cannot delete order because its status is not ORDERED");
        }

        if (stockMovementRepository.existsByOrderId(id)) {
            throw new CannotDeleteOrderException("Cannot delete order because stock movements are associated with it");
        }

        if (invoiceRepository.existsByOrderId(id)) {
            throw new CannotDeleteOrderException("Cannot delete order because an invoice has already been issued for it");
        }

        orderRepository.delete(order);
        return ApiResponse.success("Order deleted successfully");
    }
}
