package esgi.easisell.dto;

import esgi.easisell.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRecognitionResponseDTO {

    private boolean recognized; // Client reconnu ou non
    private UUID customerId;
    private String fullName;
    private String customerType;
    private Integer trustLevel;
    private String starRating;
    private String reliabilityDescription;
    private String recommendation;

    // Statistiques du client
    private Integer totalPurchases;
    private BigDecimal totalAmountSpent;
    private BigDecimal maxDeferredAmount;
    private String lastVisitDate;

    // Pour les nouveaux clients
    private String suggestedMaxAmount;
    private boolean canHaveDeferredPayment;

    // Constructeur pour client reconnu
    public CustomerRecognitionResponseDTO(Customer customer, BigDecimal requestedAmount) {
        this.recognized = true;
        this.customerId = customer.getCustomerId();
        this.fullName = customer.getFullName();
        this.customerType = customer.getCustomerType().name();
        this.trustLevel = customer.getTrustLevel();
        this.starRating = customer.getStarRating();
        this.reliabilityDescription = customer.getReliabilityDescription();
        this.totalPurchases = customer.getTotalPurchasesCount();
        this.totalAmountSpent = customer.getTotalAmountSpent();
        this.maxDeferredAmount = customer.getMaxDeferredAmount();
        this.lastVisitDate = customer.getLastVisitDate() != null ?
                customer.getLastVisitDate().toString() : null;
        this.canHaveDeferredPayment = customer.canHaveDeferredPayment(requestedAmount);
        this.recommendation = getRecommendationText(customer, requestedAmount);
    }

    // Constructeur pour client non reconnu
    public static CustomerRecognitionResponseDTO forUnknownCustomer(String fullName, String phone) {
        return CustomerRecognitionResponseDTO.builder()
                .recognized(false)
                .fullName(fullName)
                .customerType("NOUVEAU")
                .trustLevel(1)
                .starRating("⭐")
                .reliabilityDescription("Nouveau client - Prudence recommandée")
                .totalPurchases(0)
                .totalAmountSpent(BigDecimal.ZERO)
                .maxDeferredAmount(BigDecimal.valueOf(50))
                .suggestedMaxAmount("50€")
                .canHaveDeferredPayment(true)
                .recommendation("⚠️ Nouveau client - Limitez à 50€ et demandez une pièce d'identité")
                .build();
    }

    private String getRecommendationText(Customer customer, BigDecimal requestedAmount) {
        if (!customer.canHaveDeferredPayment(requestedAmount)) {
            return "❌ MONTANT TROP ÉLEVÉ - Limite autorisée: " + customer.getMaxDeferredAmount() + "€";
        }

        switch (customer.getCustomerType()) {
            case VIP:
                return "⭐ CLIENT VIP - Accord total recommandé";
            case FIDELE:
                return "✅ Client fidèle - Paiement différé recommandé";
            case REGULIER:
                return "👍 Client régulier - Paiement différé acceptable";
            case OCCASIONNEL:
                return "📝 Client occasionnel - OK pour ce montant";
            case NOUVEAU:
                return "⚠️ Nouveau client - Prudence recommandée";
            case BLACKLIST:
                return "🚨 CLIENT BLACKLISTÉ - REFUSER";
            default:
                return "❓ Évaluation nécessaire";
        }
    }
}
