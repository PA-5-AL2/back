package esgi.easisell.repository;

import esgi.easisell.entity.ClientRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRequestRepository extends JpaRepository<ClientRequest, UUID> {

    List<ClientRequest> findByStatusOrderByRequestDateDesc(ClientRequest.RequestStatus status);

    List<ClientRequest> findByEmailIgnoreCase(String email);

    @Query("SELECT COUNT(cr) FROM ClientRequest cr WHERE cr.status = :status")
    long countByStatus(@Param("status") ClientRequest.RequestStatus status);

    List<ClientRequest> findAllByOrderByRequestDateDesc();
}