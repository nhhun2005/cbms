package cbms_backend.customer.dto.get_customer;

import cbms_backend.customer.entity.AddressType;
import cbms_backend.customer.entity.CustomerAddress;

import java.util.UUID;

public record CustomerAddressResponse(
        UUID id,
        AddressType type,
        String address,
        Double latitude,
        Double longitude,
        Boolean isActive
) {
    public static CustomerAddressResponse fromEntity(CustomerAddress customerAddress) {
        if (customerAddress == null) {
            return null;
        }
        return new CustomerAddressResponse(
                customerAddress.getId(),
                customerAddress.getType(),
                customerAddress.getAddress(),
                customerAddress.getLatitude(),
                customerAddress.getLongitude(),
                customerAddress.getIsActive()
        );
    }
}
