/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : Product.java
 * @description : Entité produit commercialisé
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
import java.math.RoundingMode;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PRODUCT",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "barcode"})
        })
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "barcode")
    private String barcode;

    private String brand; // MARQUE : CACO...

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<StockItem> stockItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Promotion> promotions;

    /**
     * Type de vente : true = au poids (kg), false = à la pièce
     */
    @Column(nullable = false)
    private Boolean isSoldByWeight = false;

    /**
     * Unité d'affichage : "kg", "pièce", "L", "paquet"
     */
    @Column(length = 10, nullable = false)
    private String unitLabel = "pièce";

    /**
     * Calcule le prix total pour une quantité (avec 2 décimales)
     */
    public BigDecimal calculateTotalPrice(BigDecimal quantity) {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formate le prix : "4.50 €/kg" ou "2.80 €/pièce"
     */
    public String getFormattedPrice() {
        return String.format("%.2f €/%s", unitPrice, unitLabel);
    }

    // Getters/Setters
    public Boolean getIsSoldByWeight() { return isSoldByWeight; }
    public void setIsSoldByWeight(Boolean isSoldByWeight) { this.isSoldByWeight = isSoldByWeight; }
    public String getUnitLabel() { return unitLabel; }
    public void setUnitLabel(String unitLabel) { this.unitLabel = unitLabel; }
}