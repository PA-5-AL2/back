package esgi.easisell.controller;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.dto.ProductResponseDTO;
import esgi.easisell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import esgi.easisell.entity.Product;
import esgi.easisell.repository.ProductRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ProductController.java
 * @description : Contrôleur REST pour la gestion des produits (avec unités)
 * @author      : Chancy MOUYABI
 * @version     : v2.0.0 (avec support kg/pièce)
 * @date        : 07/01/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;


    // ========== ENDPOINTS EXISTANTS (inchangés) ==========

    /**
     * Créer un nouveau produit
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("🆕 Création produit: {} - Type: {} - Prix: {} €/{}",
                productDTO.getName(),
                productDTO.getIsSoldByWeight() ? "poids" : "pièce",
                productDTO.getUnitPrice(),
                productDTO.getUnitLabel());

        ProductResponseDTO result = productService.createProduct(productDTO);
        return result != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.badRequest().body("Erreur lors de la création du produit");
    }

    /**
     * Récupérer tous les produits du système
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer un produit par son identifiant
     * GET /api/products/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable UUID productId) {
        ProductResponseDTO product = productService.getProductById(productId);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    /**
     * Récupérer tous les produits d'un client spécifique
     * GET /api/products/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByClient(@PathVariable UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsByClient(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer les produits d'une catégorie donnée
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Mettre à jour un produit existant
     * PUT /api/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID productId, @RequestBody ProductDTO productDTO) {
        log.info("🔄 Mise à jour produit {}: {} - Type: {} - Prix: {} €/{}",
                productId,
                productDTO.getName(),
                productDTO.getIsSoldByWeight() ? "poids" : "pièce",
                productDTO.getUnitPrice(),
                productDTO.getUnitLabel());

        ProductResponseDTO result = productService.updateProduct(productId, productDTO);
        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body("Erreur lors de la mise à jour du produit");
    }

    /**
     * Supprimer un produit du système
     * DELETE /api/products/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        boolean deleted = productService.deleteProduct(productId);
        return deleted
                ? ResponseEntity.ok("Produit supprimé avec succès")
                : ResponseEntity.notFound().build();
    }

    /**
     * Rechercher des produits par nom
     * GET /api/products/search?clientId={id}&name={texte}
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        List<ProductResponseDTO> products = productService.searchProductsByName(clientId, name);
        return ResponseEntity.ok(products);
    }

    /**
     * Trouver un produit par son code-barres
     * GET /api/products/barcode?clientId={id}&barcode={code}
     */
    @GetMapping("/barcode")
    public ResponseEntity<?> findProductByBarcode(
            @RequestParam UUID clientId,
            @RequestParam String barcode) {
        ProductResponseDTO product = productService.findProductByBarcode(clientId, barcode);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    /**
     * Récupérer les produits d'une marque spécifique
     * GET /api/products/brand?clientId={id}&brand={marque}
     */
    @GetMapping("/brand")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByBrand(
            @RequestParam UUID clientId,
            @RequestParam String brand) {
        List<ProductResponseDTO> products = productService.getProductsByBrand(clientId, brand);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer les produits ayant un code-barres
     * GET /api/products/with-barcode?clientId={id}
     */
    @GetMapping("/with-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer les produits sans code-barres
     * GET /api/products/without-barcode?clientId={id}
     */
    @GetMapping("/without-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithoutBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithoutBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Compter le nombre total de produits d'un client
     * GET /api/products/count?clientId={id}
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countProductsByClient(@RequestParam UUID clientId) {
        long count = productService.countProductsByClient(clientId);
        return ResponseEntity.ok(count);
    }

    // ========== NOUVEAUX ENDPOINTS POUR GESTION DES UNITÉS ==========

    /**
     * 🍎 Produits vendus au poids (fruits, légumes, viandes)
     * GET /api/products/by-weight?clientId={uuid}
     */
    @GetMapping("/by-weight")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByWeight(@RequestParam UUID clientId) {
        log.info("🍎 Récupération des produits au poids pour le client: {}", clientId);

        List<ProductResponseDTO> weightProducts = productService.getProductsByWeight(clientId);

        log.info("✅ Trouvé {} produits vendus au poids", weightProducts.size());
        return ResponseEntity.ok(weightProducts);
    }

    /**
     * 🍞 Produits vendus à la pièce/unité
     * GET /api/products/by-piece?clientId={uuid}
     */
    @GetMapping("/by-piece")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPiece(@RequestParam UUID clientId) {
        log.info("🍞 Récupération des produits à la pièce pour le client: {}", clientId);

        List<ProductResponseDTO> pieceProducts = productService.getProductsByPiece(clientId);

        log.info("✅ Trouvé {} produits vendus à la pièce", pieceProducts.size());
        return ResponseEntity.ok(pieceProducts);
    }

    /**
     * 📋 Grouper les produits par unité
     * GET /api/products/by-unit?clientId={uuid}
     */
    @GetMapping("/by-unit")
    public ResponseEntity<Map<String, List<ProductResponseDTO>>> getProductsByUnit(@RequestParam UUID clientId) {
        log.info("📋 Groupement des produits par unité pour le client: {}", clientId);

        List<ProductResponseDTO> allProducts = productService.getProductsByClient(clientId);
        Map<String, List<ProductResponseDTO>> productsByUnit = allProducts.stream()
                .collect(Collectors.groupingBy(product ->
                        product.getUnitLabel() != null ? product.getUnitLabel() : "unité"));

        log.info("✅ Produits groupés en {} unités différentes", productsByUnit.size());
        return ResponseEntity.ok(productsByUnit);
    }

    /**
     * 💰 Calculer le prix pour une quantité donnée
     * GET /api/products/{productId}/calculate-price?quantity=2.350
     */
    @GetMapping("/{productId}/calculate-price")
    public ResponseEntity<?> calculatePrice(
            @PathVariable UUID productId,
            @RequestParam BigDecimal quantity) {

        log.info("💰 Calcul du prix pour produit {} avec quantité {}", productId, quantity);

        try {
            ProductResponseDTO product = productService.getProductById(productId);

            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            // Validation quantité positive
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "La quantité doit être positive",
                        "productName", product.getName(),
                        "quantity", quantity
                ));
            }

            // Validation pour les produits à la pièce (doit être entier)
            if (!product.getIsSoldByWeight() && quantity.stripTrailingZeros().scale() > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Ce produit se vend à la pièce (nombre entier uniquement)",
                        "productName", product.getName(),
                        "quantity", quantity,
                        "suggestion", "Utilisez " + quantity.setScale(0, RoundingMode.HALF_UP) + " " + product.getUnitLabel()
                ));
            }

            // Calcul du prix total
            BigDecimal totalPrice = product.getUnitPrice()
                    .multiply(quantity)
                    .setScale(2, RoundingMode.HALF_UP);

            // Formatage de la quantité selon le type
            String formattedQuantity;
            if (product.getIsSoldByWeight()) {
                formattedQuantity = String.format("%.3f %s", quantity, product.getUnitLabel());
            } else {
                formattedQuantity = String.format("%.0f %s", quantity, product.getUnitLabel());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productId", productId);
            response.put("productName", product.getName());
            response.put("unitLabel", product.getUnitLabel());
            response.put("isSoldByWeight", product.getIsSoldByWeight());
            response.put("unitPrice", product.getUnitPrice());
            response.put("formattedUnitPrice", product.getFormattedPrice());
            response.put("quantity", quantity);
            response.put("formattedQuantity", formattedQuantity);
            response.put("totalPrice", totalPrice);
            response.put("formattedTotalPrice", String.format("%.2f €", totalPrice));
            response.put("timestamp", System.currentTimeMillis());

            log.info("✅ Prix calculé: {} pour {}",
                    String.format("%.2f €", totalPrice), formattedQuantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur lors du calcul de prix: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors du calcul: " + e.getMessage()
            ));
        }
    }

    /**
     * ✅ Valider une quantité selon les règles du produit
     * GET /api/products/{productId}/validate-quantity?quantity=2.5
     */
    @GetMapping("/{productId}/validate-quantity")
    public ResponseEntity<?> validateQuantity(
            @PathVariable UUID productId,
            @RequestParam BigDecimal quantity) {

        log.info("✅ Validation de quantité {} pour produit {}", quantity, productId);

        try {
            ProductResponseDTO product = productService.getProductById(productId);

            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            boolean isValid = true;
            String message = "Quantité valide";
            String suggestion = null;

            // Validation quantité positive
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                isValid = false;
                message = "La quantité doit être positive";
            }
            // Validation pour les produits à la pièce (doit être entier)
            else if (!product.getIsSoldByWeight() && quantity.stripTrailingZeros().scale() > 0) {
                isValid = false;
                message = "Ce produit se vend à la pièce (nombre entier uniquement)";
                suggestion = "Essayez " + quantity.setScale(0, RoundingMode.HALF_UP) + " " + product.getUnitLabel();
            }

            // Formatage de la quantité
            String formattedQuantity;
            if (product.getIsSoldByWeight()) {
                formattedQuantity = String.format("%.3f %s", quantity, product.getUnitLabel());
            } else {
                formattedQuantity = String.format("%.0f %s", quantity, product.getUnitLabel());
            }

            Map<String, Object> response = Map.of(
                    "isValid", isValid,
                    "message", message,
                    "productName", product.getName(),
                    "productType", product.getIsSoldByWeight() ? "weight" : "piece",
                    "unitLabel", product.getUnitLabel(),
                    "originalQuantity", quantity,
                    "formattedQuantity", formattedQuantity,
                    "suggestion", suggestion != null ? suggestion : "",
                    "timestamp", System.currentTimeMillis()
            );

            log.info("📊 Validation: {} - {}",
                    isValid ? "✅ VALIDE" : "❌ INVALIDE", message);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "isValid", false,
                    "message", "Erreur lors de la validation: " + e.getMessage()
            ));
        }
    }

    /**
     * 📊 Statistiques des produits par type d'unité
     * GET /api/products/stats?clientId={uuid}
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats(@RequestParam UUID clientId) {
        log.info("📊 Génération des statistiques pour le client: {}", clientId);

        try {
            List<ProductResponseDTO> allProducts = productService.getProductsByClient(clientId);

            long totalProducts = allProducts.size();
            long weightProducts = allProducts.stream()
                    .filter(p -> p.getIsSoldByWeight() != null && p.getIsSoldByWeight())
                    .count();
            long pieceProducts = totalProducts - weightProducts;

            // Grouper par unité
            Map<String, Long> unitStats = allProducts.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getUnitLabel() != null ? p.getUnitLabel() : "unité",
                            Collectors.counting()
                    ));

            // Prix moyen par type
            double avgWeightPrice = allProducts.stream()
                    .filter(p -> p.getIsSoldByWeight() != null && p.getIsSoldByWeight())
                    .mapToDouble(p -> p.getUnitPrice().doubleValue())
                    .average()
                    .orElse(0.0);

            double avgPiecePrice = allProducts.stream()
                    .filter(p -> p.getIsSoldByWeight() == null || !p.getIsSoldByWeight())
                    .mapToDouble(p -> p.getUnitPrice().doubleValue())
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = Map.of(
                    "clientId", clientId,
                    "totalProducts", totalProducts,
                    "weightProducts", weightProducts,
                    "pieceProducts", pieceProducts,
                    "weightPercentage", totalProducts > 0 ? Math.round((weightProducts * 100.0) / totalProducts) : 0,
                    "piecePercentage", totalProducts > 0 ? Math.round((pieceProducts * 100.0) / totalProducts) : 0,
                    "unitDistribution", unitStats,
                    "avgWeightPrice", Math.round(avgWeightPrice * 100.0) / 100.0,
                    "avgPiecePrice", Math.round(avgPiecePrice * 100.0) / 100.0,
                    "timestamp", System.currentTimeMillis()
            );

            log.info("✅ Statistiques générées: {} produits total ({} poids, {} pièces)",
                    totalProducts, weightProducts, pieceProducts);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération des statistiques: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la génération des statistiques: " + e.getMessage()
            ));
        }
    }

    /**
     * 🔄 Convertir un produit pièce vers poids (ou inverse)
     * PUT /api/products/{productId}/convert-unit
     */
    @PutMapping("/{productId}/convert-unit")
    public ResponseEntity<?> convertProductUnit(
            @PathVariable UUID productId,
            @RequestBody Map<String, Object> conversionData) {

        log.info("🔄 Conversion d'unité pour le produit: {}", productId);

        try {
            ProductResponseDTO product = productService.getProductById(productId);

            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            Boolean newIsSoldByWeight = (Boolean) conversionData.get("isSoldByWeight");
            String newUnitLabel = (String) conversionData.get("unitLabel");
            BigDecimal newUnitPrice = conversionData.get("unitPrice") != null ?
                    new BigDecimal(conversionData.get("unitPrice").toString()) : null;

            // Créer DTO de mise à jour
            ProductDTO updateDTO = new ProductDTO();
            updateDTO.setName(product.getName());
            updateDTO.setDescription(product.getDescription());
            updateDTO.setBarcode(product.getBarcode());
            updateDTO.setBrand(product.getBrand());
            updateDTO.setUnitPrice(newUnitPrice != null ? newUnitPrice : product.getUnitPrice());
            updateDTO.setIsSoldByWeight(newIsSoldByWeight);
            updateDTO.setUnitLabel(newUnitLabel);

            ProductResponseDTO updatedProduct = productService.updateProduct(productId, updateDTO);

            if (updatedProduct != null) {
                log.info("✅ Produit converti: {} -> Type: {} ({})",
                        updatedProduct.getName(),
                        updatedProduct.getIsSoldByWeight() ? "poids" : "pièce",
                        updatedProduct.getFormattedPrice());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Produit converti avec succès",
                        "product", updatedProduct,
                        "oldType", product.getIsSoldByWeight() ? "poids" : "pièce",
                        "newType", updatedProduct.getIsSoldByWeight() ? "poids" : "pièce"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Erreur lors de la conversion"
                ));
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la conversion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors de la conversion: " + e.getMessage()
            ));
        }
    }

    /**
     * 🏷️ Suggestion de prix basée sur des produits similaires
     * GET /api/products/suggest-price?clientId={uuid}&name={nom}&unitLabel={unite}
     */
    @GetMapping("/suggest-price")
    public ResponseEntity<?> suggestPrice(
            @RequestParam UUID clientId,
            @RequestParam String name,
            @RequestParam(required = false) String unitLabel) {

        log.info("🏷️ Suggestion de prix pour: {} (unité: {})", name, unitLabel);

        try {
            List<ProductResponseDTO> allProducts = productService.getProductsByClient(clientId);

            // Rechercher des produits similaires par nom
            List<ProductResponseDTO> similarProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()) ||
                            name.toLowerCase().contains(p.getName().toLowerCase()))
                    .collect(Collectors.toList());

            // Si une unité est spécifiée, filtrer par unité
            if (unitLabel != null && !unitLabel.isEmpty()) {
                similarProducts = similarProducts.stream()
                        .filter(p -> unitLabel.equalsIgnoreCase(p.getUnitLabel()))
                        .collect(Collectors.toList());
            }

            if (similarProducts.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "suggestion", "Aucun produit similaire trouvé",
                        "recommendedPrice", 0.00,
                        "similarProducts", List.of()
                ));
            }

            // Calculer prix moyen
            double avgPrice = similarProducts.stream()
                    .mapToDouble(p -> p.getUnitPrice().doubleValue())
                    .average()
                    .orElse(0.0);

            BigDecimal recommendedPrice = BigDecimal.valueOf(avgPrice)
                    .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> response = Map.of(
                    "suggestion", "Prix basé sur " + similarProducts.size() + " produits similaires",
                    "recommendedPrice", recommendedPrice,
                    "formattedPrice", String.format("%.2f €/%s",
                            recommendedPrice.doubleValue(),
                            unitLabel != null ? unitLabel : "unité"),
                    "similarProducts", similarProducts.stream()
                            .limit(5) // Limiter à 5 exemples
                            .map(p -> Map.of(
                                    "name", p.getName(),
                                    "price", p.getUnitPrice(),
                                    "formattedPrice", p.getFormattedPrice()
                            ))
                            .collect(Collectors.toList()),
                    "totalSimilar", similarProducts.size()
            );

            log.info("✅ Prix suggéré: {} € basé sur {} produits similaires",
                    recommendedPrice, similarProducts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la suggestion de prix: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la suggestion: " + e.getMessage()
            ));
        }
    }
    // === NOUVEAUX ENDPOINTS STOCK ===

    /**
     * Produits en stock faible
     * GET /api/products/low-stock?clientId={uuid}
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDTO>> getLowStockProducts(@RequestParam UUID clientId) {
        List<Product> products = productRepository.findLowStockProducts(clientId);
        List<ProductResponseDTO> response = products.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Produits qui expirent bientôt
     * GET /api/products/expiring?clientId={uuid}&days=7
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<ProductResponseDTO>> getExpiringProducts(
            @RequestParam UUID clientId,
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(days);
        List<Product> products = productRepository.findExpiringProducts(clientId, Timestamp.valueOf(futureDate));
        List<ProductResponseDTO> response = products.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Produits en rupture de stock
     * GET /api/products/out-of-stock?clientId={uuid}
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductResponseDTO>> getOutOfStockProducts(@RequestParam UUID clientId) {
        List<Product> products = productRepository.findOutOfStockProducts(clientId);
        List<ProductResponseDTO> response = products.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Ajuster la quantité d'un produit
     * PATCH /api/products/{productId}/adjust-quantity?change=5
     */
    @PatchMapping("/{productId}/adjust-quantity")
    public ResponseEntity<?> adjustQuantity(
            @PathVariable UUID productId,
            @RequestParam int change) {

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        int newQuantity = product.getQuantity() + change;

        if (newQuantity < 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantité insuffisante",
                    "currentQuantity", product.getQuantity(),
                    "requestedChange", change
            ));
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);

        return ResponseEntity.ok(Map.of(
                "message", "Quantité ajustée avec succès",
                "productName", product.getName(),
                "oldQuantity", product.getQuantity() - change,
                "newQuantity", newQuantity,
                "change", change
        ));
    }
}