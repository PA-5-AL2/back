package esgi.easisell.service;

import esgi.easisell.dto.ProductDTO;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;

    /**
     * Créer un nouveau produit
     */
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        log.info("Création d'un nouveau produit : {}", productDTO.getName());

        Client client = clientRepository.findById(UUID.fromString(productDTO.getClientId()))
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + productDTO.getClientId()));

        Category category = null;
        if (productDTO.getCategoryId() != null && !productDTO.getCategoryId().isEmpty()) {
            category = categoryRepository.findById(UUID.fromString(productDTO.getCategoryId()))
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + productDTO.getCategoryId()));
        }

        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isEmpty()) {
            boolean exists = productRepository.existsByClientIdAndBarcode(
                    UUID.fromString(productDTO.getClientId()),
                    productDTO.getBarcode());

            if (exists) {
                throw new RuntimeException("Ce code-barres est déjà utilisé pour ce client");
            }
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBarcode(productDTO.getBarcode());
        product.setBrand(productDTO.getBrand());
        product.setUnitPrice(productDTO.getUnitPrice());
        product.setCategory(category);
        product.setClient(client);

        Product savedProduct = productRepository.save(product);
        log.info("Produit créé avec succès. ID: {}", savedProduct.getProductId());

        return savedProduct;
    }

    /**
     * Obtenir tous les produits
     */
    public List<Product> getAllProducts() {
        log.info("Récupération de tous les produits");
        return productRepository.findAll();
    }

    /**
     * Obtenir les produits d'un client
     */
    public List<Product> getProductsByClient(UUID clientId) {
        log.info("Récupération des produits pour le client ID: {}", clientId);
        return productRepository.findByClientUserId(clientId);
    }

    /**
     * Obtenir les produits d'une catégorie
     */
    public List<Product> getProductsByCategory(UUID categoryId) {
        log.info("Récupération des produits pour la catégorie ID: {}", categoryId);
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    /**
     * Obtenir un produit par ID
     */
    public Optional<Product> getProductById(UUID productId) {
        log.info("Récupération du produit ID: {}", productId);
        return productRepository.findById(productId);
    }

    /**
     * Mettre à jour un produit
     */
    @Transactional
    public Optional<Product> updateProduct(UUID productId, ProductDTO productDTO) {
        log.info("Mise à jour du produit ID: {}", productId);

        return productRepository.findById(productId)
                .map(product -> {
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

                    if (productDTO.getBarcode() != null &&
                            !productDTO.getBarcode().equals(product.getBarcode())) {

                        if (!productDTO.getBarcode().isEmpty() &&
                                productRepository.existsByClientIdAndBarcode(
                                        product.getClient().getUserId(),
                                        productDTO.getBarcode())) {

                            throw new RuntimeException("Ce code-barres est déjà utilisé pour ce client");
                        }

                        product.setBarcode(productDTO.getBarcode());
                    }

                    if (productDTO.getCategoryId() != null) {
                        if (productDTO.getCategoryId().isEmpty()) {
                            product.setCategory(null);
                        } else {
                            Category category = categoryRepository.findById(UUID.fromString(productDTO.getCategoryId()))
                                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + productDTO.getCategoryId()));
                            product.setCategory(category);
                        }
                    }

                    Product updated = productRepository.save(product);
                    log.info("Produit mis à jour avec succès. ID: {}", productId);
                    return updated;
                });
    }

    /**
     * Supprimer un produit
     */
    @Transactional
    public boolean deleteProduct(UUID productId) {
        log.info("Suppression du produit ID: {}", productId);

        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            log.info("Produit supprimé avec succès. ID: {}", productId);
            return true;
        }

        log.warn("Produit non trouvé pour la suppression. ID: {}", productId);
        return false;
    }

    /**
     * Rechercher des produits par nom
     */
    public List<Product> searchProductsByName(UUID clientId, String name) {
        log.info("Recherche de produits contenant '{}' pour le client ID: {}", name, clientId);
        return productRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, name);
    }

    /**
     * Rechercher un produit par code-barres
     */
    public Product findProductByBarcode(UUID clientId, String barcode) {
        log.info("Recherche de produit avec le code-barres '{}' pour le client ID: {}", barcode, clientId);
        return productRepository.findByClientAndBarcode(clientId, barcode);
    }
}