package cbms_backend.order.entity;

import cbms_backend.customer.entity.CustomerAddress;
import cbms_backend.customer.entity.CustomerInformation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Customer must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerInformation customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_address_id")
    private CustomerAddress customerAddress;

    @NotNull(message = "Order status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.ORDERED;

    @NotNull(message = "Created at must not be null")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Curtain> curtains = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderAssignment> assignments = new ArrayList<>();

    public void addCurtain(Curtain curtain) {
        if (curtains == null) {
            curtains = new ArrayList<>();
        }
        curtains.add(curtain);
        curtain.setOrder(this);
    }

    public void removeCurtain(Curtain curtain) {
        if (curtains != null) {
            curtains.remove(curtain);
        }
        curtain.setOrder(null);
    }

    public void addAssignment(OrderAssignment assignment) {
        if (assignments == null) {
            assignments = new ArrayList<>();
        }
        assignments.add(assignment);
        assignment.setOrder(this);
    }

    public void removeAssignment(OrderAssignment assignment) {
        if (assignments != null) {
            assignments.remove(assignment);
        }
        assignment.setOrder(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
