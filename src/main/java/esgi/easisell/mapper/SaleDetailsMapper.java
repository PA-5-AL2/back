package esgi.easisell.mapper;

import esgi.easisell.dto.*;
import esgi.easisell.entity.Sale;

import java.util.stream.Collectors;

public class SaleDetailsMapper {

    public static SaleDetailsDTO toDTO(Sale sale) {
        SaleDetailsDTO dto = new SaleDetailsDTO();
        dto.setSaleId(sale.getSaleId());
        dto.setSaleTimestamp(sale.getSaleTimestamp());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setIsDeferred(sale.getIsDeferred());

        // Client info
        ClientInfoDTO clientInfo = new ClientInfoDTO();
        clientInfo.setClientId(sale.getClient().getUserId());
        clientInfo.setUsername(sale.getClient().getUsername());
        clientInfo.setFirstName(sale.getClient().getFirstName());
        clientInfo.setName(sale.getClient().getName());
        clientInfo.setAddress(sale.getClient().getAddress());
        clientInfo.setCurrencyPreference(sale.getClient().getCurrencyPreference());
        dto.setClient(clientInfo);

        // Items
        dto.setItems(sale.getSaleItems().stream()
                .map(SaleItemMapper::toResponseDTO)
                .collect(Collectors.toList()));

        // Payments
        dto.setPayments(sale.getPayments().stream()
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

        dto.setIsPaid(!sale.getPayments().isEmpty());

        return dto;
    }
}
