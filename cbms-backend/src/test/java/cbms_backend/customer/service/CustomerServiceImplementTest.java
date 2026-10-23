package cbms_backend.customer.service;

import cbms_backend.core.exception.CustomerAddressAlreadyActiveException;
import cbms_backend.core.exception.CustomerAddressAlreadyInactiveException;
import cbms_backend.core.exception.CustomerAddressNotExistsException;
import cbms_backend.core.exception.CustomerAlreadyExistsException;
import cbms_backend.core.exception.CustomerNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_address.CreateCustomerAddressRequest;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerAddressResponse;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_address.UpdateCustomerAddressRequest;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;
import cbms_backend.customer.entity.AddressType;
import cbms_backend.customer.entity.CustomerAddress;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.entity.Gender;
import cbms_backend.customer.repository.CustomerAddressRepository;
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

    @Mock
    private CustomerAddressRepository customerAddressRepository;

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

    @Test
    @DisplayName("Should add customer address successfully when valid data with coordinates provided")
    void addCustomerAddress_shouldAddAddressSuccessfully_whenValidDataWithCoordinates() {
        UUID customerId = UUID.randomUUID();
        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);
        customer.setName("Nguyen Van A");
        customer.setPhone("0987654321");

        CreateCustomerAddressRequest request = new CreateCustomerAddressRequest(
                AddressType.RESIDENTIAL,
                "123 Duong 3/2, Can Tho",
                10.0299,
                105.7706
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> {
            CustomerAddress addr = invocation.getArgument(0);
            addr.setId(UUID.randomUUID());
            return addr;
        });

        ApiResponse<CustomerAddressResponse> response = customerService.addCustomerAddress(customerId, request);

        assertNotNull(response);
        assertEquals("Customer address added successfully", response.getMessage());
        assertNotNull(response.getData());
        assertNotNull(response.getData().id());
        assertEquals(customerId, response.getData().customerId());
        assertEquals(AddressType.RESIDENTIAL, response.getData().type());
        assertEquals("123 Duong 3/2, Can Tho", response.getData().address());
        assertEquals(10.0299, response.getData().latitude());
        assertEquals(105.7706, response.getData().longitude());
        assertTrue(response.getData().isActive());

        verify(customerRepository).findById(customerId);
        verify(customerAddressRepository).save(any(CustomerAddress.class));
    }

    @Test
    @DisplayName("Should add customer address successfully when coordinates are null")
    void addCustomerAddress_shouldAddAddressSuccessfully_whenCoordinatesAreNull() {
        UUID customerId = UUID.randomUUID();
        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);
        customer.setName("Nguyen Van A");
        customer.setPhone("0987654321");

        CreateCustomerAddressRequest request = new CreateCustomerAddressRequest(
                AddressType.OFFICE,
                "456 CMT8, Can Tho"
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> {
            CustomerAddress addr = invocation.getArgument(0);
            addr.setId(UUID.randomUUID());
            return addr;
        });

        ApiResponse<CustomerAddressResponse> response = customerService.addCustomerAddress(customerId, request);

        assertNotNull(response);
        assertEquals("Customer address added successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(customerId, response.getData().customerId());
        assertEquals(AddressType.OFFICE, response.getData().type());
        assertEquals("456 CMT8, Can Tho", response.getData().address());
        assertNull(response.getData().latitude());
        assertNull(response.getData().longitude());
        assertTrue(response.getData().isActive());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when adding address to non-existent customer")
    void addCustomerAddress_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        CreateCustomerAddressRequest request = new CreateCustomerAddressRequest(
                AddressType.RESIDENTIAL,
                "123 Duong 3/2, Can Tho",
                10.0299,
                105.7706
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.addCustomerAddress(customerId, request)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update customer address successfully when valid data provided")
    void updateCustomerAddress_shouldUpdateAddressSuccessfully_whenValidDataProvided() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setId(addressId);
        existingAddress.setCustomer(customer);
        existingAddress.setType(AddressType.RESIDENTIAL);
        existingAddress.setAddress("Old Address");
        existingAddress.setIsActive(true);

        UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest(
                AddressType.OFFICE,
                "New Updated Address",
                10.1234,
                105.5678
        );

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(existingAddress));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<CustomerAddressResponse> response = customerService.updateCustomerAddress(customerId, addressId, request);

        assertNotNull(response);
        assertEquals("Customer address updated successfully", response.getMessage());
        assertEquals(AddressType.OFFICE, response.getData().type());
        assertEquals("New Updated Address", response.getData().address());
        assertEquals(10.1234, response.getData().latitude());
        assertEquals(105.5678, response.getData().longitude());
        assertTrue(response.getData().isActive());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when updating address of non-existent customer")
    void updateCustomerAddress_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest(
                AddressType.OFFICE,
                "New Address"
        );

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.updateCustomerAddress(customerId, addressId, request)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerAddressNotExistsException when updating non-existent address")
    void updateCustomerAddress_shouldThrowCustomerAddressNotExistsException_whenAddressNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest(
                AddressType.OFFICE,
                "New Address"
        );

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerAddressNotExistsException.class,
                () -> customerService.updateCustomerAddress(customerId, addressId, request)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should disable customer address successfully when currently active")
    void disableCustomerAddress_shouldDisableAddressSuccessfully_whenActive() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setId(addressId);
        existingAddress.setCustomer(customer);
        existingAddress.setType(AddressType.RESIDENTIAL);
        existingAddress.setAddress("Active Address");
        existingAddress.setIsActive(true);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(existingAddress));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<CustomerAddressResponse> response = customerService.disableCustomerAddress(customerId, addressId);

        assertNotNull(response);
        assertEquals("Customer address disabled successfully", response.getMessage());
        assertFalse(response.getData().isActive());
        verify(customerAddressRepository).save(existingAddress);
    }

    @Test
    @DisplayName("Should throw CustomerAddressAlreadyInactiveException when disabling already inactive address")
    void disableCustomerAddress_shouldThrowCustomerAddressAlreadyInactiveException_whenAlreadyInactive() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setId(addressId);
        existingAddress.setCustomer(customer);
        existingAddress.setIsActive(false);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(existingAddress));

        assertThrows(
                CustomerAddressAlreadyInactiveException.class,
                () -> customerService.disableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when disabling address of non-existent customer")
    void disableCustomerAddress_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.disableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerAddressNotExistsException when disabling non-existent address")
    void disableCustomerAddress_shouldThrowCustomerAddressNotExistsException_whenAddressNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerAddressNotExistsException.class,
                () -> customerService.disableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should enable customer address successfully when currently inactive")
    void enableCustomerAddress_shouldEnableAddressSuccessfully_whenInactive() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setId(addressId);
        existingAddress.setCustomer(customer);
        existingAddress.setType(AddressType.RESIDENTIAL);
        existingAddress.setAddress("Inactive Address");
        existingAddress.setIsActive(false);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(existingAddress));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<CustomerAddressResponse> response = customerService.enableCustomerAddress(customerId, addressId);

        assertNotNull(response);
        assertEquals("Customer address enabled successfully", response.getMessage());
        assertTrue(response.getData().isActive());
        verify(customerAddressRepository).save(existingAddress);
    }

    @Test
    @DisplayName("Should throw CustomerAddressAlreadyActiveException when enabling already active address")
    void enableCustomerAddress_shouldThrowCustomerAddressAlreadyActiveException_whenAlreadyActive() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setId(addressId);
        existingAddress.setCustomer(customer);
        existingAddress.setIsActive(true);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(existingAddress));

        assertThrows(
                CustomerAddressAlreadyActiveException.class,
                () -> customerService.enableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when enabling address of non-existent customer")
    void enableCustomerAddress_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.enableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerAddressNotExistsException when enabling non-existent address")
    void enableCustomerAddress_shouldThrowCustomerAddressNotExistsException_whenAddressNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerAddressNotExistsException.class,
                () -> customerService.enableCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete customer successfully when customer exists")
    void deleteCustomer_shouldDeleteSuccessfully_whenCustomerExists() {
        UUID customerId = UUID.randomUUID();
        CustomerInformation customer = new CustomerInformation();
        customer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        ApiResponse<Void> response = customerService.deleteCustomer(customerId);

        assertNotNull(response);
        assertEquals("Customer deleted successfully", response.getMessage());
        verify(customerRepository).delete(customer);
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when deleting non-existent customer")
    void deleteCustomer_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.deleteCustomer(customerId)
        );
        verify(customerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete customer address successfully when address exists")
    void deleteCustomerAddress_shouldDeleteSuccessfully_whenAddressExists() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomerAddress address = new CustomerAddress();
        address.setId(addressId);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(address));

        ApiResponse<Void> response = customerService.deleteCustomerAddress(customerId, addressId);

        assertNotNull(response);
        assertEquals("Customer address deleted successfully", response.getMessage());
        verify(customerAddressRepository).delete(address);
    }

    @Test
    @DisplayName("Should throw CustomerNotExistsException when deleting address of non-existent customer")
    void deleteCustomerAddress_shouldThrowCustomerNotExistsException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(
                CustomerNotExistsException.class,
                () -> customerService.deleteCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw CustomerAddressNotExistsException when deleting non-existent address")
    void deleteCustomerAddress_shouldThrowCustomerAddressNotExistsException_whenAddressNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerAddressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThrows(
                CustomerAddressNotExistsException.class,
                () -> customerService.deleteCustomerAddress(customerId, addressId)
        );
        verify(customerAddressRepository, never()).delete(any());
    }
}
