package cbms_backend;

import cbms_backend.customer.entity.AddressType;
import cbms_backend.customer.entity.CustomerAddress;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.entity.Gender;
import cbms_backend.customer.repository.CustomerAddressRepository;
import cbms_backend.customer.repository.CustomerInformationRepository;
import cbms_backend.invoice.entity.Invoice;
import cbms_backend.invoice.repository.InvoiceRepository;
import cbms_backend.material.entity.Material;
import cbms_backend.material.entity.StockMovement;
import cbms_backend.material.entity.StockMovementType;
import cbms_backend.material.entity.Supplier;
import cbms_backend.material.repository.MaterialRepository;
import cbms_backend.material.repository.StockMovementRepository;
import cbms_backend.material.repository.SupplierRepository;
import cbms_backend.order.entity.AssignmentRole;
import cbms_backend.order.entity.Curtain;
import cbms_backend.order.entity.Order;
import cbms_backend.order.entity.OrderAssignment;
import cbms_backend.order.entity.OrderStatus;
import cbms_backend.order.repository.CurtainRepository;
import cbms_backend.order.repository.OrderAssignmentRepository;
import cbms_backend.order.repository.OrderRepository;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CurtainBusinessEntitiesTest {

    @Autowired
    private Validator validator;

    @Autowired
    private CustomerInformationRepository customerInformationRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CurtainRepository curtainRepository;

    @Autowired
    private OrderAssignmentRepository orderAssignmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should successfully persist and retrieve CustomerInformation and CustomerAddress")
    void testCustomerAndAddress() {
        CustomerInformation customer = new CustomerInformation();
        customer.setName("Nguyen Van A");
        customer.setGender(Gender.MALE);
        customer.setPhone("0987654321");
        customer.setAddress("123 Duong 3/2, Can Tho");
        CustomerInformation savedCustomer = customerInformationRepository.save(customer);

        assertThat(savedCustomer.getId()).isNotNull();

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(savedCustomer);
        address.setType(AddressType.RESIDENTIAL);
        address.setAddress("123 Duong 3/2, Can Tho");
        address.setLatitude(10.0333);
        address.setLongitude(105.7833);
        address.setIsActive(true);
        CustomerAddress savedAddress = customerAddressRepository.save(address);

        assertThat(savedAddress.getId()).isNotNull();
        assertThat(savedAddress.getCustomer().getId()).isEqualTo(savedCustomer.getId());

        List<CustomerAddress> addresses = customerAddressRepository.findByCustomerId(savedCustomer.getId());
        assertThat(addresses).hasSize(1);
    }

    @Test
    @DisplayName("Should successfully persist Material, Supplier, and StockMovement")
    void testMaterialSupplierAndStockMovement() {
        Material material = new Material();
        material.setName("Premium Silk Fabric");
        material.setDescription("High quality silk for curtains");
        material.setUnit("meter");
        material.setQuantity(100.0);
        material.setImageUrl("https://example.com/silk.jpg");
        Material savedMaterial = materialRepository.save(material);
        assertThat(savedMaterial.getId()).isNotNull();
        assertThat(savedMaterial.getUpdatedAt()).isNotNull();

        Supplier supplier = new Supplier();
        supplier.setName("Silk Textile Co.");
        supplier.setContact("0912345678");
        Supplier savedSupplier = supplierRepository.save(supplier);
        assertThat(savedSupplier.getId()).isNotNull();

        StockMovement movement = new StockMovement();
        movement.setMaterial(savedMaterial);
        movement.setSupplier(savedSupplier);
        movement.setType(StockMovementType.IN);
        movement.setQuantity(50.0);
        StockMovement savedMovement = stockMovementRepository.save(movement);

        assertThat(savedMovement.getId()).isNotNull();
        assertThat(savedMovement.getCreatedAt()).isNotNull();
        assertThat(savedMovement.getMaterial().getId()).isEqualTo(savedMaterial.getId());
        assertThat(savedMovement.getSupplier().getId()).isEqualTo(savedSupplier.getId());
    }

    @Test
    @DisplayName("Should successfully persist Order, Curtains, OrderAssignment, and Invoice")
    void testOrderWorkflowAndInvoice() {
        // Create User
        User user = new User();
        user.setName("Staff Member");
        user.setPhone("0901234567");
        user.setPassword("secret123");
        user.setStatus(UserStatus.WORKING);
        User savedUser = userRepository.save(user);

        // Create Customer & Address
        CustomerInformation customer = new CustomerInformation();
        customer.setName("Tran Thi B");
        customer.setGender(Gender.FEMALE);
        customer.setPhone("0911223344");
        customer.setAddress("456 Nguyen Trai, Can Tho");
        CustomerInformation savedCustomer = customerInformationRepository.save(customer);

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(savedCustomer);
        address.setType(AddressType.OFFICE);
        address.setAddress("456 Nguyen Trai, Can Tho");
        CustomerAddress savedAddress = customerAddressRepository.save(address);

        // Create Order
        Order order = new Order();
        order.setCustomer(savedCustomer);
        order.setCustomerAddress(savedAddress);
        order.setStatus(OrderStatus.ORDERED);
        Order savedOrder = orderRepository.save(order);
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getCreatedAt()).isNotNull();

        // Create Curtains
        Curtain curtain1 = new Curtain();
        curtain1.setOrder(savedOrder);
        curtain1.setWidth(2.5);
        curtain1.setHeight(3.0);
        curtainRepository.save(curtain1);

        Curtain curtain2 = new Curtain();
        curtain2.setOrder(savedOrder);
        curtain2.setWidth(1.8);
        curtain2.setHeight(2.8);
        curtainRepository.save(curtain2);

        List<Curtain> curtains = curtainRepository.findByOrderId(savedOrder.getId());
        assertThat(curtains).hasSize(2);

        // Create OrderAssignment
        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrder(savedOrder);
        assignment.setUser(savedUser);
        assignment.setRole(AssignmentRole.SEWER);
        OrderAssignment savedAssignment = orderAssignmentRepository.save(assignment);
        assertThat(savedAssignment.getId()).isNotNull();
        assertThat(savedAssignment.getAssignedAt()).isNotNull();

        // Create Invoice
        Invoice invoice = new Invoice();
        invoice.setOrder(savedOrder);
        invoice.setCustomer(savedCustomer);
        invoice.setSubtotal(1500000.0);
        invoice.setTaxRate(0.1);
        invoice.setTotal(1650000.0);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        assertThat(savedInvoice.getId()).isNotNull();
        assertThat(savedInvoice.getIssuedAt()).isNotNull();
        assertThat(savedInvoice.getOrder().getId()).isEqualTo(savedOrder.getId());
        assertThat(savedInvoice.getCustomer().getId()).isEqualTo(savedCustomer.getId());
    }

    @Test
    @DisplayName("Should detect invalid Vietnamese phone number format for CustomerInformation")
    void testCustomerPhoneValidation() {
        CustomerInformation customer = new CustomerInformation();
        customer.setName("Nguyen Van A");
        customer.setGender(Gender.MALE);

        // Invalid: does not start with 0 or wrong length
        customer.setPhone("12345");
        Set<ConstraintViolation<CustomerInformation>> violations = validator.validate(customer);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));

        // Invalid: contains letters
        customer.setPhone("098765432a");
        violations = validator.validate(customer);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));

        // Invalid: 11 digits
        customer.setPhone("09876543210");
        violations = validator.validate(customer);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));

        // Valid: 10 digits starting with 0
        customer.setPhone("0987654321");
        violations = validator.validate(customer);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should detect invalid fields in Material")
    void testMaterialValidation() {
        Material material = new Material();
        material.setName(""); // Blank
        material.setUnit("meter");
        material.setQuantity(-5.0); // Negative quantity

        Set<ConstraintViolation<Material>> violations = validator.validate(material);
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    @DisplayName("Should detect invalid dimensions in Curtain")
    void testCurtainValidation() {
        Curtain curtain = new Curtain();
        curtain.setWidth(0.0); // Not positive
        curtain.setHeight(-1.5); // Negative

        Set<ConstraintViolation<Curtain>> violations = validator.validate(curtain);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("width"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("height"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("order"));
    }

    @Test
    @DisplayName("Should detect invalid amounts in Invoice")
    void testInvoiceValidation() {
        Invoice invoice = new Invoice();
        invoice.setSubtotal(-100.0);
        invoice.setTaxRate(-0.05);
        invoice.setTotal(-105.0);

        Set<ConstraintViolation<Invoice>> violations = validator.validate(invoice);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subtotal"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("taxRate"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("total"));
    }
}

