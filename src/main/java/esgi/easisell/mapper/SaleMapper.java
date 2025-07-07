package esgi.easisell.mapper;

import esgi.easisell.dto.SaleResponseDTO;
import esgi.easisell.entity.Sale;

public class SaleMapper {

    public static SaleResponseDTO toResponseDTO(Sale sale) {
        if (sale == null) {
            return null;
        }

        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setSaleId(sale.getSaleId());
        dto.setSaleTimestamp(sale.getSaleTimestamp());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setIsDeferred(sale.getIsDeferred());

        if (sale.getClient() != null) {
            dto.setClientId(sale.getClient().getUserId());
            dto.setClientName(sale.getClient().getName());
            dto.setClientUsername(sale.getClient().getUsername());
        }

        // ✅ Protection contre les listes null
        dto.setItemCount(sale.getSaleItems() != null ? sale.getSaleItems().size() : 0);
        dto.setIsPaid(sale.getPayments() != null && !sale.getPayments().isEmpty());

        return dto;
    }
}