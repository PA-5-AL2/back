package esgi.easisell.service;

import esgi.easisell.dto.SaleTotalDTO;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.service.interfaces.ISalePriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service  // Changé de @Component à @Service
@RequiredArgsConstructor
class SalePriceCalculatorImpl implements ISalePriceCalculator {

    @Override
    public BigDecimal calculateItemPrice(Product product, int quantity) {
        return product.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public BigDecimal calculateTotal(List<SaleItem> items) {
        return items.stream()
                .map(SaleItem::getPriceAtSale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public SaleTotalDTO calculateSaleTotal(Sale sale) {
        BigDecimal subtotal = calculateTotal(sale.getSaleItems());
        BigDecimal taxAmount = BigDecimal.ZERO; // TODO: Implémenter les taxes
        BigDecimal discountAmount = BigDecimal.ZERO; // TODO: Implémenter les remises

        return SaleTotalDTO.builder()
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(subtotal.add(taxAmount).subtract(discountAmount))
                .currency(sale.getClient().getCurrencyPreference())
                .build();
    }
}
