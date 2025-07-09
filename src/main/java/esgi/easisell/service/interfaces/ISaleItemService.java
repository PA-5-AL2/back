package esgi.easisell.service.interfaces;

import esgi.easisell.dto.SaleItemResponseDTO;

import java.math.BigDecimal;
import java.util.UUID;

public interface ISaleItemService {
    SaleItemResponseDTO addProductToSale(UUID saleId, String barcode, BigDecimal quantity); // ✅ BigDecimal
    SaleItemResponseDTO addProductByIdToSale(UUID saleId, UUID productId, BigDecimal quantity); // ✅ BigDecimal
    SaleItemResponseDTO updateItemQuantity(UUID saleItemId, BigDecimal newQuantity); // ✅ BigDecimal
    void removeItemFromSale(UUID saleItemId);
}
