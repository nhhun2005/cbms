package cbms_backend.customer.repository;

import cbms_backend.customer.entity.CustomerInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerInformationRepository extends JpaRepository<CustomerInformation, UUID> {
    Optional<CustomerInformation> findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, UUID id);

    @Query("SELECT c FROM CustomerInformation c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
    Optional<CustomerInformation> findByIdWithAddresses(@Param("id") UUID id);

    @Query("SELECT c FROM CustomerInformation c LEFT JOIN FETCH c.addresses WHERE c.phone = :phone")
    Optional<CustomerInformation> findByPhoneWithAddresses(@Param("phone") String phone);

    @Query("SELECT DISTINCT c FROM CustomerInformation c LEFT JOIN FETCH c.addresses")
    List<CustomerInformation> findAllWithAddresses();
}
