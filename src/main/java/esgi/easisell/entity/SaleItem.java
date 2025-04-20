package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "SALE_ITEM")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID saleItemId;

    private int quantitySold;
    private BigDecimal priceAtSale;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    @ToString.Exclude
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;
}