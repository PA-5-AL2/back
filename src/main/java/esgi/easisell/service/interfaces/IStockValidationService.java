package esgi.easisell.service.interfaces;

import esgi.easisell.entity.Product;
import java.util.UUID;

public interface IStockValidationService {
    void validateStockAvailable(Product product, UUID clientId, int requestedQuantity);
}