package esgi.easisell.repository;

import esgi.easisell.entity.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, UUID> {

    List<DeletedUser> findByUserTypeOrderByDeletedAtDesc(String userType);

    List<DeletedUser> findByDeletedByOrderByDeletedAtDesc(String deletedBy);

    @Query("SELECT d FROM DeletedUser d WHERE d.deletedAt BETWEEN :startDate AND :endDate ORDER BY d.deletedAt DESC")
    List<DeletedUser> findByDateRangeOrderByDeletedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}
