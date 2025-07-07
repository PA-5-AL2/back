package esgi.easisell.service;

import esgi.easisell.entity.Product;
import esgi.easisell.exception.InsufficientStockException;
import esgi.easisell.service.interfaces.IStockValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class StockValidationServiceImpl implements IStockValidationService {

    private final StockItemService stockItemService;

    @Override
    public void validateStockAvailable(Product product, UUID clientId, int requestedQuantity) {
        int availableStock = stockItemService.getTotalStockQuantityByProduct(
                clientId, product.getProductId());

        if (availableStock < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                            product.getName(), availableStock, requestedQuantity));
        }
    }
}