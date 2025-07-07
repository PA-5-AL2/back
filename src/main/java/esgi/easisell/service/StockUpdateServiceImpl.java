package esgi.easisell.service;

import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.exception.StockUpdateException;
import esgi.easisell.service.interfaces.IStockUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class StockUpdateServiceImpl implements IStockUpdateService {

    private final StockItemService stockItemService;

    @Override
    @Transactional
    public void decreaseStockForSale(Sale sale) {
        for (SaleItem item : sale.getSaleItems()) {
            boolean success = stockItemService.adjustStockQuantity(
                    item.getProduct().getProductId(),
                    -item.getQuantitySold()
            );

            if (!success) {
                throw new StockUpdateException(
                        "Erreur lors de la mise à jour du stock pour: " +
                                item.getProduct().getName());
            }
        }
    }
}