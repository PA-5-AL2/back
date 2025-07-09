package esgi.easisell.service.interfaces;

import esgi.easisell.dto.SaleTotalDTO;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import java.math.BigDecimal;
import java.util.List;

public interface ISalePriceCalculator {
    BigDecimal calculateItemPrice(Product product, BigDecimal quantity);
    BigDecimal calculateTotal(List<SaleItem> items);
    SaleTotalDTO calculateSaleTotal(Sale sale);
}