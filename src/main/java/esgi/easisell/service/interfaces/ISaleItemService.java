package esgi.easisell.service.interfaces;

import esgi.easisell.dto.SaleItemResponseDTO;
import java.util.UUID;

public interface ISaleItemService {
    SaleItemResponseDTO addProductToSale(UUID saleId, String barcode, int quantity);
    SaleItemResponseDTO addProductByIdToSale(UUID saleId, UUID productId, int quantity);
    SaleItemResponseDTO updateItemQuantity(UUID saleItemId, int newQuantity);
    void removeItemFromSale(UUID saleItemId);
}