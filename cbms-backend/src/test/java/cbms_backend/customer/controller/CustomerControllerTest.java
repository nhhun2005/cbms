package cbms_backend.customer.controller;

import cbms_backend.core.exception.CustomerAddressAlreadyInactiveException;
import cbms_backend.core.exception.CustomerAddressNotExistsException;
import cbms_backend.core.exception.CustomerAlreadyExistsException;
import cbms_backend.core.exception.CustomerNotExistsException;
import cbms_backend.core.exception.GlobalExceptionHandler;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_address.CreateCustomerAddressRequest;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerAddressResponse;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_address.UpdateCustomerAddressRequest;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;
import cbms_backend.customer.entity.AddressType;
import cbms_backend.customer.entity.Gender;
import cbms_backend.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/customers should return 201 when valid request")
    void createCustomer_shouldReturn201_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                id,
                "Nguyen Van A",
                Gender.MALE,
                "123 Duong 3/2, Can Tho",
                "0987654321",
                Collections.emptyList()
        );

        when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer created successfully"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van A\", \"gender\": \"MALE\", \"address\": \"123 Duong 3/2, Can Tho\", \"phone\": \"0987654321\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"));
    }

    @Test
    @DisplayName("POST /api/v1/customers should return 400 when name is blank")
    void createCustomer_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"phone\": \"0987654321\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/customers should return 400 when phone is invalid")
    void createCustomer_shouldReturn400_whenPhoneIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van A\", \"phone\": \"12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/customers should return 409 when phone already exists")
    void createCustomer_shouldReturn409_whenPhoneAlreadyExists() throws Exception {
        when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenThrow(new CustomerAlreadyExistsException("Phone number already exists"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van A\", \"phone\": \"0987654321\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} should return 200 when updated successfully")
    void updateCustomer_shouldReturn200_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                id,
                "Nguyen Van B",
                Gender.FEMALE,
                "New Address",
                "0912345678",
                Collections.emptyList()
        );

        when(customerService.updateCustomer(eq(id), any(UpdateCustomerRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer updated successfully"));

        mockMvc.perform(put("/api/v1/customers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van B\", \"gender\": \"FEMALE\", \"address\": \"New Address\", \"phone\": \"0912345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van B"))
                .andExpect(jsonPath("$.data.phone").value("0912345678"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} should return 404 when customer does not exist")
    void updateCustomer_shouldReturn404_whenCustomerNotExists() throws Exception {
        UUID id = UUID.randomUUID();

        when(customerService.updateCustomer(eq(id), any(UpdateCustomerRequest.class)))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(put("/api/v1/customers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van B\", \"phone\": \"0912345678\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} should return 409 when new phone already exists")
    void updateCustomer_shouldReturn409_whenPhoneAlreadyExists() throws Exception {
        UUID id = UUID.randomUUID();

        when(customerService.updateCustomer(eq(id), any(UpdateCustomerRequest.class)))
                .thenThrow(new CustomerAlreadyExistsException("Phone number already exists"));

        mockMvc.perform(put("/api/v1/customers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nguyen Van B\", \"phone\": \"0912345678\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} should return 200 when customer exists")
    void getCustomerById_shouldReturn200_whenCustomerExists() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                id,
                "Nguyen Van A",
                Gender.MALE,
                "123 Duong 3/2",
                "0987654321",
                Collections.emptyList()
        );

        when(customerService.getCustomerById(id))
                .thenReturn(ApiResponse.success(response, "Customer retrieved successfully"));

        mockMvc.perform(get("/api/v1/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} should return 404 when customer not found")
    void getCustomerById_shouldReturn404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(customerService.getCustomerById(id))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(get("/api/v1/customers/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/phone/{phone} should return 200 when found")
    void getCustomerByPhone_shouldReturn200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                id,
                "Nguyen Van A",
                Gender.MALE,
                "123 Duong 3/2",
                "0987654321",
                Collections.emptyList()
        );

        when(customerService.getCustomerByPhone("0987654321"))
                .thenReturn(ApiResponse.success(response, "Customer retrieved successfully"));

        mockMvc.perform(get("/api/v1/customers/phone/0987654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer retrieved successfully"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"));
    }

    @Test
    @DisplayName("GET /api/v1/customers should return 200 with list of customers")
    void getAllCustomers_shouldReturn200() throws Exception {
        CustomerResponse response = new CustomerResponse(
                UUID.randomUUID(),
                "Nguyen Van A",
                Gender.MALE,
                "123 Duong 3/2",
                "0987654321",
                Collections.emptyList()
        );

        when(customerService.getAllCustomers())
                .thenReturn(ApiResponse.success(List.of(response), "Customers retrieved successfully"));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customers retrieved successfully"))
                .andExpect(jsonPath("$.data[0].name").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 201 when valid request with coordinates")
    void addCustomerAddress_shouldReturn201_whenValidWithCoordinates() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CustomerAddressResponse response = new CustomerAddressResponse(
                addressId,
                customerId,
                AddressType.RESIDENTIAL,
                "123 Duong 3/2, Can Tho",
                10.0299,
                105.7706,
                true
        );

        when(customerService.addCustomerAddress(eq(customerId), any(CreateCustomerAddressRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer address added successfully"));

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"123 Duong 3/2, Can Tho\", \"latitude\": 10.0299, \"longitude\": 105.7706}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer address added successfully"))
                .andExpect(jsonPath("$.data.id").value(addressId.toString()))
                .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.data.type").value("RESIDENTIAL"))
                .andExpect(jsonPath("$.data.address").value("123 Duong 3/2, Can Tho"))
                .andExpect(jsonPath("$.data.latitude").value(10.0299))
                .andExpect(jsonPath("$.data.longitude").value(105.7706))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 201 when coordinates are omitted")
    void addCustomerAddress_shouldReturn201_whenCoordinatesOmitted() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CustomerAddressResponse response = new CustomerAddressResponse(
                addressId,
                customerId,
                AddressType.OFFICE,
                "456 CMT8, Can Tho",
                null,
                null,
                true
        );

        when(customerService.addCustomerAddress(eq(customerId), any(CreateCustomerAddressRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer address added successfully"));

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"OFFICE\", \"address\": \"456 CMT8, Can Tho\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer address added successfully"))
                .andExpect(jsonPath("$.data.type").value("OFFICE"))
                .andExpect(jsonPath("$.data.latitude").doesNotExist())
                .andExpect(jsonPath("$.data.longitude").doesNotExist())
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 404 when customer not found")
    void addCustomerAddress_shouldReturn404_whenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.addCustomerAddress(eq(customerId), any(CreateCustomerAddressRequest.class)))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"123 Duong 3/2, Can Tho\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 400 when address is blank")
    void addCustomerAddress_shouldReturn400_whenAddressIsBlank() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 400 when type is null")
    void addCustomerAddress_shouldReturn400_whenTypeIsNull() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\": \"123 Duong 3/2, Can Tho\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 400 when latitude exceeds Google Maps range (> 90)")
    void addCustomerAddress_shouldReturn400_whenLatitudeExceedsRange() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"123 Duong 3/2\", \"latitude\": 95.0, \"longitude\": 105.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Latitude must be between -90 and 90"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 400 when longitude exceeds Google Maps range (> 180)")
    void addCustomerAddress_shouldReturn400_whenLongitudeExceedsRange() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"123 Duong 3/2\", \"latitude\": 10.0, \"longitude\": 185.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Longitude must be between -180 and 180"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{customerId}/addresses should return 400 when only latitude is provided without longitude")
    void addCustomerAddress_shouldReturn400_whenOnlyLatitudeProvided() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers/{customerId}/addresses", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"123 Duong 3/2\", \"latitude\": 10.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Both latitude and longitude must be provided together"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 200 when valid request with coordinates")
    void updateCustomerAddress_shouldReturn200_whenValidWithCoordinates() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CustomerAddressResponse response = new CustomerAddressResponse(
                addressId,
                customerId,
                AddressType.OFFICE,
                "New Address, Can Tho",
                10.1234,
                105.5678,
                true
        );

        when(customerService.updateCustomerAddress(eq(customerId), eq(addressId), any(UpdateCustomerAddressRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer address updated successfully"));

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"OFFICE\", \"address\": \"New Address, Can Tho\", \"latitude\": 10.1234, \"longitude\": 105.5678}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer address updated successfully"))
                .andExpect(jsonPath("$.data.id").value(addressId.toString()))
                .andExpect(jsonPath("$.data.type").value("OFFICE"))
                .andExpect(jsonPath("$.data.address").value("New Address, Can Tho"))
                .andExpect(jsonPath("$.data.latitude").value(10.1234))
                .andExpect(jsonPath("$.data.longitude").value(105.5678));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 200 when coordinates omitted")
    void updateCustomerAddress_shouldReturn200_whenCoordinatesOmitted() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CustomerAddressResponse response = new CustomerAddressResponse(
                addressId,
                customerId,
                AddressType.RESIDENTIAL,
                "New Address Without Coords",
                null,
                null,
                true
        );

        when(customerService.updateCustomerAddress(eq(customerId), eq(addressId), any(UpdateCustomerAddressRequest.class)))
                .thenReturn(ApiResponse.success(response, "Customer address updated successfully"));

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"New Address Without Coords\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer address updated successfully"))
                .andExpect(jsonPath("$.data.latitude").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 400 when address is blank")
    void updateCustomerAddress_shouldReturn400_whenAddressIsBlank() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 400 when latitude exceeds range")
    void updateCustomerAddress_shouldReturn400_whenLatitudeExceedsRange() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"Valid address\", \"latitude\": 95.0, \"longitude\": 100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Latitude must be between -90 and 90"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 400 when longitude exceeds range")
    void updateCustomerAddress_shouldReturn400_whenLongitudeExceedsRange() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"Valid address\", \"latitude\": 10.0, \"longitude\": 185.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Longitude must be between -180 and 180"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 400 when only latitude provided")
    void updateCustomerAddress_shouldReturn400_whenOnlyLatitudeProvided() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"Valid address\", \"latitude\": 10.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Both latitude and longitude must be provided together"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 404 when customer not found")
    void updateCustomerAddress_shouldReturn404_whenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.updateCustomerAddress(eq(customerId), eq(addressId), any(UpdateCustomerAddressRequest.class)))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"Valid address\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{customerId}/addresses/{addressId} should return 404 when address not found")
    void updateCustomerAddress_shouldReturn404_whenAddressNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.updateCustomerAddress(eq(customerId), eq(addressId), any(UpdateCustomerAddressRequest.class)))
                .thenThrow(new CustomerAddressNotExistsException("Customer address does not exist"));

        mockMvc.perform(put("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"RESIDENTIAL\", \"address\": \"Valid address\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer address does not exist"));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{customerId}/addresses/{addressId}/disable should return 200 when disabled successfully")
    void disableCustomerAddress_shouldReturn200_whenDisabledSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CustomerAddressResponse response = new CustomerAddressResponse(
                addressId,
                customerId,
                AddressType.RESIDENTIAL,
                "Address, Can Tho",
                10.0,
                105.0,
                false
        );

        when(customerService.disableCustomerAddress(customerId, addressId))
                .thenReturn(ApiResponse.success(response, "Customer address disabled successfully"));

        mockMvc.perform(patch("/api/v1/customers/{customerId}/addresses/{addressId}/disable", customerId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer address disabled successfully"))
                .andExpect(jsonPath("$.data.id").value(addressId.toString()))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{customerId}/addresses/{addressId}/disable should return 404 when customer not found")
    void disableCustomerAddress_shouldReturn404_whenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.disableCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(patch("/api/v1/customers/{customerId}/addresses/{addressId}/disable", customerId, addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{customerId}/addresses/{addressId}/disable should return 404 when address not found")
    void disableCustomerAddress_shouldReturn404_whenAddressNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.disableCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerAddressNotExistsException("Customer address does not exist"));

        mockMvc.perform(patch("/api/v1/customers/{customerId}/addresses/{addressId}/disable", customerId, addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer address does not exist"));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{customerId}/addresses/{addressId}/disable should return 409 when address is already inactive")
    void disableCustomerAddress_shouldReturn409_whenAlreadyInactive() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.disableCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerAddressAlreadyInactiveException("Customer address is already inactive"));

        mockMvc.perform(patch("/api/v1/customers/{customerId}/addresses/{addressId}/disable", customerId, addressId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Customer address is already inactive"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} should return 200 when customer exists")
    void deleteCustomer_shouldReturn200_whenCustomerExists() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.deleteCustomer(customerId))
                .thenReturn(ApiResponse.success("Customer deleted successfully"));

        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} should return 404 when customer not found")
    void deleteCustomer_shouldReturn404_whenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.deleteCustomer(customerId))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{customerId}/addresses/{addressId} should return 200 when address exists")
    void deleteCustomerAddress_shouldReturn200_whenAddressExists() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.deleteCustomerAddress(customerId, addressId))
                .thenReturn(ApiResponse.success("Customer address deleted successfully"));

        mockMvc.perform(delete("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer address deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{customerId}/addresses/{addressId} should return 404 when customer not found")
    void deleteCustomerAddress_shouldReturn404_whenCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.deleteCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerNotExistsException("Customer does not exist"));

        mockMvc.perform(delete("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{customerId}/addresses/{addressId} should return 404 when address not found")
    void deleteCustomerAddress_shouldReturn404_whenAddressNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customerService.deleteCustomerAddress(customerId, addressId))
                .thenThrow(new CustomerAddressNotExistsException("Customer address does not exist"));

        mockMvc.perform(delete("/api/v1/customers/{customerId}/addresses/{addressId}", customerId, addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer address does not exist"));
    }
}
