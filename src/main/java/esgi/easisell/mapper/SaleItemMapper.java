package esgi.easisell.mapper;

import esgi.easisell.dto.SaleItemResponseDTO;
import esgi.easisell.entity.SaleItem;

public class SaleItemMapper {

    /**
     * ✅ MÉTHODE MISE À JOUR avec support des unités
     */
    public static SaleItemResponseDTO toResponseDTO(SaleItem saleItem) {
        if (saleItem == null) {
            return null;
        }

        // Utiliser la méthode fromSaleItem pour remplir automatiquement les champs
        return SaleItemResponseDTO.fromSaleItem(
                saleItem.getSaleItemId(),
                saleItem.getSale().getSaleId(),
                saleItem.getProduct().getProductId(),
                saleItem.getProduct().getName(),
                saleItem.getProduct().getBarcode(),
                saleItem.getQuantitySold(),
                saleItem.getProduct().getUnitPrice(),
                saleItem.getPriceAtSale(),
                saleItem.getProduct().getIsSoldByWeight(),
                saleItem.getProduct().getUnitLabel()
        );
    }

    /**
     * ✅ MÉTHODE ALTERNATIVE plus explicite
     */
    public static SaleItemResponseDTO toResponseDTODetailed(SaleItem saleItem) {
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

        // ✅ NOUVEAU : Propriétés des unités
        dto.setIsSoldByWeight(saleItem.getProduct().getIsSoldByWeight());
        dto.setUnitLabel(saleItem.getProduct().getUnitLabel());

        // ✅ NOUVEAU : Formatage automatique
        dto.setFormattedQuantity(dto.generateFormattedQuantity());
        dto.setFormattedUnitPrice(dto.generateFormattedUnitPrice());
        dto.setFormattedTotalPrice(dto.generateFormattedTotalPrice());

        return dto;
    }
}