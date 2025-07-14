package esgi.easisell.entity;

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
     * Nom du client ou référence pour le paiement différé
     */
    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    /**
     * Téléphone du client (optionnel)
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
}