package cbms_backend.customer.service;

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
        address.setIsActive(true);

        customer.addAddress(address);
        CustomerAddress saved = customerAddressRepository.save(address);
        return ApiResponse.success(CustomerAddressResponse.fromEntity(saved), "Customer address added successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CustomerAddressResponse> updateCustomerAddress(UUID customerId, UUID addressId, UpdateCustomerAddressRequest request) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotExistsException("Customer does not exist");
        }

        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new CustomerAddressNotExistsException("Customer address does not exist"));

        address.setType(request.type());
        address.setAddress(request.address().trim());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        CustomerAddress updated = customerAddressRepository.save(address);
        return ApiResponse.success(CustomerAddressResponse.fromEntity(updated), "Customer address updated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<CustomerAddressResponse> disableCustomerAddress(UUID customerId, UUID addressId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotExistsException("Customer does not exist");
        }

        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new CustomerAddressNotExistsException("Customer address does not exist"));

        if (Boolean.FALSE.equals(address.getIsActive())) {
            throw new CustomerAddressAlreadyInactiveException("Customer address is already inactive");
        }

        address.setIsActive(false);
        CustomerAddress saved = customerAddressRepository.save(address);
        return ApiResponse.success(CustomerAddressResponse.fromEntity(saved), "Customer address disabled successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCustomer(UUID id) {
        CustomerInformation customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotExistsException("Customer does not exist"));

        customerRepository.delete(customer);
        return ApiResponse.success("Customer deleted successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteCustomerAddress(UUID customerId, UUID addressId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotExistsException("Customer does not exist");
        }

        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new CustomerAddressNotExistsException("Customer address does not exist"));

        customerAddressRepository.delete(address);
        return ApiResponse.success("Customer address deleted successfully");
    }
}
