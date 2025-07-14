package esgi.easisell.dto;

import esgi.easisell.entity.DeferredPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferredPaymentResponseDTO {

    // Informations de base du paiement
    private UUID id;
    private UUID saleId;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    private String status;
    private String notes;
    private String currency;
    private boolean isOverdue;
    private double paymentProgress;
    private int reminderCount;
    private String createdAt;
    private String lastReminderSent;

    // Informations client enrichies
    private String customerName;
    private String customerPhone;
    private boolean hasCustomerProfile; // Client reconnu ou non
    private UUID customerId; // ID du profil customer (si existe)

    // Informations de fidélité (si client reconnu)
    private String customerType; // VIP, FIDELE, NOUVEAU, etc.
    private Integer trustLevel; // 1-5 étoiles
    private String starRating; // "⭐⭐⭐⭐⭐"
    private String reliabilityDescription; // Description textuelle
    private String recommendation; // Recommandation pour le commerçant
    private boolean isAuthorizedAmount; // Le montant est-il autorisé ?

    // Statistiques du client (si profil existe)
    private Integer customerTotalPurchases;
    private BigDecimal customerTotalSpent;
    private BigDecimal customerMaxDeferredAmount;

    // Constructeur à partir de l'entité
    public DeferredPaymentResponseDTO(DeferredPayment payment) {
        // Informations de base
        this.id = payment.getDeferredPaymentId();
        this.saleId = payment.getSale().getSaleId();
        this.amount = payment.getAmount();
        this.amountPaid = payment.getAmountPaid();
        this.remainingAmount = payment.getRemainingAmount();
        this.dueDate = payment.getDueDate();
        this.status = payment.getStatus().name();
        this.notes = payment.getNotes();
        this.currency = payment.getCurrency();
        this.isOverdue = payment.isOverdue();
        this.paymentProgress = payment.getPaymentProgress();
        this.reminderCount = payment.getReminderCount();
        this.createdAt = payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null;
        this.lastReminderSent = payment.getLastReminderSent() != null ?
                payment.getLastReminderSent().toString() : null;

        // Informations client
        this.customerName = payment.getEffectiveCustomerName();
        this.customerPhone = payment.getEffectiveCustomerPhone();
        this.hasCustomerProfile = payment.hasCustomerProfile();

        if (payment.getCustomer() != null) {
            // Client avec profil complet
            this.customerId = payment.getCustomer().getCustomerId();
            this.customerType = payment.getCustomerTypeLabel();
            this.trustLevel = payment.getCustomerTrustLevel();
            this.starRating = payment.getCustomerStarRating();
            this.reliabilityDescription = payment.getCustomerReliabilityDescription();
            this.recommendation = payment.getRecommendationForMerchant();
            this.isAuthorizedAmount = payment.isAmountAuthorizedForCustomer();

            // Statistiques du client
            this.customerTotalPurchases = payment.getCustomer().getTotalPurchasesCount();
            this.customerTotalSpent = payment.getCustomer().getTotalAmountSpent();
            this.customerMaxDeferredAmount = payment.getCustomer().getMaxDeferredAmount();
        } else {
            // Client sans profil (fallback)
            this.customerType = "INCONNU";
            this.trustLevel = 1;
            this.starRating = "⭐";
            this.reliabilityDescription = "Client non reconnu - Prudence recommandée";
            this.recommendation = "⚠️ Client non reconnu - Limitez le montant et demandez une pièce d'identité";
            this.isAuthorizedAmount = payment.getAmount().compareTo(BigDecimal.valueOf(50)) <= 0;
            this.customerTotalPurchases = 0;
            this.customerTotalSpent = BigDecimal.ZERO;
            this.customerMaxDeferredAmount = BigDecimal.valueOf(50);
        }
    }
}