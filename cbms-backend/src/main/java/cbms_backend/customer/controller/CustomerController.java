package cbms_backend.customer.controller;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_address.CreateCustomerAddressRequest;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerAddressResponse;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_address.UpdateCustomerAddressRequest;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;
import cbms_backend.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(request));
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> addCustomerAddress(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateCustomerAddressRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.addCustomerAddress(customerId, request));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> updateCustomerAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateCustomerAddressRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomerAddress(customerId, addressId, request));
    }

    @PatchMapping("/{customerId}/addresses/{addressId}/disable")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> disableCustomerAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(customerService.disableCustomerAddress(customerId, addressId));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.ok(customerService.deleteCustomerAddress(customerId, addressId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
