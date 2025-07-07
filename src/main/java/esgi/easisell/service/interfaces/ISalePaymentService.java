package esgi.easisell.service.interfaces;

import esgi.easisell.dto.PaymentResultDTO;
import java.math.BigDecimal;
import java.util.UUID;

public interface ISalePaymentService {
    PaymentResultDTO processPayment(UUID saleId, String paymentType, BigDecimal amountReceived, String currency);
}