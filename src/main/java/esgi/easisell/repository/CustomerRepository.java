package esgi.easisell.repository;

import esgi.easisell.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Trouver tous les customers d'un client (boutique)
     */
    List<Customer> findByClientUserIdOrderByLastVisitDateDesc(UUID clientId);

    /**
     * Rechercher un customer par nom et téléphone (pour reconnaissance)
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND (LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR c.phone = :phone) " +
            "ORDER BY c.lastVisitDate DESC")
    List<Customer> findByNameOrPhone(@Param("clientId") UUID clientId,
                                     @Param("name") String name,
                                     @Param("phone") String phone);

    /**
     * Rechercher par nom complet
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%')) " +
            "ORDER BY c.totalPurchasesCount DESC")
    List<Customer> findByFullName(@Param("clientId") UUID clientId,
                                  @Param("fullName") String fullName);

    /**
     * Rechercher par téléphone exact
     */
    Optional<Customer> findByClientUserIdAndPhone(UUID clientId, String phone);

    /**
     * Customers VIP d'un client
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND c.customerType IN ('VIP', 'FIDELE') " +
            "ORDER BY c.totalAmountSpent DESC")
    List<Customer> findVipCustomers(@Param("clientId") UUID clientId);

    /**
     * Customers à risque
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND (c.customerType = 'BLACKLIST' OR c.status = 'SUSPENDED') " +
            "ORDER BY c.updatedAt DESC")
    List<Customer> findRiskyCustomers(@Param("clientId") UUID clientId);

    /**
     * Top customers par montant dépensé
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND c.status = 'ACTIVE' " +
            "ORDER BY c.totalAmountSpent DESC")
    List<Customer> findTopCustomersBySpending(@Param("clientId") UUID clientId);

    /**
     * Customers par type
     */
    List<Customer> findByClientUserIdAndCustomerTypeOrderByTotalAmountSpentDesc(
            UUID clientId, Customer.CustomerType customerType);

    /**
     * Recherche textuelle avancée
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR c.phone LIKE CONCAT('%', :searchTerm, '%') " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY c.lastVisitDate DESC")
    List<Customer> searchCustomers(@Param("clientId") UUID clientId,
                                   @Param("searchTerm") String searchTerm);

    /**
     * Statistiques par type de customer
     */
    @Query("SELECT c.customerType, COUNT(c), COALESCE(SUM(c.totalAmountSpent), 0) " +
            "FROM Customer c WHERE c.client.userId = :clientId " +
            "GROUP BY c.customerType")
    List<Object[]> getCustomerStatsByType(@Param("clientId") UUID clientId);

    /**
     * Customers inactifs (pas d'achat depuis X jours)
     */
    @Query("SELECT c FROM Customer c WHERE c.client.userId = :clientId " +
            "AND c.lastVisitDate < :thresholdDate " +
            "AND c.status = 'ACTIVE' " +
            "ORDER BY c.lastVisitDate ASC")
    List<Customer> findInactiveCustomers(@Param("clientId") UUID clientId,
                                         @Param("thresholdDate") java.sql.Timestamp thresholdDate);

    /**
     * Top customers avec le plus de paiements différés
     */
    @Query("SELECT c FROM Customer c " +
            "WHERE c.client.userId = :clientId " +
            "AND EXISTS (SELECT 1 FROM DeferredPayment dp WHERE dp.customer = c) " +
            "ORDER BY c.totalAmountSpent DESC")
    List<Customer> findTopCustomersByDeferredPayments(@Param("clientId") UUID clientId);
}