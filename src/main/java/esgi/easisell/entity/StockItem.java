/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockItem.java
 * @description : Entité lot de stock d'un produit
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
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

    @Version
    @Column(name = "version")
    private Long version;

    //Timestamp de dernière modification
    @Column(name = "last_modified")
    private Timestamp lastModified;

    //Callback pour mettre à jour automatiquement le timestamp
    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.lastModified = new Timestamp(System.currentTimeMillis());
    }
}