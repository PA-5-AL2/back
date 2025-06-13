package esgi.easisell.mapper;

import esgi.easisell.dto.SaleItemResponseDTO;
import esgi.easisell.entity.SaleItem;

public class SaleItemMapper {

    public static SaleItemResponseDTO toResponseDTO(SaleItem saleItem) {
        SaleItemResponseDTO dto = new SaleItemResponseDTO();
        dto.setSaleItemId(saleItem.getSaleItemId());
        dto.setSaleId(saleItem.getSale().getSaleId());
        dto.setProductId(saleItem.getProduct().getProductId());
        dto.setProductName(saleItem.getProduct().getName());
        dto.setProductBarcode(saleItem.getProduct().getBarcode());
        dto.setQuantitySold(saleItem.getQuantitySold());
        dto.setUnitPrice(saleItem.getProduct().getUnitPrice());
        dto.setPriceAtSale(saleItem.getPriceAtSale());
        dto.setTotalPrice(saleItem.getPriceAtSale());
        return dto;
    }
}