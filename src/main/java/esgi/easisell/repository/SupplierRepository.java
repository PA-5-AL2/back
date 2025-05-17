package esgi.easisell.repository;

import esgi.easisell.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    List<Supplier> findByClientUserId(UUID clientId);

    List<Supplier> findByClientUserIdAndNameContainingIgnoreCase(UUID clientId, String name);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.client.userId = :clientId")
    long countByClientId(@Param("clientId") UUID clientId);
}