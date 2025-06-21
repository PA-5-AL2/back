package esgi.easisell.repository;

import esgi.easisell.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    // Ventes par client avec pagination
    Page<Sale> findByClientUserIdOrderBySaleTimestampDesc(UUID clientId, Pageable pageable);

    // Ventes par client sans pagination
    List<Sale> findByClientUserIdOrderBySaleTimestampDesc(UUID clientId);

    // Ventes d'un client sur une période
    @Query("SELECT s FROM Sale s WHERE s.client.userId = :clientId " +
            "AND s.saleTimestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY s.saleTimestamp DESC")
    List<Sale> findSalesByClientAndPeriod(@Param("clientId") UUID clientId,
                                          @Param("startDate") Timestamp startDate,
                                          @Param("endDate") Timestamp endDate);

    // Ventes du jour pour un client
    @Query("SELECT s FROM Sale s WHERE s.client.userId = :clientId " +
            "AND DATE(s.saleTimestamp) = CURRENT_DATE " +
            "ORDER BY s.saleTimestamp DESC")
    List<Sale> findTodaySalesByClient(@Param("clientId") UUID clientId);

    // Total des ventes sur une période
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND s.saleTimestamp BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesAmount(@Param("clientId") UUID clientId,
                                   @Param("startDate") Timestamp startDate,
                                   @Param("endDate") Timestamp endDate);

    // Total des ventes du jour
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND DATE(s.saleTimestamp) = CURRENT_DATE")
    BigDecimal getTodayTotalSales(@Param("clientId") UUID clientId);

    // Nombre de ventes sur une période
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.client.userId = :clientId " +
            "AND s.saleTimestamp BETWEEN :startDate AND :endDate")
    long countSalesByPeriod(@Param("clientId") UUID clientId,
                            @Param("startDate") Timestamp startDate,
                            @Param("endDate") Timestamp endDate);

    // Ventes en attente de paiement
    @Query("SELECT s FROM Sale s WHERE s.client.userId = :clientId " +
            "AND s.isDeferred = true " +
            "AND SIZE(s.payments) = 0 " +
            "ORDER BY s.saleTimestamp DESC")
    List<Sale> findPendingPaymentSales(@Param("clientId") UUID clientId);

    // Vérifier si une vente appartient à un client
    @Query("SELECT COUNT(s) > 0 FROM Sale s WHERE s.saleId = :saleId " +
            "AND s.client.userId = :clientId")
    boolean existsByIdAndClientId(@Param("saleId") UUID saleId,
                                  @Param("clientId") UUID clientId);

    // Produits les plus vendus
    @Query("SELECT si.product.productId, si.product.name, SUM(si.quantitySold) as totalQuantity " +
            "FROM SaleItem si JOIN si.sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND s.saleTimestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY si.product.productId, si.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(@Param("clientId") UUID clientId,
                                          @Param("startDate") Timestamp startDate,
                                          @Param("endDate") Timestamp endDate,
                                          Pageable pageable);

    // ✅ VENTES EN ATTENTE (non finalisées)
    @Query("SELECT s FROM Sale s WHERE s.client.userId = :clientId " +
            "AND SIZE(s.payments) = 0 " +
            "ORDER BY s.saleTimestamp DESC")
    List<Sale> findPendingSalesByClient(@Param("clientId") UUID clientId);

    // ✅ PRODUITS LES PLUS VENDUS AUJOURD'HUI - VERSION CORRIGÉE
    @Query("SELECT si.product.productId, si.product.name, SUM(si.quantitySold) as totalQuantity " +
            "FROM SaleItem si JOIN si.sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND DATE(s.saleTimestamp) = CURRENT_DATE " +
            "AND SIZE(s.payments) > 0 " +
            "GROUP BY si.product.productId, si.product.name " +
            "ORDER BY SUM(si.quantitySold) DESC")
    List<Object[]> findTodayTopSellingProducts(@Param("clientId") UUID clientId);

    // ✅ STATISTIQUES PAR HEURE - VERSION NATIVE SQL
    @Query(value = "SELECT HOUR(s.sale_timestamp) as hour, COUNT(s.sale_id) as salesCount, " +
            "COALESCE(SUM(s.total_amount), 0) as totalAmount " +
            "FROM sale s " +
            "WHERE s.client_id = UNHEX(REPLACE(:clientId, '-', '')) " +
            "AND DATE(s.sale_timestamp) = CURDATE() " +
            "AND (SELECT COUNT(*) FROM payment p WHERE p.sale_id = s.sale_id) > 0 " +
            "GROUP BY HOUR(s.sale_timestamp) " +
            "ORDER BY hour",
            nativeQuery = true)
    List<Object[]> findTodayHourlySalesStats(@Param("clientId") String clientId);
}