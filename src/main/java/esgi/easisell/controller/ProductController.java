package esgi.easisell.controller;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.entity.Product;
import esgi.easisell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Créer un nouveau produit
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            Product product = productService.createProduct(productDTO);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Erreur lors de la création du produit", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer tous les produits
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Récupérer les produits d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Product>> getProductsByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(productService.getProductsByClient(clientId));
    }

    /**
     * Récupérer les produits d'une catégorie
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * Récupérer un produit par ID
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour un produit
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID productId,
                                           @RequestBody ProductDTO productDTO) {
        try {
            return productService.updateProduct(productId, productDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer un produit
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        boolean deleted = productService.deleteProduct(productId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Produit supprimé avec succès"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rechercher des produits par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(clientId, name));
    }

    /**
     * Rechercher un produit par code-barres
     */
    @GetMapping("/barcode")
    public ResponseEntity<?> findProductByBarcode(
            @RequestParam UUID clientId,
            @RequestParam String barcode) {
        Product product = productService.findProductByBarcode(clientId, barcode);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}