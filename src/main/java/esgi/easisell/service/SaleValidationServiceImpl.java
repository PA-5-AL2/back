package esgi.easisell.service;

import esgi.easisell.entity.Sale;
import esgi.easisell.exception.SaleAlreadyFinalizedException;
import esgi.easisell.repository.SaleRepository;
import esgi.easisell.service.interfaces.ISaleValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

// SERVICES AUXILIAIRES - Interface Segregation Principle
@Service
@RequiredArgsConstructor
class SaleValidationServiceImpl implements ISaleValidationService {

    private final SaleRepository saleRepository;

    @Override
    public boolean canAccessSale(UUID saleId, UUID userId) {
        return saleRepository.existsByIdAndClientId(saleId, userId);
    }

    @Override
    public void validateSaleNotFinalized(Sale sale) {
        if (!sale.getPayments().isEmpty()) {
            throw new SaleAlreadyFinalizedException(
                    "Cette vente est déjà finalisée");
        }
    }
}
