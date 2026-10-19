package cbms_backend.customer.service;

import cbms_backend.core.exception.CustomerAlreadyExistsException;
import cbms_backend.core.exception.CustomerNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.entity.Gender;
import cbms_backend.customer.repository.CustomerInformationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplementTest {

    @Mock
    private CustomerInformationRepository customerRepository;

    @InjectMocks
    private CustomerServiceImplement customerService;

    @Test
    @DisplayName("Should create customer successfully when valid data provided")
    void createCustomer_shouldCreateCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Nguyen Van A",
                Gender.MALE,
                "123 Nguyen Trai, Q1",
                "0987654321"
        );

        when(customerRepository.existsByPhone("0987654321")).thenReturn(false);
        when(customerRepository.save(any(CustomerInformation.class))).thenAnswer(invocation -> {
            CustomerInformation c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ApiResponse<CustomerResponse> response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Customer created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertNotNull(response.getData().id());
        assertEquals("Nguyen Van A", response.getData().name());
        assertEquals("0987654321", response.getData().phone());
        assertEquals(Gender.MALE, response.getData().gender());
        assertEquals("123 Nguyen Trai, Q1", response.getData().address());

        verify(customerRepository).save(any(CustomerInformation.class));
    }

    @Test
    @DisplayName("Should create customer without optional address and gender successfully")
    void createCustomer_withoutGenderAndAddress_shouldCreateCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest("Nguyen Van B", "0912345678");

        when(customerRepository.existsByPhone("0912345678")).thenReturn(false);
        when(customerRepository.save(any(CustomerInformation.class))).thenAnswer(invocation -> {
            CustomerInformation c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ApiResponse<CustomerResponse> response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Customer created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("Nguyen Van B", response.getData().name());
        assertEquals("0912345678", response.getData().phone());
        assertNull(response.getData().gender());
        assertNull(response.getData().address());
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when phone already exists on creation")
    void createCustomer_shouldThrowCustomerAlreadyExistsException_whenPhoneExists() {
        CreateCustomerRequest request = new CreateCustomerRequest("Nguyen Van A", "0987654321");

        when(customerRepository.existsByPhone("0987654321")).thenReturn(true);

        CustomerAlreadyExistsException exception = assertThrows(
                CustomerAlreadyExistsException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals("Phone number already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerInformation.class));
    }

    @Test
    @DisplayName("Should update customer successfully when valid data provided")
    void updateCustomer_shouldUpdateCustomerSuccessfully() {
        UUID id = UUID.randomUUID();
        CustomerInformation existing = new CustomerInformation();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setPhone("0987654321");
        existing.setGender(Gender.MALE);
        existing.setAddress("Old Address");

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "New Name",
                Gender.FEMALE,
                "New Address",
                "0909999999"
        );

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByPhoneAndIdNot("0909999999", id)).thenReturn(false);
        when(customerRepository.save(existing)).thenReturn(existing);

        ApiResponse<CustomerResponse> response = customerService.updateCustomer(id, updateRequest);

        assertNotNull(response);
        assertEquals("Customer updated successfully", response.getMessage());
        assertEquals("New Name", response.getData().name());
        assertEquals("0909999999", response.getData().phone());
        assertEquals(Gender.FEMALE, response.getData().gender());
        assertEquals("New Address", response.getData().address());

        verify(customerRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when updating non-existent customer")
    void updateCustomer_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID id = UUID.randomUUID();
        UpdateCustomerRequest request = new UpdateCustomerRequest("Name", Gender.MALE, "Address", "0987654321");

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        CustomerNotExistsException exception = assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.updateCustomer(id, request)
        );

        assertEquals("Customer does not exist", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerInformation.class));
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when updating phone that is already used by another customer")
    void updateCustomer_shouldThrowCustomerAlreadyExistsException_whenPhoneAlreadyUsedByAnotherCustomer() {
        UUID id = UUID.randomUUID();
        CustomerInformation existing = new CustomerInformation();
        existing.setId(id);
        existing.setPhone("0987654321");

        UpdateCustomerRequest request = new UpdateCustomerRequest("Name", Gender.MALE, "Address", "0909999999");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByPhoneAndIdNot("0909999999", id)).thenReturn(true);

        CustomerAlreadyExistsException exception = assertThrows(
                CustomerAlreadyExistsException.class,
                () -> customerService.updateCustomer(id, request)
        );

        assertEquals("Phone number already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerInformation.class));
    }

    @Test
    @DisplayName("Should retrieve customer by id successfully")
    void getCustomerById_shouldReturnCustomer_whenExists() {
        UUID id = UUID.randomUUID();
        CustomerInformation customer = new CustomerInformation();
        customer.setId(id);
        customer.setName("Nguyen Van A");
        customer.setPhone("0987654321");
        customer.setAddresses(new ArrayList<>());

        when(customerRepository.findByIdWithAddresses(id)).thenReturn(Optional.of(customer));

        ApiResponse<CustomerResponse> response = customerService.getCustomerById(id);

        assertNotNull(response);
        assertEquals("Customer retrieved successfully", response.getMessage());
        assertEquals(id, response.getData().id());
        assertEquals("Nguyen Van A", response.getData().name());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when customer not found by id")
    void getCustomerById_shouldThrowCustomerNotExistsException_whenNotFound() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findByIdWithAddresses(id)).thenReturn(Optional.empty());

        CustomerNotExistsException exception = assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.getCustomerById(id)
        );

        assertEquals("Customer does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should retrieve customer by phone successfully")
    void getCustomerByPhone_shouldReturnCustomer_whenExists() {
        UUID id = UUID.randomUUID();
        CustomerInformation customer = new CustomerInformation();
        customer.setId(id);
        customer.setName("Nguyen Van A");
        customer.setPhone("0987654321");

        when(customerRepository.findByPhoneWithAddresses("0987654321")).thenReturn(Optional.of(customer));

        ApiResponse<CustomerResponse> response = customerService.getCustomerByPhone("0987654321");

        assertNotNull(response);
        assertEquals("Customer retrieved successfully", response.getMessage());
        assertEquals("0987654321", response.getData().phone());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when customer not found by phone")
    void getCustomerByPhone_shouldThrowCustomerNotExistsException_whenNotFound() {
        when(customerRepository.findByPhoneWithAddresses("0987654321")).thenReturn(Optional.empty());

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.getCustomerByPhone("0987654321")
        );
    }

    @Test
    @DisplayName("Should retrieve all customers successfully")
    void getAllCustomers_shouldReturnListOfCustomers() {
        CustomerInformation c1 = new CustomerInformation();
        c1.setId(UUID.randomUUID());
        c1.setName("Customer 1");
        c1.setPhone("0987654321");

        CustomerInformation c2 = new CustomerInformation();
        c2.setId(UUID.randomUUID());
        c2.setName("Customer 2");
        c2.setPhone("0912345678");

        when(customerRepository.findAllWithAddresses()).thenReturn(List.of(c1, c2));

        ApiResponse<List<CustomerResponse>> response = customerService.getAllCustomers();

        assertNotNull(response);
        assertEquals("Customers retrieved successfully", response.getMessage());
        assertEquals(2, response.getData().size());
    }
}
