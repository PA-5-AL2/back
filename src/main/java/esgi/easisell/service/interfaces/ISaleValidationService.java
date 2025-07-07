package esgi.easisell.service.interfaces;

import esgi.easisell.entity.Sale;

import java.util.UUID;

public interface ISaleValidationService {
    boolean canAccessSale(UUID saleId, UUID userId);
    void validateSaleNotFinalized(Sale sale);
}

