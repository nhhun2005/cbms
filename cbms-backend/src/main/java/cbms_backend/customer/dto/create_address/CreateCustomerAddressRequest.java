package cbms_backend.customer.dto.create_address;

import cbms_backend.customer.entity.AddressType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerAddressRequest(
        @NotNull(message = "Address type must not be null")
        AddressType type,

        @NotBlank(message = "Address must not be blank")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        Double longitude,

        Boolean isActive
) {
    public CreateCustomerAddressRequest(AddressType type, String address, Double latitude, Double longitude) {
        this(type, address, latitude, longitude, true);
    }

    public CreateCustomerAddressRequest(AddressType type, String address) {
        this(type, address, null, null, true);
    }

    @AssertTrue(message = "Both latitude and longitude must be provided together")
    public boolean isCoordinatePairValid() {
        return (latitude == null && longitude == null) || (latitude != null && longitude != null);
    }

    public boolean resolveIsActive() {
        return isActive == null || isActive;
    }
}
