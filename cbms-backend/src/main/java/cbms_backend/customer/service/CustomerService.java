package cbms_backend.customer.service;

import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    ApiResponse<CustomerResponse> createCustomer(CreateCustomerRequest request);
    ApiResponse<CustomerResponse> updateCustomer(UUID id, UpdateCustomerRequest request);
    ApiResponse<CustomerResponse> getCustomerById(UUID id);
    ApiResponse<CustomerResponse> getCustomerByPhone(String phone);
    ApiResponse<List<CustomerResponse>> getAllCustomers();
}
