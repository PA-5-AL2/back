package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemDTO {
    private String productName;
    private String barcode;
    private int quantity;  // Garde int pour compatibilité legacy
    private BigDecimal unitPrice;
    private BigDecimal total;

    // ========== NOUVELLES PROPRIÉTÉS POUR UNITÉS ==========

    /**
     * Quantité formatée avec unité : "2.350 kg", "3 pièces"
     * Utilisé pour l'affichage sur le ticket de caisse
     */
    private String formattedQuantity;

    /**
     * Prix unitaire formaté : "4.50 €/kg", "2.80 €/pièce"
     */
    private String formattedUnitPrice;

    /**
     * Type de vente : true = au poids, false = à la pièce
     */
    private Boolean isSoldByWeight;

    /**
     * Unité du produit : "kg", "pièce", "L", etc.
     */
    private String unitLabel;

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Constructeur helper pour créer un ReceiptItemDTO avec formatage automatique
     */
    public static ReceiptItemDTO fromSaleItem(String productName, String barcode,
                                              BigDecimal quantitySold, BigDecimal unitPrice,
                                              BigDecimal total, Boolean isSoldByWeight,
                                              String unitLabel) {
        ReceiptItemDTO dto = new ReceiptItemDTO();
        dto.setProductName(productName);
        dto.setBarcode(barcode);
        dto.setQuantity(quantitySold.intValue()); // Pour compatibilité legacy
        dto.setUnitPrice(unitPrice);
        dto.setTotal(total);
        dto.setIsSoldByWeight(isSoldByWeight);
        dto.setUnitLabel(unitLabel);

        // Formatage automatique
        if (isSoldByWeight != null && isSoldByWeight) {
            dto.setFormattedQuantity(String.format("%.3f %s", quantitySold, unitLabel));
        } else {
            dto.setFormattedQuantity(String.format("%.0f %s", quantitySold, unitLabel));
        }

        dto.setFormattedUnitPrice(String.format("%.2f €/%s", unitPrice, unitLabel));

        return dto;
    }

    /**
     * Génère la ligne de ticket : "Tomates cerises: 2.350 kg × 4.50 €/kg = 10.58 €"
     */
    public String generateReceiptLine() {
        if (formattedQuantity != null && formattedUnitPrice != null) {
            return String.format("%s: %s × %s = %.2f €",
                    productName, formattedQuantity, formattedUnitPrice, total);
        } else {
            // Fallback legacy
            return String.format("%s: %d × %.2f € = %.2f €",
                    productName, quantity, unitPrice, total);
        }
    }
}