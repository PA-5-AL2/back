package esgi.easisell.service.payment;

public class PaymentProcessorFactory {

    public static PaymentProcessor create(String paymentType) {
        switch (paymentType.toUpperCase()) {
            case "CASH":
                return new CashPaymentProcessor();
            case "CARD":
                return new CardPaymentProcessor();
            default:
                throw new IllegalArgumentException("Type de paiement non supporté: " + paymentType);
        }
    }
}