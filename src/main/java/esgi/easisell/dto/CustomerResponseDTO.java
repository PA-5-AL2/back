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
public class CustomerResponseDTO {

    private UUID customerId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String email;
    private String address;

    // Statistiques de fidélité
    private String customerType;
    private Integer trustLevel;
    private String starRating;
    private Integer totalPurchasesCount;
    private BigDecimal totalAmountSpent;
    private Integer loyaltyPoints;

    // Dates
    private String firstVisitDate;
    private String lastVisitDate;

    // Paramètres
    private String status;
    private BigDecimal maxDeferredAmount;
    private Boolean allowsDeferredPayment;
    private String preferredPaymentMethod;
    private String notes;

    // Informations calculées
    private String reliabilityDescription;
    private boolean canHaveDeferredPayment;

    // Constructeur à partir de l'entité
    public CustomerResponseDTO(Customer customer) {
        this.customerId = customer.getCustomerId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.fullName = customer.getFullName();
        this.phone = customer.getPhone();
        this.email = customer.getEmail();
        this.address = customer.getAddress();
        this.customerType = customer.getCustomerType().name();
        this.trustLevel = customer.getTrustLevel();
        this.starRating = customer.getStarRating();
        this.totalPurchasesCount = customer.getTotalPurchasesCount();
        this.totalAmountSpent = customer.getTotalAmountSpent();
        this.loyaltyPoints = customer.getLoyaltyPoints();
        this.firstVisitDate = customer.getFirstVisitDate() != null ?
                customer.getFirstVisitDate().toString() : null;
        this.lastVisitDate = customer.getLastVisitDate() != null ?
                customer.getLastVisitDate().toString() : null;
        this.status = customer.getStatus().name();
        this.maxDeferredAmount = customer.getMaxDeferredAmount();
        this.allowsDeferredPayment = customer.getAllowsDeferredPayment();
        this.preferredPaymentMethod = customer.getPreferredPaymentMethod();
        this.notes = customer.getNotes();
        this.reliabilityDescription = customer.getReliabilityDescription();
        this.canHaveDeferredPayment = customer.getAllowsDeferredPayment() &&
                customer.getStatus() == Customer.CustomerStatus.ACTIVE;
    }
}