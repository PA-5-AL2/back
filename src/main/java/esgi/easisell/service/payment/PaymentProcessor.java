package esgi.easisell.service.payment;

import esgi.easisell.model.PaymentResult;
import java.math.BigDecimal;

public interface PaymentProcessor {
    PaymentResult processPayment(BigDecimal totalAmount, BigDecimal amountReceived, String currency);
}