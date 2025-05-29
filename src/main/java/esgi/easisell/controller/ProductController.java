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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        ProductResponseDTO result = productService.createProduct(productDTO);
        return result != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.badRequest().body("Erreur lors de la création du produit");
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable UUID productId) {
        ProductResponseDTO product = productService.getProductById(productId);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByClient(@PathVariable UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsByClient(clientId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID productId, @RequestBody ProductDTO productDTO) {
        ProductResponseDTO result = productService.updateProduct(productId, productDTO);
        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body("Erreur lors de la mise à jour du produit");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        boolean deleted = productService.deleteProduct(productId);
        return deleted
                ? ResponseEntity.ok("Produit supprimé avec succès")
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        List<ProductResponseDTO> products = productService.searchProductsByName(clientId, name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/barcode")
    public ResponseEntity<?> findProductByBarcode(
            @RequestParam UUID clientId,
            @RequestParam String barcode) {
        ProductResponseDTO product = productService.findProductByBarcode(clientId, barcode);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/brand")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByBrand(
            @RequestParam UUID clientId,
            @RequestParam String brand) {
        List<ProductResponseDTO> products = productService.getProductsByBrand(clientId, brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/with-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/without-barcode")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithoutBarcode(@RequestParam UUID clientId) {
        List<ProductResponseDTO> products = productService.getProductsWithoutBarcode(clientId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countProductsByClient(@RequestParam UUID clientId) {
        long count = productService.countProductsByClient(clientId);
        return ResponseEntity.ok(count);
    }
}