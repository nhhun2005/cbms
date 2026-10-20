package cbms_backend.customer.service;

import cbms_backend.core.exception.CustomerAlreadyExistsException;
import cbms_backend.core.exception.CustomerNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.customer.dto.create_address.CreateCustomerAddressRequest;
import cbms_backend.customer.dto.create_customer.CreateCustomerRequest;
import cbms_backend.customer.dto.get_customer.CustomerAddressResponse;
import cbms_backend.customer.dto.get_customer.CustomerResponse;
import cbms_backend.customer.dto.update_customer.UpdateCustomerRequest;
import cbms_backend.customer.entity.CustomerAddress;
import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.repository.CustomerAddressRepository;
import cbms_backend.customer.repository.CustomerInformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImplement implements CustomerService {

    private final CustomerInformationRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        String phone = request.phone().trim();
        if (customerRepository.existsByPhone(phone)) {
            throw new CustomerAlreadyExistsException("Phone number already exists");
        }

        CustomerInformation customer = new CustomerInformation();
        customer.setName(request.name().trim());
        customer.setPhone(phone);
        customer.setGender(request.gender());
        customer.setAddress(request.address() != null ? request.address().trim() : null);

        CustomerInformation saved = customerRepository.save(customer);
        return ApiResponse.success(CustomerResponse.fromEntity(saved), "Customer created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(UUID id, UpdateCustomerRequest request) {
        CustomerInformation customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        String phone = request.phone().trim();
        if (customerRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new CustomerAlreadyExistsException("Phone number already exists");
        }

        customer.setName(request.name().trim());
        customer.setPhone(phone);
        customer.setGender(request.gender());
        customer.setAddress(request.address() != null ? request.address().trim() : null);

        CustomerInformation updated = customerRepository.save(customer);
        return ApiResponse.success(CustomerResponse.fromEntity(updated), "Customer updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerResponse> getCustomerById(UUID id) {
        CustomerInformation customer = customerRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        return ApiResponse.success(CustomerResponse.fromEntity(customer), "Customer retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerResponse> getCustomerByPhone(String phone) {
        CustomerInformation customer = customerRepository.findByPhoneWithAddresses(phone.trim())
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        return ApiResponse.success(CustomerResponse.fromEntity(customer), "Customer retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerRepository.findAllWithAddresses().stream()
                .map(CustomerResponse::fromEntity)
                .toList();

        return ApiResponse.success(customers, "Customers retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CustomerAddressResponse> addCustomerAddress(UUID customerId, CreateCustomerAddressRequest request) {
        CustomerInformation customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setType(request.type());
        address.setAddress(request.address().trim());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        address.setIsActive(request.resolveIsActive());

        customer.addAddress(address);
        CustomerAddress saved = customerAddressRepository.save(address);
        return ApiResponse.success(CustomerAddressResponse.fromEntity(saved), "Customer address added successfully");
    }
}
