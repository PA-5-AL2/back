package esgi.easisell.service.payment;

import esgi.easisell.model.PaymentResult;
import java.math.BigDecimal;
import java.util.UUID;

public class CashPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(BigDecimal totalAmount, BigDecimal amountReceived, String currency) {
        if (amountReceived.compareTo(totalAmount) < 0) {
            return PaymentResult.builder()
                    .successful(false)
                    .errorMessage("Montant insuffisant")
                    .build();
        }

        BigDecimal change = amountReceived.subtract(totalAmount);

        return PaymentResult.builder()
                .successful(true)
                .paymentId(UUID.randomUUID())
                .amountPaid(totalAmount)
                .changeAmount(change)
                .currency(currency)
                .message("Paiement en espèces réussi")
                .build();
    }
}