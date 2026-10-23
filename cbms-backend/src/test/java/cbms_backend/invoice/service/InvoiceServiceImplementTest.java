package cbms_backend.invoice.service;

import cbms_backend.core.config.TaxConfig;
import cbms_backend.core.exception.InvoiceAlreadyExistsException;
import cbms_backend.core.exception.InvoiceNotExistsException;
import cbms_backend.core.exception.OrderNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.invoice.dto.CreateCustomPriceInvoiceRequest;
import cbms_backend.invoice.dto.CreateInvoiceRequest;
import cbms_backend.invoice.dto.InvoiceResponse;
import cbms_backend.invoice.dto.UpdateInvoiceRequest;
import cbms_backend.invoice.entity.Invoice;
import cbms_backend.invoice.repository.InvoiceRepository;
import cbms_backend.material.entity.Material;
import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderStatus;
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
class InvoiceServiceImplementTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private TaxConfig taxConfig;

    @InjectMocks
    private InvoiceServiceImplement invoiceService;

    private Order order;
    private CustomerInformation customer;
    private Material materialA;
    private Material materialB;
    private StockMovement sm1;
    private StockMovement sm2;

    @BeforeEach
    void setUp() {
        customer = new CustomerInformation();
        customer.setId(UUID.randomUUID());
        customer.setName("Nguyen Van B");
        customer.setPhone("0912345678");

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.SEWN);

        materialA = new Material();
        materialA.setId(UUID.randomUUID());
        materialA.setName("Vải Voan");
        materialA.setPrice(100000.0);

        materialB = new Material();
        materialB.setId(UUID.randomUUID());
        materialB.setName("Vải Gấm");
        materialB.setPrice(200000.0);

        sm1 = new StockMovement();
        sm1.setId(UUID.randomUUID());
        sm1.setOrder(order);
        sm1.setMaterial(materialA);
        sm1.setType(StockMovementType.OUT);
        sm1.setQuantity(5.0); // 5 * 100,000 = 500,000

        sm2 = new StockMovement();
        sm2.setId(UUID.randomUUID());
        sm2.setOrder(order);
        sm2.setMaterial(materialB);
        sm2.setType(StockMovementType.OUT);
        sm2.setQuantity(2.0); // 2 * 200,000 = 400,000
    }

    @Test
    @DisplayName("createInvoice should calculate subtotal from OUT movements and apply global tax rate and discount")
    void createInvoice_shouldSucceed() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1, sm2));
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        CreateInvoiceRequest request = new CreateInvoiceRequest(order.getId(), 10.0);
        ApiResponse<InvoiceResponse> response = invoiceService.createInvoice(request);

        assertNotNull(response);
        assertEquals("Invoice created successfully", response.getMessage());

        InvoiceResponse data = response.getData();
        assertNotNull(data);
        assertEquals(order.getId(), data.orderId());
        assertEquals(customer.getId(), data.customerId());
        assertEquals(900000.0, data.subtotal()); // 500,000 + 400,000
        assertEquals(0.10, data.taxRate());
        assertEquals(10.0, data.discount());
        // gross = 900,000 * 1.10 = 990,000. discount = 10% => total = 990,000 * 0.90 = 891,000
        assertEquals(891000.0, data.total());

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("createInvoice should default discount to 0 when discount is null")
    void createInvoice_shouldDefaultDiscountToZero() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1));
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        CreateInvoiceRequest request = new CreateInvoiceRequest(order.getId(), null);
        ApiResponse<InvoiceResponse> response = invoiceService.createInvoice(request);

        assertEquals(500000.0, response.getData().subtotal());
        assertEquals(0.0, response.getData().discount());
        assertEquals(550000.0, response.getData().total());
    }

    @Test
    @DisplayName("createInvoice should throw OrderNotExistsException when order not found")
    void createInvoice_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

        CreateInvoiceRequest request = new CreateInvoiceRequest(order.getId(), 0.0);
        assertThrows(OrderNotExistsException.class, () -> invoiceService.createInvoice(request));
    }

    @Test
    @DisplayName("createInvoice should throw IllegalArgumentException when order is canceled")
    void createInvoice_shouldThrow_whenOrderCanceled() {
        order.setStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        CreateInvoiceRequest request = new CreateInvoiceRequest(order.getId(), 0.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(request));
    }

    @Test
    @DisplayName("createInvoice should throw InvoiceAlreadyExistsException when order already has invoice")
    void createInvoice_shouldThrow_whenInvoiceAlreadyExists() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(true);

        CreateInvoiceRequest request = new CreateInvoiceRequest(order.getId(), 0.0);
        assertThrows(InvoiceAlreadyExistsException.class, () -> invoiceService.createInvoice(request));
    }

    @Test
    @DisplayName("createInvoice should throw IllegalArgumentException when discount is negative or exceeds 100")
    void createInvoice_shouldThrow_whenDiscountInvalid() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1));
        when(taxConfig.getTaxRate()).thenReturn(0.10);

        CreateInvoiceRequest requestOver = new CreateInvoiceRequest(order.getId(), 105.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(requestOver));

        CreateInvoiceRequest requestNeg = new CreateInvoiceRequest(order.getId(), -5.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(requestNeg));
    }

    @Test
    @DisplayName("createCustomPriceInvoice should calculate discount properly such that subtotal, taxRate, and discount yield the custom price")
    void createCustomPriceInvoice_shouldSucceed() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1, sm2)); // subtotal = 900,000, gross = 990,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        // Customer agrees on price of 891,000 (10% discount)
        CreateCustomPriceInvoiceRequest request = new CreateCustomPriceInvoiceRequest(order.getId(), 891000.0);
        ApiResponse<InvoiceResponse> response = invoiceService.createCustomPriceInvoice(request);

        assertNotNull(response);
        assertEquals("Invoice with custom price created successfully", response.getMessage());

        InvoiceResponse data = response.getData();
        assertEquals(900000.0, data.subtotal());
        assertEquals(0.10, data.taxRate());
        // gross = 990,000, target = 891,000 => discount = ((990,000 - 891,000) / 990,000) * 100 = 10.0%
        assertEquals(10.0, data.discount());
        assertEquals(891000.0, data.total());
    }

    @Test
    @DisplayName("createCustomPriceInvoice should throw IllegalArgumentException when custom price exceeds gross total or is negative")
    void createCustomPriceInvoice_shouldThrow_whenCustomPriceInvalid() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1)); // subtotal = 500,000, gross = 550,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);

        CreateCustomPriceInvoiceRequest requestOver = new CreateCustomPriceInvoiceRequest(order.getId(), 700000.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createCustomPriceInvoice(requestOver));

        CreateCustomPriceInvoiceRequest requestNeg = new CreateCustomPriceInvoiceRequest(order.getId(), -500.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createCustomPriceInvoice(requestNeg));
    }

    @Test
    @DisplayName("getInvoiceById should return invoice when exists")
    void getInvoiceById_shouldSucceed() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setOrder(order);
        invoice.setCustomer(customer);
        invoice.setSubtotal(500000.0);
        invoice.setTaxRate(0.10);
        invoice.setDiscount(0.0);
        invoice.setTotal(550000.0);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceById(invoiceId);
        assertNotNull(response);
        assertEquals(invoiceId, response.getData().id());
    }

    @Test
    @DisplayName("getInvoiceById should throw InvoiceNotExistsException when not found")
    void getInvoiceById_shouldThrow_whenNotFound() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotExistsException.class, () -> invoiceService.getInvoiceById(invoiceId));
    }

    @Test
    @DisplayName("getInvoiceByOrderId should return invoice when exists")
    void getInvoiceByOrderId_shouldSucceed() {
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setOrder(order);
        invoice.setCustomer(customer);
        invoice.setSubtotal(500000.0);
        invoice.setTaxRate(0.10);
        invoice.setDiscount(0.0);
        invoice.setTotal(550000.0);

        when(invoiceRepository.findByOrderId(order.getId())).thenReturn(Optional.of(invoice));

        ApiResponse<InvoiceResponse> response = invoiceService.getInvoiceByOrderId(order.getId());
        assertNotNull(response);
        assertEquals(order.getId(), response.getData().orderId());
    }

    @Test
    @DisplayName("getInvoicesByCustomerId should return list of customer invoices")
    void getInvoicesByCustomerId_shouldReturnList() {
        when(invoiceRepository.findByCustomerId(customer.getId())).thenReturn(List.of());

        ApiResponse<List<InvoiceResponse>> response = invoiceService.getInvoicesByCustomerId(customer.getId());
        assertNotNull(response);
        assertEquals(0, response.getData().size());
    }

    @Test
    @DisplayName("getAllInvoices should return all invoices")
    void getAllInvoices_shouldReturnList() {
        when(invoiceRepository.findAllByOrderByIssuedAtDesc()).thenReturn(List.of());

        ApiResponse<List<InvoiceResponse>> response = invoiceService.getAllInvoices();
        assertNotNull(response);
        assertEquals(0, response.getData().size());
    }

    @Test
    @DisplayName("updateInvoice with standard discount should recalculate amounts properly")
    void updateInvoice_withStandardDiscount_shouldSucceed() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);
        existingInvoice.setSubtotal(500000.0);
        existingInvoice.setTaxRate(0.10);
        existingInvoice.setDiscount(0.0);
        existingInvoice.setTotal(550000.0);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1, sm2)); // subtotal = 900,000, gross = 990,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(20.0, null);
        ApiResponse<InvoiceResponse> response = invoiceService.updateInvoice(invoiceId, request);

        assertNotNull(response);
        assertEquals("Invoice updated successfully", response.getMessage());

        InvoiceResponse data = response.getData();
        assertEquals(900000.0, data.subtotal());
        assertEquals(0.10, data.taxRate());
        assertEquals(20.0, data.discount());
        // gross = 990,000, 20% discount => 792,000
        assertEquals(792000.0, data.total());
    }

    @Test
    @DisplayName("updateInvoice with custom price should recalculate discount percentage and set total to custom price")
    void updateInvoice_withCustomPrice_shouldSucceed() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);
        existingInvoice.setSubtotal(500000.0);
        existingInvoice.setTaxRate(0.10);
        existingInvoice.setDiscount(0.0);
        existingInvoice.setTotal(550000.0);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1, sm2)); // subtotal = 900,000, gross = 990,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        // Custom price 891,000 => 10% discount
        UpdateInvoiceRequest request = new UpdateInvoiceRequest(null, 891000.0);
        ApiResponse<InvoiceResponse> response = invoiceService.updateInvoice(invoiceId, request);

        assertNotNull(response);
        InvoiceResponse data = response.getData();
        assertEquals(900000.0, data.subtotal());
        assertEquals(10.0, data.discount());
        assertEquals(891000.0, data.total());
    }

    @Test
    @DisplayName("updateInvoiceByOrderId should update invoice when found by orderId")
    void updateInvoiceByOrderId_shouldSucceed() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);
        existingInvoice.setSubtotal(500000.0);
        existingInvoice.setTaxRate(0.10);
        existingInvoice.setDiscount(0.0);
        existingInvoice.setTotal(550000.0);

        when(invoiceRepository.findByOrderId(order.getId())).thenReturn(Optional.of(existingInvoice));
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1)); // subtotal = 500,000, gross = 550,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(10.0, null);
        ApiResponse<InvoiceResponse> response = invoiceService.updateInvoiceByOrderId(order.getId(), request);

        assertNotNull(response);
        assertEquals("Invoice updated successfully", response.getMessage());
        assertEquals(495000.0, response.getData().total()); // 550,000 * 0.90
    }

    @Test
    @DisplayName("updateInvoice should throw IllegalArgumentException when both discount and customPrice are specified")
    void updateInvoice_shouldThrow_whenBothDiscountAndCustomPriceSpecified() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(10.0, 800000.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateInvoice(invoiceId, request));
    }

    @Test
    @DisplayName("updateInvoice should throw IllegalArgumentException when customPrice exceeds gross total")
    void updateInvoice_shouldThrow_whenCustomPriceExceedsGrossTotal() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(stockMovementRepository.findByOrderIdAndTypeWithMaterial(order.getId(), StockMovementType.OUT))
                .thenReturn(List.of(sm1)); // gross = 550,000
        when(taxConfig.getTaxRate()).thenReturn(0.10);

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(null, 700000.0);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateInvoice(invoiceId, request));
    }

    @Test
    @DisplayName("updateInvoice should throw InvoiceNotExistsException when invoice not found")
    void updateInvoice_shouldThrow_whenInvoiceNotFound() {
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(10.0, null);
        assertThrows(InvoiceNotExistsException.class, () -> invoiceService.updateInvoice(invoiceId, request));
    }

    @Test
    @DisplayName("updateInvoice should throw IllegalArgumentException when order is canceled")
    void updateInvoice_shouldThrow_whenOrderCanceled() {
        UUID invoiceId = UUID.randomUUID();
        order.setStatus(OrderStatus.CANCELED);
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setOrder(order);
        existingInvoice.setCustomer(customer);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(10.0, null);
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateInvoice(invoiceId, request));
    }
}
