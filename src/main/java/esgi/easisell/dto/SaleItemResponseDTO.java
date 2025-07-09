package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponseDTO {
    private UUID saleItemId;
    private UUID saleId;
    private UUID productId;
    private String productName;
    private String productBarcode;
    private BigDecimal quantitySold;
    private BigDecimal unitPrice;
    private BigDecimal priceAtSale;
    private BigDecimal totalPrice;

    // ========== NOUVELLES PROPRIÉTÉS POUR UNITÉS ==========

    /**
     * Type de vente : true = au poids, false = à la pièce
     */
    private Boolean isSoldByWeight;

    /**
     * Unité du produit : "kg", "pièce", "L", etc.
     */
    private String unitLabel;

    /**
     * Quantité formatée avec unité : "2.350 kg", "3 pièces"
     */
    private String formattedQuantity;

    /**
     * Prix unitaire formaté : "4.50 €/kg", "2.80 €/pièce"
     */
    private String formattedUnitPrice;

    /**
     * Prix total formaté : "10.58 €"
     */
    private String formattedTotalPrice;

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Génère la quantité formatée selon le type de produit
     */
    public String generateFormattedQuantity() {
        if (quantitySold == null || unitLabel == null) {
            return "0 unité";
        }

        if (isSoldByWeight != null && isSoldByWeight) {
            // Pour les produits au poids : 3 décimales
            return String.format("%.3f %s", quantitySold, unitLabel);
        } else {
            // Pour les pièces : sans décimales si entier
            if (quantitySold.stripTrailingZeros().scale() <= 0) {
                return String.format("%.0f %s", quantitySold, unitLabel);
            } else {
                return String.format("%.2f %s", quantitySold, unitLabel);
            }
        }
    }

    /**
     * Génère le prix unitaire formaté
     */
    public String generateFormattedUnitPrice() {
        if (unitPrice == null || unitLabel == null) {
            return "0.00 €/unité";
        }
        return String.format("%.2f €/%s", unitPrice, unitLabel);
    }

    /**
     * Génère le prix total formaté
     */
    public String generateFormattedTotalPrice() {
        if (totalPrice == null) {
            return "0.00 €";
        }
        return String.format("%.2f €", totalPrice);
    }

    // ========== CONSTRUCTEUR AVEC PRODUCT ==========

    /**
     * Constructeur qui remplit automatiquement les champs formatés
     * À utiliser dans vos mappers/services
     */
    public static SaleItemResponseDTO fromSaleItem(UUID saleItemId, UUID saleId,
                                                   UUID productId, String productName, String productBarcode,
                                                   BigDecimal quantitySold, BigDecimal unitPrice, BigDecimal priceAtSale,
                                                   Boolean isSoldByWeight, String unitLabel) {

        SaleItemResponseDTO dto = new SaleItemResponseDTO();
        dto.setSaleItemId(saleItemId);
        dto.setSaleId(saleId);
        dto.setProductId(productId);
        dto.setProductName(productName);
        dto.setProductBarcode(productBarcode);
        dto.setQuantitySold(quantitySold);
        dto.setUnitPrice(unitPrice);
        dto.setPriceAtSale(priceAtSale);
        dto.setTotalPrice(priceAtSale); // totalPrice = priceAtSale
        dto.setIsSoldByWeight(isSoldByWeight);
        dto.setUnitLabel(unitLabel);

        // Générer les champs formatés automatiquement
        dto.setFormattedQuantity(dto.generateFormattedQuantity());
        dto.setFormattedUnitPrice(dto.generateFormattedUnitPrice());
        dto.setFormattedTotalPrice(dto.generateFormattedTotalPrice());

        return dto;
    }
}