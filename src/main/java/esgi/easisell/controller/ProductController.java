package esgi.easisell.controller;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.dto.ProductResponseDTO;
import esgi.easisell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ProductController.java
 * @description : Contrôleur REST pour la gestion des produits
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 01/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * Ce contrôleur expose les endpoints REST pour la gestion complète des produits :
 * - CRUD complet (Create, Read, Update, Delete)
 * - Recherche et filtrage avancé
 * - Gestion des codes-barres
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Créer un nouveau produit
     * POST /api/products
     *
     * @param productDTO les données du produit à créer
     * @return le produit créé avec son ID ou erreur
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        ProductResponseDTO result = productService.createProduct(productDTO);
        return result != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.badRequest().body("Erreur lors de la création du produit");
    }

    /**
     * Récupérer tous les produits du système
     * GET /api/products
     * ATTENTION : Endpoint admin - retourne TOUS les produits
     *
     * @return la liste complète des produits
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer un produit par son identifiant
     * GET /api/products/{productId}
     *
     * @param productId l'identifiant unique du produit
     * @return le produit trouvé ou 404 si inexistant
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
     * Endpoint principal pour les supérettes
     *
     * @param clientId l'identifiant du client propriétaire
     * @return la liste des produits de ce client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByClient(@PathVariable UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsByClient(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer les produits d'une catégorie donnée
     * GET /api/products/category/{categoryId}
     *
     * @param categoryId l'identifiant de la catégorie
     * @return la liste des produits de cette catégorie
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Mettre à jour un produit existant
     * PUT /api/products/{productId}
     * Note : Mise à jour complète (tous les champs)
     *
     * @param productId  l'identifiant du produit à modifier
     * @param productDTO les nouvelles données du produit
     * @return le produit mis à jour ou erreur
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID productId, @RequestBody ProductDTO productDTO) {
        ProductResponseDTO result = productService.updateProduct(productId, productDTO);
        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body("Erreur lors de la mise à jour du produit");
    }

    /**
     * Supprimer un produit du système
     * DELETE /api/products/{productId}
     * ATTENTION : Suppression définitive (cascade sur stock, ventes, etc.)
     *
     * @param productId l'identifiant du produit à supprimer
     * @return confirmation de suppression ou 404 si inexistant
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
     * Recherche insensible à la casse avec LIKE %texte%
     *
     * @param clientId l'identifiant du client (scope de recherche)
     * @param name     le texte à rechercher dans le nom du produit
     * @return la liste des produits correspondants
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
     * Endpoint critique pour le scan en caisse !
     *
     * @param clientId l'identifiant du client
     * @param barcode  le code-barres du produit recherché
     * @return le produit trouvé ou 404 si inexistant
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
     *
     * @param clientId l'identifiant du client
     * @param brand    le nom de la marque
     * @return la liste des produits de cette marque
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
     * Utile pour les caisses automatiques / scan
     *
     * @param clientId l'identifiant du client
     * @return la liste des produits avec code-barres
     */
    @GetMapping("/with-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupérer les produits sans code-barres
     * GET /api/products/without-barcode?clientId={id}
     * Utile pour identifier les produits à étiqueter
     *
     * @param clientId l'identifiant du client
     * @return la liste des produits sans code-barres
     */
    @GetMapping("/without-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithoutBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithoutBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    /**
     * Compter le nombre total de produits d'un client
     * GET /api/products/count?clientId={id}
     * Utile pour les dashboards et statistiques
     *
     * @param clientId l'identifiant du client
     * @return le nombre de produits
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countProductsByClient(@RequestParam UUID clientId) {
        long count = productService.countProductsByClient(clientId);
        return ResponseEntity.ok(count);
    }
}