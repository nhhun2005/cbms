package cbms_backend.customer.dto.create_customer;

import cbms_backend.customer.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = "Customer name must not be blank")
        @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
        String name,

        Gender gender,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @NotBlank(message = "Phone must not be blank")
        @Size(min = 10, max = 10, message = "Phone must contain 10 digits")
        @Pattern(regexp = "0\\d{9}", message = "Phone must be a valid Vietnamese phone number")
        String phone
) {
    public CreateCustomerRequest(String name, String phone) {
        this(name, null, null, phone);
    }
}
