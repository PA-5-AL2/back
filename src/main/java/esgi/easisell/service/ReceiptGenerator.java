package esgi.easisell.service;

import esgi.easisell.dto.*;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.Payment;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class ReceiptGenerator {

    public static ReceiptDTO generate(Sale sale) {
        ReceiptDTO receipt = new ReceiptDTO();
        receipt.setSaleId(sale.getSaleId());
        receipt.setReceiptNumber("REC-" + sale.getSaleId().toString().substring(0, 8).toUpperCase());
        receipt.setDateTime(sale.getSaleTimestamp().toLocalDateTime());

        // Client info
        ClientInfoDTO clientInfo = new ClientInfoDTO();
        clientInfo.setClientId(sale.getClient().getUserId());
        clientInfo.setName(sale.getClient().getName());
        clientInfo.setUsername(sale.getClient().getUsername());
        receipt.setClient(clientInfo);

        // Items
        receipt.setItems(sale.getSaleItems().stream()
                .map(item -> {
                    ReceiptItemDTO receiptItem = new ReceiptItemDTO();
                    receiptItem.setProductName(item.getProduct().getName());
                    receiptItem.setBarcode(item.getProduct().getBarcode());
                    receiptItem.setQuantity(item.getQuantitySold().intValue());
                    receiptItem.setUnitPrice(item.getProduct().getUnitPrice());
                    receiptItem.setTotal(item.getPriceAtSale());
                    // ✅ NOUVEAU : Ajout des informations formatées pour le ticket
                    if (item.getProduct().getIsSoldByWeight() != null && item.getProduct().getIsSoldByWeight()) {
                        // Pour les produits au poids : "2.350 kg"
                        receiptItem.setFormattedQuantity(String.format("%.3f %s",
                                item.getQuantitySold(), item.getProduct().getUnitLabel()));
                    } else {
                        // Pour les pièces : "3 pièces"
                        receiptItem.setFormattedQuantity(String.format("%.0f %s",
                                item.getQuantitySold(), item.getProduct().getUnitLabel()));
                    }
                    return receiptItem;
                })
                .collect(Collectors.toList()));

        // Totaux
        receipt.setSubtotal(sale.getTotalAmount());
        receipt.setTaxAmount(BigDecimal.ZERO); // TODO: Implémenter les taxes
        receipt.setTotalAmount(sale.getTotalAmount());

        // Paiements
        receipt.setPayments(sale.getPayments().stream()
                .map(payment -> {
                    PaymentInfoDTO paymentInfo = new PaymentInfoDTO();
                    paymentInfo.setPaymentId(payment.getPaymentId());
                    paymentInfo.setType(payment.getType());
                    paymentInfo.setAmount(payment.getAmount());
                    paymentInfo.setCurrency(payment.getCurrency());
                    paymentInfo.setPaymentDate(payment.getPaymentDate());
                    return paymentInfo;
                })
                .collect(Collectors.toList()));

        // Calculer la monnaie rendue
        BigDecimal totalPaid = sale.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        receipt.setChangeAmount(totalPaid.subtract(sale.getTotalAmount()));

        receipt.setCurrency(sale.getClient().getCurrencyPreference());
        receipt.setCashierName("Caissier"); // TODO: Récupérer le nom réel

        return receipt;
    }
}