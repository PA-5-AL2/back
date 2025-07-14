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

    private UUID id;
    private UUID saleId;
    private String customerName;
    private String customerPhone;
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

    // Constructeur à partir de l'entité
    public DeferredPaymentResponseDTO(DeferredPayment payment) {
        this.id = payment.getDeferredPaymentId();
        this.saleId = payment.getSale().getSaleId();
        this.customerName = payment.getCustomerName();
        this.customerPhone = payment.getCustomerPhone();
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
    }
}