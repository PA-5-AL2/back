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
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID stockItemId;

    // Relations
    @ManyToOne
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    // Champs
    private int quantity;
    private int reorderThreshold;
    private Timestamp purchaseDate;
    private Timestamp expirationDate;
    private BigDecimal purchasePrice;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    private Supplier supplier;
}