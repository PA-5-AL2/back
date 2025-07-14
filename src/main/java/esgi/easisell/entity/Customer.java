package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "customer_id")
    private UUID customerId;

    /**
     * Relation avec le client (boutique)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Informations de base
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Informations de fidélité
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", length = 20)
    @Builder.Default
    private CustomerType customerType = CustomerType.NOUVEAU;

    @Column(name = "total_purchases_count")
    @Builder.Default
    private Integer totalPurchasesCount = 0;

    @Column(name = "total_amount_spent", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmountSpent = BigDecimal.ZERO;

    @Column(name = "trust_level")
    @Builder.Default
    private Integer trustLevel = 1;

    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;

    /**
     * Dates importantes
     */
    @CreationTimestamp
    @Column(name = "first_visit_date")
    private Timestamp firstVisitDate;

    @Column(name = "last_visit_date")
    private Timestamp lastVisitDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    /**
     * Statut du client
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Préférences de paiement
     */
    @Column(name = "preferred_payment_method", length = 20)
    private String preferredPaymentMethod;

    @Column(name = "allows_deferred_payment")
    @Builder.Default
    private Boolean allowsDeferredPayment = true;

    @Column(name = "max_deferred_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal maxDeferredAmount = BigDecimal.valueOf(100);

    /**
     * Enums
     */
    public enum CustomerType {
        NOUVEAU,        // Premier achat
        OCCASIONNEL,    // 2-5 achats
        REGULIER,       // 6-15 achats
        FIDELE,         // 16-50 achats
        VIP,            // 50+ achats
        INACTIF,        // Pas d'achat depuis 6 mois
        BLACKLIST       // Client problématique
    }

    public enum CustomerStatus {
        ACTIVE,         // Client actif
        INACTIVE,       // Client inactif
        SUSPENDED,      // Suspendu (pb paiements)
        BLACKLISTED     // Blacklisté
    }

    /**
     * Méthodes utilitaires
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canHaveDeferredPayment(BigDecimal amount) {
        return allowsDeferredPayment &&
                status == CustomerStatus.ACTIVE &&
                customerType != CustomerType.BLACKLIST &&
                amount.compareTo(maxDeferredAmount) <= 0;
    }

    public void updateAfterPurchase(BigDecimal amount) {
        this.totalPurchasesCount++;
        this.totalAmountSpent = this.totalAmountSpent.add(amount);
        this.lastVisitDate = Timestamp.valueOf(LocalDateTime.now());

        // Recalculer le type automatiquement
        this.customerType = calculateCustomerType();
        this.trustLevel = calculateTrustLevel();

        // Points de fidélité (1 point par euro)
        this.loyaltyPoints += amount.intValue();
    }

    public CustomerType calculateCustomerType() {
        if (totalPurchasesCount <= 1) return CustomerType.NOUVEAU;
        if (totalPurchasesCount <= 5) return CustomerType.OCCASIONNEL;
        if (totalPurchasesCount <= 15) return CustomerType.REGULIER;
        if (totalPurchasesCount <= 50) return CustomerType.FIDELE;
        return CustomerType.VIP;
    }

    private Integer calculateTrustLevel() {
        if (totalPurchasesCount == 0) return 1;
        if (totalPurchasesCount <= 2) return 2;
        if (totalPurchasesCount <= 10) return 3;
        if (totalPurchasesCount <= 25) return 4;
        return 5;
    }

    public String getReliabilityDescription() {
        switch (customerType) {
            case NOUVEAU:
                return "Nouveau client - Prudence recommandée";
            case OCCASIONNEL:
                return "Client occasionnel - Fiabilité moyenne";
            case REGULIER:
                return "Client régulier - Fiable";
            case FIDELE:
                return "Client fidèle - Très fiable";
            case VIP:
                return "Client VIP - Extrêmement fiable";
            case INACTIF:
                return "Client inactif - Attention";
            case BLACKLIST:
                return "Client blacklisté - REFUSER";
            default:
                return "Statut inconnu";
        }
    }

    public String getStarRating() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < trustLevel; i++) {
            stars.append("⭐");
        }
        return stars.toString();
    }
}