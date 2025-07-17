package esgi.easisell.repository;

import esgi.easisell.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  REPOSITORY PROMOTION - ACCÈS AUX DONNÉES
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    // ========== RECHERCHES PAR PRODUIT ==========

    /**
     * Trouve toutes les promotions d'un produit
     */
    List<Promotion> findByProductProductId(UUID productId);

    /**
     * Trouve la promotion active d'un produit
     */
    @Query("SELECT p FROM Promotion p WHERE p.product.productId = :productId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    Optional<Promotion> findActivePromotionByProduct(@Param("productId") UUID productId,
                                                     @Param("now") Timestamp now);

    /**
     * Vérifie si un produit a une promotion active
     */
    @Query("SELECT COUNT(p) > 0 FROM Promotion p WHERE p.product.productId = :productId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    boolean hasActivePromotion(@Param("productId") UUID productId, @Param("now") Timestamp now);

    // ========== RECHERCHES PAR CLIENT ==========

    /**
     * Trouve toutes les promotions d'un client
     */
    List<Promotion> findByClientUserId(UUID clientId);

    /**
     * Trouve les promotions actives d'un client
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotionsByClient(@Param("clientId") UUID clientId,
                                                 @Param("now") Timestamp now);

    /**
     * Trouve les promotions qui vont commencer bientôt
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND p.startDate > :now AND p.startDate <= :futureDate")
    List<Promotion> findUpcomingPromotions(@Param("clientId") UUID clientId,
                                           @Param("now") Timestamp now,
                                           @Param("futureDate") Timestamp futureDate);

    /**
     * Trouve les promotions qui expirent bientôt
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND p.endDate > :now AND p.endDate <= :soonDate")
    List<Promotion> findExpiringSoonPromotions(@Param("clientId") UUID clientId,
                                               @Param("now") Timestamp now,
                                               @Param("soonDate") Timestamp soonDate);

    // ========== RECHERCHES PAR STATUT ==========

    /**
     * Trouve les promotions expirées et encore actives (pour nettoyage)
     */
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.endDate < :now")
    List<Promotion> findExpiredActivePromotions(@Param("now") Timestamp now);

    /**
     * Trouve les promotions par type
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId AND p.promotionType = :type")
    List<Promotion> findByClientAndType(@Param("clientId") UUID clientId,
                                        @Param("type") Promotion.PromotionType type);

    // ========== RECHERCHES PAR PÉRIODE ==========

    /**
     * Trouve les promotions dans une période donnée
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND ((p.startDate BETWEEN :startDate AND :endDate) " +
            "OR (p.endDate BETWEEN :startDate AND :endDate) " +
            "OR (p.startDate <= :startDate AND p.endDate >= :endDate))")
    List<Promotion> findPromotionsInPeriod(@Param("clientId") UUID clientId,
                                           @Param("startDate") Timestamp startDate,
                                           @Param("endDate") Timestamp endDate);

    // ========== STATISTIQUES ==========

    /**
     * Compte les promotions actives d'un client
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    long countActivePromotionsByClient(@Param("clientId") UUID clientId, @Param("now") Timestamp now);

    /**
     * Compte les produits en promotion d'un client
     */
    @Query("SELECT COUNT(DISTINCT p.product) FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate")
    long countProductsOnPromotionByClient(@Param("clientId") UUID clientId, @Param("now") Timestamp now);

    // ========== OPÉRATIONS DE MAINTENANCE ==========

    /**
     * Désactive automatiquement les promotions expirées
     */
    @Modifying
    @Transactional
    @Query("UPDATE Promotion p SET p.isActive = false, p.updatedAt = :now " +
            "WHERE p.isActive = true AND p.endDate < :now")
    int deactivateExpiredPromotions(@Param("now") Timestamp now);

    /**
     * Désactive toutes les promotions d'un produit
     */
    @Modifying
    @Transactional
    @Query("UPDATE Promotion p SET p.isActive = false, p.updatedAt = :now " +
            "WHERE p.product.productId = :productId AND p.isActive = true")
    int deactivateAllPromotionsForProduct(@Param("productId") UUID productId, @Param("now") Timestamp now);

    /**
     * Trouve les conflits de promotion (plusieurs promotions actives pour le même produit)
     */
    @Query("SELECT p FROM Promotion p WHERE p.product.productId IN " +
            "(SELECT p2.product.productId FROM Promotion p2 " +
            "WHERE p2.isActive = true AND :now BETWEEN p2.startDate AND p2.endDate " +
            "GROUP BY p2.product.productId HAVING COUNT(p2) > 1)")
    List<Promotion> findPromotionConflicts(@Param("now") Timestamp now);

    // ========== RECHERCHES AVANCÉES ==========

    /**
     * Trouve les promotions avec un discount minimum
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate " +
            "AND ((p.discountPercentage >= :minDiscount AND p.promotionType = 'PERCENTAGE') " +
            "OR (p.discountAmount >= :minAmount AND p.promotionType = 'FIXED_AMOUNT'))")
    List<Promotion> findPromotionsWithMinimumDiscount(@Param("clientId") UUID clientId,
                                                      @Param("now") Timestamp now,
                                                      @Param("minDiscount") Double minDiscount,
                                                      @Param("minAmount") Double minAmount);

    /**
     * Trouve les meilleures promotions (plus gros pourcentage)
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND p.isActive = true AND :now BETWEEN p.startDate AND p.endDate " +
            "ORDER BY p.discountPercentage DESC")
    List<Promotion> findBestPromotionsByPercentage(@Param("clientId") UUID clientId,
                                                   @Param("now") Timestamp now);

    /**
     * Recherche de promotions par nom de produit
     */
    @Query("SELECT p FROM Promotion p WHERE p.client.userId = :clientId " +
            "AND LOWER(p.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Promotion> findPromotionsByProductName(@Param("clientId") UUID clientId,
                                                @Param("productName") String productName);
}