package esgi.easisell.entity;

import esgi.easisell.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deferred_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferredPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "deferred_payment_id")
    private UUID deferredPaymentId;

    /**
     * Relation avec le customer (acheteur) - NOUVEAU
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    /**
     * Relation avec la vente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    /**
     * Relation avec le client
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Nom du client (pour compatibilité avec anciens paiements)
     */
    @Column(name = "customer_name", length = 255)
    private String customerName;

    /**
     * Téléphone du client (pour compatibilité)
     */
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    /**
     * Montant total à payer
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Montant déjà payé (pour les paiements partiels)
     */
    @Column(name = "amount_paid", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /**
     * Date limite de paiement
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Date de création du paiement différé
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    /**
     * Date de dernière mise à jour
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    /**
     * Date du dernier rappel envoyé
     */
    @Column(name = "last_reminder_sent")
    private Timestamp lastReminderSent;

    /**
     * Nombre de rappels envoyés
     */
    @Column(name = "reminder_count")
    @Builder.Default
    private Integer reminderCount = 0;

    /**
     * Statut du paiement différé
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Notes ou commentaires sur le paiement
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Devise du paiement
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "EUR";

    /**
     * Statuts possibles pour un paiement différé
     */
    public enum PaymentStatus {
        PENDING,     // En attente
        PARTIAL,     // Partiellement payé
        PAID,        // Complètement payé
        OVERDUE,     // En retard
        CANCELLED    // Annulé
    }

    /**
     * Méthodes utilitaires
     */
    public BigDecimal getRemainingAmount() {
        return amount.subtract(amountPaid);
    }

    public boolean isOverdue() {
        return dueDate.isBefore(LocalDate.now()) && status == PaymentStatus.PENDING;
    }

    public boolean isFullyPaid() {
        return amountPaid.compareTo(amount) >= 0;
    }

    public double getPaymentProgress() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return amountPaid.divide(amount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * Obtenir le nom effectif du client (Customer en priorité, sinon nom saisi)
     */
    public String getEffectiveCustomerName() {
        return customer != null ? customer.getFullName() : customerName;
    }

    /**
     * Obtenir le téléphone effectif
     */
    public String getEffectiveCustomerPhone() {
        return customer != null ? customer.getPhone() : customerPhone;
    }

    /**
     * Vérifier si c'est un client reconnu (avec profil Customer)
     */
    public boolean hasCustomerProfile() {
        return customer != null;
    }

    /**
     * Obtenir le type de client (si profil existe)
     */
    public String getCustomerTypeLabel() {
        if (customer == null) return "INCONNU";
        return customer.getCustomerType().name();
    }

    /**
     * Obtenir le niveau de confiance (si profil existe)
     */
    public Integer getCustomerTrustLevel() {
        return customer != null ? customer.getTrustLevel() : 1;
    }

    /**
     * Obtenir la description de fiabilité
     */
    public String getCustomerReliabilityDescription() {
        if (customer == null) return "Client non reconnu - Prudence recommandée";
        return customer.getReliabilityDescription();
    }

    /**
     * Obtenir l'évaluation en étoiles
     */
    public String getCustomerStarRating() {
        if (customer == null) return "⭐";
        return customer.getStarRating();
    }

    /**
     * Vérifier si le client peut avoir ce montant en paiement différé
     */
    public boolean isAmountAuthorizedForCustomer() {
        if (customer == null) return amount.compareTo(BigDecimal.valueOf(50)) <= 0; // Limite par défaut
        return customer.canHaveDeferredPayment(amount);
    }

    /**
     * Obtenir une recommandation pour le commerçant
     */
    public String getRecommendationForMerchant() {
        if (customer == null) {
            return "⚠️ Client non reconnu - Demandez une pièce d'identité et limitez le montant à 50€";
        }

        switch (customer.getCustomerType()) {
            case VIP:
                return "⭐ Client VIP - Accord total recommandé (limite: " + customer.getMaxDeferredAmount() + "€)";
            case FIDELE:
                return "✅ Client fidèle - Paiement différé recommandé";
            case REGULIER:
                return "👍 Client régulier - Paiement différé acceptable";
            case OCCASIONNEL:
                return "📝 Client occasionnel - Paiement différé OK pour petits montants";
            case NOUVEAU:
                return "⚠️ Nouveau client - Prudence recommandée";
            case BLACKLIST:
                return "🚨 CLIENT BLACKLISTÉ - REFUSER LE PAIEMENT DIFFÉRÉ";
            case INACTIF:
                return "⏰ Client inactif - Vérifier sa situation actuelle";
            default:
                return "❓ Évaluation nécessaire";
        }
    }
}