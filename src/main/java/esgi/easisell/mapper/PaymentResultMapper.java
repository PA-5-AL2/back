package esgi.easisell.mapper;

import esgi.easisell.dto.PaymentResultDTO;
import esgi.easisell.entity.Sale;
import esgi.easisell.model.PaymentResult;

public class PaymentResultMapper {

    public static PaymentResultDTO toDTO(PaymentResult result, Sale sale) {
        PaymentResultDTO dto = new PaymentResultDTO();
        dto.setSuccessful(result.isSuccessful());
        dto.setPaymentId(result.getPaymentId());
        dto.setSaleId(sale.getSaleId());
        dto.setAmountPaid(result.getAmountPaid());
        dto.setChangeAmount(result.getChangeAmount());
        dto.setCurrency(result.getCurrency());
        dto.setMessage(result.getMessage());
        dto.setErrorMessage(result.getErrorMessage());
        return dto;
    }
}