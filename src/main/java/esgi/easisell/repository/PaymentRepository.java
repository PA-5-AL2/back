package esgi.easisell.repository;

import esgi.easisell.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Paiements par type sur une période
    @Query("SELECT p.type, SUM(p.amount) FROM Payment p " +
            "JOIN p.sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.type")
    List<Object[]> getPaymentsByTypeAndPeriod(@Param("clientId") UUID clientId,
                                              @Param("startDate") Timestamp startDate,
                                              @Param("endDate") Timestamp endDate);

    // Total des paiements par type aujourd'hui
    @Query("SELECT p.type, COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "JOIN p.sale s " +
            "WHERE s.client.userId = :clientId " +
            "AND DATE(p.paymentDate) = CURRENT_DATE " +
            "GROUP BY p.type")
    List<Object[]> getTodayPaymentsByType(@Param("clientId") UUID clientId);

    // Paiements d'une vente
    List<Payment> findBySaleSaleId(UUID saleId);
}