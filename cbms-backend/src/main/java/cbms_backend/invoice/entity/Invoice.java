package cbms_backend.invoice.entity;

import cbms_backend.customer.entity.CustomerInformation;
import cbms_backend.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Order must not be null")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotNull(message = "Customer must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerInformation customer;

    @NotNull(message = "Subtotal must not be null")
    @PositiveOrZero(message = "Subtotal must be greater than or equal to 0")
    @Column(nullable = false)
    private Double subtotal;

    @NotNull(message = "Tax rate must not be null")
    @DecimalMin(value = "0.0", message = "Tax rate must be at least 0.0")
    @Column(name = "tax_rate", nullable = false)
    private Double taxRate;

    @NotNull(message = "Total must not be null")
    @PositiveOrZero(message = "Total must be greater than or equal to 0")
    @Column(nullable = false)
    private Double total;

    @NotNull(message = "Issued at must not be null")
    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
