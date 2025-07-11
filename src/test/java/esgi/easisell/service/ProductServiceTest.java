package esgi.easisell.service;

import esgi.easisell.dto.ProductDTO;
import esgi.easisell.dto.ProductResponseDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.repository.CategoryRepository;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ProductService productService;

    private UUID clientId;
    private UUID productId;
    private UUID categoryId;
    private Client testClient;
    private Category testCategory;
    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        // Setup Client
        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setFirstName("John");
        testClient.setUsername("john@test.com");
        testClient.setName("Supérette John");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("TEST1234");

        // Setup Category
        testCategory = new Category();
        testCategory.setCategoryId(categoryId);
        testCategory.setName("Électronique");
        testCategory.setClient(testClient);

        // Setup Product
        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("iPhone 15");
        testProduct.setDescription("Smartphone Apple");
        testProduct.setBarcode("123456789");
        testProduct.setBrand("Apple");
        testProduct.setUnitPrice(new BigDecimal("999.99"));
        testProduct.setCategory(testCategory);
        testProduct.setClient(testClient);
        testProduct.setIsSoldByWeight(false);
        testProduct.setUnitLabel("pièce");

        // Setup DTO
        testProductDTO = new ProductDTO();
        testProductDTO.setName("iPhone 15");
        testProductDTO.setDescription("Smartphone Apple");
        testProductDTO.setBarcode("123456789");
        testProductDTO.setBrand("Apple");
        testProductDTO.setUnitPrice(new BigDecimal("999.99"));
        testProductDTO.setCategoryId(categoryId.toString());
        testProductDTO.setClientId(clientId.toString());
        testProductDTO.setIsSoldByWeight(false);
        testProductDTO.setUnitLabel("pièce");
    }

    @Test
    @DisplayName("Devrait créer un produit avec succès")
    void createProduct_ShouldReturnCreatedProduct_WhenValidInput() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsByClientIdAndBarcode(clientId, "123456789")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponseDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals("Apple", result.getBrand());
        assertEquals(new BigDecimal("999.99"), result.getUnitPrice());
        assertFalse(result.getIsSoldByWeight());
        assertEquals("pièce", result.getUnitLabel());

        verify(clientRepository, times(1)).findById(clientId);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Devrait retourner null quand le client n'existe pas")
    void createProduct_ShouldReturnNull_WhenClientNotFound() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When
        ProductResponseDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNull(result);
        verify(clientRepository, times(1)).findById(clientId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Devrait créer un produit sans catégorie")
    void createProduct_ShouldCreateProduct_WhenNoCategoryProvided() {
        // Given
        testProductDTO.setCategoryId(null);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(productRepository.existsByClientIdAndBarcode(any(), any())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponseDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Devrait retourner tous les produits")
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait retourner les produits d'un client")
    void getProductsByClient_ShouldReturnClientProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByClientUserId(clientId)).thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getProductsByClient(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
        verify(productRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    @DisplayName("Devrait retourner un produit par ID")
    void getProductById_ShouldReturnProduct_WhenExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        ProductResponseDTO result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Devrait retourner null quand le produit n'existe pas")
    void getProductById_ShouldReturnNull_WhenNotExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ProductResponseDTO result = productService.getProductById(productId);

        // Then
        assertNull(result);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Devrait mettre à jour un produit avec succès")
    void updateProduct_ShouldReturnUpdatedProduct_WhenValidInput() {
        // Given
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("iPhone 15 Pro");
        updateDTO.setUnitPrice(new BigDecimal("1199.99"));
        updateDTO.setIsSoldByWeight(false);
        updateDTO.setUnitLabel("pièce");

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponseDTO result = productService.updateProduct(productId, updateDTO);

        // Then
        assertNotNull(result);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Devrait retourner null quand le produit à mettre à jour n'existe pas")
    void updateProduct_ShouldReturnNull_WhenProductNotExists() {
        // Given
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("iPhone 15 Pro");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ProductResponseDTO result = productService.updateProduct(productId, updateDTO);

        // Then
        assertNull(result);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Devrait supprimer un produit avec succès")
    void deleteProduct_ShouldReturnTrue_WhenProductExists() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(true);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertTrue(result);
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Devrait retourner false quand le produit à supprimer n'existe pas")
    void deleteProduct_ShouldReturnFalse_WhenProductNotExists() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(false);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertFalse(result);
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    @DisplayName("Devrait rechercher des produits par nom")
    void searchProductsByName_ShouldReturnMatchingProducts() {
        // Given
        String searchTerm = "iPhone";
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.searchProductsByName(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
        verify(productRepository, times(1))
                .findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);
    }

    @Test
    @DisplayName("Devrait trouver un produit par code-barres")
    void findProductByBarcode_ShouldReturnProduct_WhenExists() {
        // Given
        String barcode = "123456789";
        when(productRepository.findByClientAndBarcode(clientId, barcode)).thenReturn(testProduct);

        // When
        ProductResponseDTO result = productService.findProductByBarcode(clientId, barcode);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals(barcode, result.getBarcode());
        verify(productRepository, times(1)).findByClientAndBarcode(clientId, barcode);
    }

    @Test
    @DisplayName("Devrait retourner null quand le code-barres n'existe pas")
    void findProductByBarcode_ShouldReturnNull_WhenNotExists() {
        // Given
        String barcode = "999999999";
        when(productRepository.findByClientAndBarcode(clientId, barcode)).thenReturn(null);

        // When
        ProductResponseDTO result = productService.findProductByBarcode(clientId, barcode);

        // Then
        assertNull(result);
        verify(productRepository, times(1)).findByClientAndBarcode(clientId, barcode);
    }

    @Test
    @DisplayName("Devrait retourner les produits par catégorie")
    void getProductsByCategory_ShouldReturnCategoryProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryCategoryId(categoryId)).thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getProductsByCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
        verify(productRepository, times(1)).findByCategoryCategoryId(categoryId);
    }

    @Test
    @DisplayName("Devrait retourner les produits par marque")
    void getProductsByBrand_ShouldReturnBrandProducts() {
        // Given
        String brand = "Apple";
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByClientUserIdAndBrandIgnoreCase(clientId, brand))
                .thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getProductsByBrand(clientId, brand);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getBrand());
        verify(productRepository, times(1))
                .findByClientUserIdAndBrandIgnoreCase(clientId, brand);
    }

    @Test
    @DisplayName("Devrait retourner les produits vendus au poids")
    void getProductsByWeight_ShouldReturnWeightProducts() {
        // Given
        Product weightProduct = new Product();
        weightProduct.setProductId(UUID.randomUUID());
        weightProduct.setName("Pommes");
        weightProduct.setIsSoldByWeight(true);
        weightProduct.setUnitLabel("kg");
        weightProduct.setClient(testClient);

        List<Product> products = Arrays.asList(weightProduct);
        when(productRepository.findByClientUserId(clientId)).thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getProductsByWeight(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pommes", result.get(0).getName());
        assertTrue(result.get(0).getIsSoldByWeight());
        verify(productRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    @DisplayName("Devrait retourner les produits vendus à la pièce")
    void getProductsByPiece_ShouldReturnPieceProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByClientUserId(clientId)).thenReturn(products);

        // When
        List<ProductResponseDTO> result = productService.getProductsByPiece(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
        assertFalse(result.get(0).getIsSoldByWeight());
        verify(productRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    @DisplayName("Devrait compter les produits d'un client")
    void countProductsByClient_ShouldReturnCorrectCount() {
        // Given
        long expectedCount = 5L;
        when(productRepository.countByClientUserId(clientId)).thenReturn(expectedCount);

        // When
        long result = productService.countProductsByClient(clientId);

        // Then
        assertEquals(expectedCount, result);
        verify(productRepository, times(1)).countByClientUserId(clientId);
    }

    @Test
    @DisplayName("Devrait gérer les produits avec des UUID invalides")
    void createProduct_ShouldReturnNull_WhenInvalidUUID() {
        // Given
        testProductDTO.setClientId("invalid-uuid");

        // When
        ProductResponseDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNull(result);
        verify(clientRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Devrait gérer les DTO avec des champs null")
    void createProduct_ShouldReturnNull_WhenDTOHasNullFields() {
        // Given
        testProductDTO.setName(null);

        // When
        ProductResponseDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNull(result);
        verify(productRepository, never()).save(any(Product.class));
    }
}