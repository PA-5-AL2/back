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
    @Column(name = "sale_item_id")
    private UUID saleItemId;

    @Column(nullable = false)
    private int quantitySold;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;
}