package esgi.easisell.repository;

import esgi.easisell.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByClientUserId(UUID clientId);

    List<Category> findByClientUserIdAndNameContainingIgnoreCase(UUID clientId, String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.client.userId = :clientId AND c.categoryId = :categoryId")
    boolean existsByClientUserIdAndCategoryId(@Param("clientId") UUID clientId, @Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.client.userId = :clientId")
    long countByClientId(@Param("clientId") UUID clientId);
}