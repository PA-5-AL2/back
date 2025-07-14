package esgi.easisell.repository;

import esgi.easisell.entity.DeferredPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeferredPaymentRepository extends JpaRepository<DeferredPayment, UUID> {

    /**
     * Trouver tous les paiements différés d'un client
     */
    List<DeferredPayment> findByClientUserIdOrderByCreatedAtDesc(UUID clientId);

    /**
     * Trouver les paiements différés par statut
     */
    List<DeferredPayment> findByStatusAndClientUserIdOrderByDueDateAsc(
            DeferredPayment.PaymentStatus status, UUID clientId);

    /**
     * Trouver les paiements en retard
     */
    @Query("SELECT dp FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND dp.dueDate < :currentDate " +
            "AND dp.status IN ('PENDING', 'PARTIAL') " +
            "ORDER BY dp.dueDate ASC")
    List<DeferredPayment> findOverduePayments(@Param("clientId") UUID clientId,
                                              @Param("currentDate") LocalDate currentDate);

    /**
     * Compter les paiements en retard
     */
    @Query("SELECT COUNT(dp) FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND dp.dueDate < :currentDate " +
            "AND dp.status IN ('PENDING', 'PARTIAL')")
    Long countOverduePayments(@Param("clientId") UUID clientId,
                              @Param("currentDate") LocalDate currentDate);

    /**
     * Calculer le montant total en attente pour un client
     */
    @Query("SELECT COALESCE(SUM(dp.amount - dp.amountPaid), 0) FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND dp.status IN ('PENDING', 'PARTIAL')")
    BigDecimal getTotalPendingAmount(@Param("clientId") UUID clientId);

    /**
     * Trouver les paiements qui arrivent à échéance bientôt
     */
    @Query("SELECT dp FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND dp.dueDate BETWEEN :startDate AND :endDate " +
            "AND dp.status IN ('PENDING', 'PARTIAL') " +
            "ORDER BY dp.dueDate ASC")
    List<DeferredPayment> findPaymentsDueSoon(@Param("clientId") UUID clientId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Rechercher par nom de client
     */
    @Query("SELECT dp FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND LOWER(dp.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY dp.createdAt DESC")
    List<DeferredPayment> searchByCustomerName(@Param("clientId") UUID clientId,
                                               @Param("searchTerm") String searchTerm);

    /**
     * Statistiques des paiements différés par statut
     */
    @Query("SELECT dp.status, COUNT(dp), COALESCE(SUM(dp.amount - dp.amountPaid), 0) " +
            "FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "GROUP BY dp.status")
    List<Object[]> getPaymentStatsByStatus(@Param("clientId") UUID clientId);

    /**
     * Trouver les paiements différés d'une vente spécifique
     */
    List<DeferredPayment> findBySaleSaleId(UUID saleId);

    /**
     * Paiements nécessitant un rappel (pas de rappel depuis X jours)
     */
    @Query("SELECT dp FROM DeferredPayment dp " +
            "WHERE dp.client.userId = :clientId " +
            "AND dp.status IN ('PENDING', 'PARTIAL') " +
            "AND (dp.lastReminderSent IS NULL OR dp.lastReminderSent < :reminderThreshold) " +
            "AND dp.dueDate <= :currentDate " +
            "ORDER BY dp.dueDate ASC")
    List<DeferredPayment> findPaymentsNeedingReminder(@Param("clientId") UUID clientId,
                                                      @Param("currentDate") LocalDate currentDate,
                                                      @Param("reminderThreshold") java.sql.Timestamp reminderThreshold);
}