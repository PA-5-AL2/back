package esgi.easisell.service.payment;

import esgi.easisell.model.PaymentResult;
import java.math.BigDecimal;
import java.util.UUID;

public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(BigDecimal totalAmount, BigDecimal amountReceived, String currency) {
        // TODO: Intégrer avec une vraie passerelle de paiement
        // Pour l'instant, on simule un paiement réussi

        return PaymentResult.builder()
                .successful(true)
                .paymentId(UUID.randomUUID())
                .amountPaid(totalAmount)
                .changeAmount(BigDecimal.ZERO)
                .currency(currency)
                .message("Paiement par carte réussi")
                .build();
    }
}