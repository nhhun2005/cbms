package cbms_backend.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "curtains")
public class Curtain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Order must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Width must not be null")
    @Positive(message = "Width must be greater than 0")
    @Column(nullable = false)
    private Double width;

    @NotNull(message = "Height must not be null")
    @Positive(message = "Height must be greater than 0")
    @Column(nullable = false)
    private Double height;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curtain other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
