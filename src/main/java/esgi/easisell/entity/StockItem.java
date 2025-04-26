package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STOCK_ITEM")
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stock_item_id")
    private UUID stockItemId;

    @Column(nullable = false)
    private Integer quantity;

    private Integer reorderThreshold;
    private Timestamp purchaseDate;
    private Timestamp expirationDate;
    private BigDecimal purchasePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    private Supplier supplier;
}