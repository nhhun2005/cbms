package cbms_backend.customer.dto.get_customer;

import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.customer.entity.Gender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        Gender gender,
        String address,
        String phone,
        List<CustomerAddressResponse> addresses
) {
    public static CustomerResponse fromEntity(CustomerInformation customer) {
        if (customer == null) {
            return null;
        }
        List<CustomerAddressResponse> addressResponses = Collections.emptyList();
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            addressResponses = customer.getAddresses().stream()
                    .map(CustomerAddressResponse::fromEntity)
                    .toList();
        }
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getGender(),
                customer.getAddress(),
                customer.getPhone(),
                addressResponses
        );
    }
}
