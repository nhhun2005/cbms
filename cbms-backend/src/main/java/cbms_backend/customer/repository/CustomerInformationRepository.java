package cbms_backend.customer.repository;

import cbms_backend.customer.entity.CustomerInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerInformationRepository extends JpaRepository<CustomerInformation, UUID> {
    Optional<CustomerInformation> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
