package esgi.easisell.service;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.dto.ProductResponseDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.repository.CategoryRepository;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductDTO productDTO) {
        log.info("Création d'un nouveau produit : {} (Type: {})",
                productDTO.getName(),
                productDTO.getIsSoldByWeight() ? "au poids" : "à la pièce");

        if (!isValidProductDTO(productDTO)) {
            return null;
        }

        UUID clientUUID = parseUUID(productDTO.getClientId());
        if (clientUUID == null) {
            return null;
        }

        Optional<Client> clientOpt = clientRepository.findById(clientUUID);
        if (clientOpt.isEmpty()) {
            return null;
        }

        Category category = null;
        if (hasValidCategoryId(productDTO.getCategoryId())) {
            UUID categoryUUID = parseUUID(productDTO.getCategoryId());
            if (categoryUUID == null) {
                return null;
            }

            Optional<Category> categoryOpt = categoryRepository.findById(categoryUUID);
            if (categoryOpt.isEmpty()) {
                return null;
            }
            category = categoryOpt.get();
        }

        if (isBarcodeAlreadyExists(clientUUID, productDTO.getBarcode())) {
            return null;
        }

        Product product = buildProduct(productDTO, clientOpt.get(), category);
        Product savedProduct = productRepository.save(product);

        log.info("Produit créé avec succès. ID: {} - Prix: {}",
                savedProduct.getProductId(),
                savedProduct.getFormattedPrice());

        return convertToResponseDTO(savedProduct);
    }

    public List<ProductResponseDTO> getAllProducts() {
        log.info("Récupération de tous les produits");
        return productRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsByClient(UUID clientId) {
        log.info("Récupération des produits pour le client ID: {}", clientId);
        return productRepository.findByClientUserId(clientId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(UUID productId) {
        log.info("Récupération du produit ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);
        return productOpt.map(this::convertToResponseDTO).orElse(null);
    }

    @Transactional
    public ProductResponseDTO updateProduct(UUID productId, ProductDTO productDTO) {
        log.info("Mise à jour du produit ID: {} (Type: {})",
                productId,
                productDTO.getIsSoldByWeight() ? "au poids" : "à la pièce");

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return null;
        }

        Product product = productOpt.get();
        updateProductFields(product, productDTO);

        if (shouldUpdateBarcode(product, productDTO)) {
            if (isBarcodeAlreadyExists(product.getClient().getUserId(), productDTO.getBarcode())) {
                return null;
            }
            product.setBarcode(productDTO.getBarcode());
        }

        if (shouldUpdateCategory(productDTO)) {
            Category category = findCategoryById(productDTO.getCategoryId());
            if (productDTO.getCategoryId() != null && !productDTO.getCategoryId().isEmpty() && category == null) {
                return null;
            }
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Produit mis à jour avec succès. ID: {} - Nouveau prix: {}",
                productId, updatedProduct.getFormattedPrice());

        return convertToResponseDTO(updatedProduct);
    }

    @Transactional
    public boolean deleteProduct(UUID productId) {
        log.info("Suppression du produit ID: {}", productId);
        if (!productRepository.existsById(productId)) {
            return false;
        }

        productRepository.deleteById(productId);
        log.info("Produit supprimé avec succès. ID: {}", productId);
        return true;
    }

    public List<ProductResponseDTO> searchProductsByName(UUID clientId, String name) {
        log.info("Recherche de produits contenant '{}' pour le client ID: {}", name, clientId);
        return productRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, name).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO findProductByBarcode(UUID clientId, String barcode) {
        log.info("Recherche de produit avec le code-barres '{}' pour le client ID: {}", barcode, clientId);
        Product product = productRepository.findByClientAndBarcode(clientId, barcode);
        return product != null ? convertToResponseDTO(product) : null;
    }

    public List<ProductResponseDTO> getProductsByCategory(UUID categoryId) {
        log.info("Récupération des produits pour la catégorie ID: {}", categoryId);
        return productRepository.findByCategoryCategoryId(categoryId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsByBrand(UUID clientId, String brand) {
        log.info("Récupération des produits de la marque '{}' pour le client ID: {}", brand, clientId);
        return productRepository.findByClientUserIdAndBrandIgnoreCase(clientId, brand).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsWithBarcode(UUID clientId) {
        log.info("Récupération des produits avec code-barres pour le client ID: {}", clientId);
        return productRepository.findByClientUserIdAndBarcodeIsNotNull(clientId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsWithoutBarcode(UUID clientId) {
        log.info("Récupération des produits sans code-barres pour le client ID: {}", clientId);
        return productRepository.findByClientUserIdAndBarcodeIsNull(clientId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public long countProductsByClient(UUID clientId) {
        log.info("Comptage des produits pour le client ID: {}", clientId);
        return productRepository.countByClientUserId(clientId);
    }

    // ========== NOUVELLES MÉTHODES POUR GESTION DES UNITÉS ==========

    /**
     * Récupère les produits vendus au poids (fruits, légumes, viandes)
     */
    public List<ProductResponseDTO> getProductsByWeight(UUID clientId) {
        log.info("🍎 Récupération des produits au poids pour le client ID: {}", clientId);
        return productRepository.findByClientUserId(clientId).stream()
                .filter(product -> product.getIsSoldByWeight() != null && product.getIsSoldByWeight())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les produits vendus à la pièce
     */
    public List<ProductResponseDTO> getProductsByPiece(UUID clientId) {
        log.info("🍞 Récupération des produits à la pièce pour le client ID: {}", clientId);
        return productRepository.findByClientUserId(clientId).stream()
                .filter(product -> product.getIsSoldByWeight() == null || !product.getIsSoldByWeight())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Convertit une entité Product en ProductResponseDTO
     * ✅ MISE À JOUR avec les nouvelles propriétés
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO(product);

        // ✅ AJOUT des nouvelles propriétés pour les unités
        dto.setIsSoldByWeight(product.getIsSoldByWeight());
        dto.setUnitLabel(product.getUnitLabel());
        dto.setFormattedPrice(product.getFormattedPrice());

        return dto;
    }

    /**
     * Construit un Product à partir d'un ProductDTO
     * ✅ MISE À JOUR avec les nouvelles propriétés
     */
    private Product buildProduct(ProductDTO productDTO, Client client, Category category) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBarcode(productDTO.getBarcode());
        product.setBrand(productDTO.getBrand());
        product.setUnitPrice(productDTO.getUnitPrice());
        product.setCategory(category);
        product.setClient(client);

        // ✅ AJOUT des nouvelles propriétés pour les unités
        product.setIsSoldByWeight(productDTO.getIsSoldByWeight() != null ? productDTO.getIsSoldByWeight() : false);
        product.setUnitLabel(productDTO.getUnitLabel() != null ? productDTO.getUnitLabel() : "pièce");

        return product;
    }

    /**
     * Met à jour les champs d'un Product
     * ✅ MISE À JOUR avec les nouvelles propriétés
     */
    private void updateProductFields(Product product, ProductDTO productDTO) {
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getBrand() != null) {
            product.setBrand(productDTO.getBrand());
        }
        if (productDTO.getUnitPrice() != null) {
            product.setUnitPrice(productDTO.getUnitPrice());
        }

        // ✅ AJOUT mise à jour des nouvelles propriétés
        if (productDTO.getIsSoldByWeight() != null) {
            product.setIsSoldByWeight(productDTO.getIsSoldByWeight());
        }
        if (productDTO.getUnitLabel() != null) {
            product.setUnitLabel(productDTO.getUnitLabel());
        }
    }

    // ========== MÉTHODES UTILITAIRES EXISTANTES (non modifiées) ==========

    private boolean isValidProductDTO(ProductDTO productDTO) {
        return productDTO.getName() != null && !productDTO.getName().trim().isEmpty() &&
                productDTO.getClientId() != null && !productDTO.getClientId().trim().isEmpty() &&
                productDTO.getUnitPrice() != null;
    }

    private boolean hasValidCategoryId(String categoryId) {
        return categoryId != null && !categoryId.trim().isEmpty();
    }

    private boolean isBarcodeAlreadyExists(UUID clientId, String barcode) {
        return barcode != null && !barcode.isEmpty() &&
                productRepository.existsByClientIdAndBarcode(clientId, barcode);
    }

    private boolean shouldUpdateBarcode(Product product, ProductDTO productDTO) {
        return productDTO.getBarcode() != null &&
                !productDTO.getBarcode().equals(product.getBarcode());
    }

    private boolean shouldUpdateCategory(ProductDTO productDTO) {
        return productDTO.getCategoryId() != null;
    }

    private Category findCategoryById(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return null;
        }

        UUID categoryUUID = parseUUID(categoryId);
        if (categoryUUID == null) {
            return null;
        }

        Optional<Category> categoryOpt = categoryRepository.findById(categoryUUID);
        return categoryOpt.orElse(null);
    }

    private UUID parseUUID(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            return null;
        }

        if (!isValidUUIDFormat(uuidString)) {
            return null;
        }

        return UUID.fromString(uuidString);
    }

    private boolean isValidUUIDFormat(String uuid) {
        return uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}