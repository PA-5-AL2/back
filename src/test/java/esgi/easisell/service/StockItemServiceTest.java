package esgi.easisell.service;

import esgi.easisell.dto.CreateStockItemDTO;
import esgi.easisell.dto.UpdateStockItemDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.StockItem;
import esgi.easisell.entity.Supplier;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.ProductRepository;
import esgi.easisell.repository.StockItemRepository;
import esgi.easisell.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour StockItemService")
class StockItemServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private StockItemService stockItemService;

    private UUID clientId;
    private UUID productId;
    private UUID stockItemId;
    private UUID supplierId;
    private Client testClient;
    private Product testProduct;
    private StockItem testStockItem;
    private Supplier testSupplier;
    private CreateStockItemDTO createStockItemDTO;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();
        stockItemId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        // Setup Client
        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setFirstName("John");
        testClient.setUsername("john@test.com");
        testClient.setName("Supérette John");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("TEST1234");

        // Setup Product
        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("iPhone 15");
        testProduct.setBarcode("123456789");
        testProduct.setUnitPrice(new BigDecimal("999.99"));
        testProduct.setClient(testClient);

        // Setup Supplier
        testSupplier = new Supplier();
        testSupplier.setSupplierId(supplierId);
        testSupplier.setName("TechSupply");
        testSupplier.setFirstName("Jean");
        testSupplier.setContactInfo("contact@techsupply.com");
        testSupplier.setClient(testClient);

        // Setup StockItem
        testStockItem = new StockItem();
        testStockItem.setStockItemId(stockItemId);
        testStockItem.setProduct(testProduct);
        testStockItem.setClient(testClient);
        testStockItem.setQuantity(50);
        testStockItem.setReorderThreshold(10);
        testStockItem.setPurchasePrice(new BigDecimal("800.00"));
        testStockItem.setPurchaseDate(Timestamp.valueOf(LocalDateTime.now()));
        testStockItem.setSupplier(testSupplier);

        // Setup DTO
        createStockItemDTO = new CreateStockItemDTO();
        createStockItemDTO.setProductId(productId.toString());
        createStockItemDTO.setClientId(clientId.toString());
        createStockItemDTO.setQuantity(50);
        createStockItemDTO.setReorderThreshold(10);
        createStockItemDTO.setPurchasePrice(new BigDecimal("800.00"));
        createStockItemDTO.setPurchaseDate(LocalDateTime.now());
        createStockItemDTO.setSupplierId(supplierId.toString());
    }

    @Test
    @DisplayName("Devrait créer un item de stock avec succès")
    void createStockItem_ShouldReturnCreatedStockItem_WhenValidInput() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(testSupplier));
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        StockItem result = stockItemService.createStockItem(createStockItemDTO);

        // Then
        assertNotNull(result);
        assertEquals(50, result.getQuantity());
        assertEquals(10, result.getReorderThreshold());
        assertEquals(new BigDecimal("800.00"), result.getPurchasePrice());
        assertEquals(testProduct, result.getProduct());
        assertEquals(testClient, result.getClient());
        assertEquals(testSupplier, result.getSupplier());

        verify(productRepository, times(1)).findById(productId);
        verify(clientRepository, times(1)).findById(clientId);
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(stockItemRepository, times(1)).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait lever une exception quand le produit n'existe pas")
    void createStockItem_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockItemService.createStockItem(createStockItemDTO));

        assertTrue(exception.getMessage().contains("Produit non trouvé"));
        verify(productRepository, times(1)).findById(productId);
        verify(stockItemRepository, never()).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait lever une exception quand le client n'existe pas")
    void createStockItem_ShouldThrowException_WhenClientNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockItemService.createStockItem(createStockItemDTO));

        assertTrue(exception.getMessage().contains("Client non trouvé"));
        verify(clientRepository, times(1)).findById(clientId);
        verify(stockItemRepository, never()).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait créer un item de stock sans fournisseur")
    void createStockItem_ShouldCreateStockItem_WhenNoSupplierProvided() {
        // Given
        createStockItemDTO.setSupplierId(null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        StockItem result = stockItemService.createStockItem(createStockItemDTO);

        // Then
        assertNotNull(result);
        verify(supplierRepository, never()).findById(any());
        verify(stockItemRepository, times(1)).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait retourner les items de stock d'un client")
    void getStockItemsByClient_ShouldReturnClientStockItems() {
        // Given
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByClientUserId(clientId)).thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemService.getStockItemsByClient(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(stockItemRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    @DisplayName("Devrait retourner un item de stock par ID")
    void getStockItemById_ShouldReturnStockItem_WhenExists() {
        // Given
        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(testStockItem));

        // When
        Optional<StockItem> result = stockItemService.getStockItemById(stockItemId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testStockItem, result.get());
        verify(stockItemRepository, times(1)).findById(stockItemId);
    }

    @Test
    @DisplayName("Devrait retourner empty quand l'item de stock n'existe pas")
    void getStockItemById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.empty());

        // When
        Optional<StockItem> result = stockItemService.getStockItemById(stockItemId);

        // Then
        assertFalse(result.isPresent());
        verify(stockItemRepository, times(1)).findById(stockItemId);
    }

    @Test
    @DisplayName("Devrait retourner les items de stock par produit")
    void getStockItemsByProduct_ShouldReturnProductStockItems() {
        // Given
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemService.getStockItemsByProduct(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
    }

    @Test
    @DisplayName("Devrait mettre à jour un item de stock avec succès")
    void updateStockItem_ShouldReturnUpdatedStockItem_WhenValidInput() {
        // Given
        UpdateStockItemDTO updateDTO = new UpdateStockItemDTO();
        updateDTO.setQuantity(75);
        updateDTO.setReorderThreshold(15);
        updateDTO.setPurchasePrice(new BigDecimal("850.00"));

        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(testStockItem));
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        Optional<StockItem> result = stockItemService.updateStockItem(stockItemId, updateDTO);

        // Then
        assertTrue(result.isPresent());
        verify(stockItemRepository, times(1)).findById(stockItemId);
        verify(stockItemRepository, times(1)).save(testStockItem);
    }

    @Test
    @DisplayName("Devrait retourner empty quand l'item de stock à mettre à jour n'existe pas")
    void updateStockItem_ShouldReturnEmpty_WhenStockItemNotExists() {
        // Given
        UpdateStockItemDTO updateDTO = new UpdateStockItemDTO();
        updateDTO.setQuantity(75);

        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.empty());

        // When
        Optional<StockItem> result = stockItemService.updateStockItem(stockItemId, updateDTO);

        // Then
        assertFalse(result.isPresent());
        verify(stockItemRepository, times(1)).findById(stockItemId);
        verify(stockItemRepository, never()).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait ajuster la quantité de stock avec succès")
    void adjustStockQuantity_ShouldReturnTrue_WhenStockAdjusted() {
        // Given
        int quantityChange = -5; // Diminution
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        boolean result = stockItemService.adjustStockQuantity(productId, quantityChange);

        // Then
        assertTrue(result);
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
        verify(stockItemRepository, times(1)).save(testStockItem);
    }

    @Test
    @DisplayName("Devrait augmenter le stock avec succès")
    void adjustStockQuantity_ShouldIncreaseStock_WhenPositiveQuantityChange() {
        // Given
        int quantityChange = 25; // Augmentation
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(testStockItem);

        // When
        boolean result = stockItemService.adjustStockQuantity(productId, quantityChange);

        // Then
        assertTrue(result);
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
        verify(stockItemRepository, times(1)).save(testStockItem);
    }

    @Test
    @DisplayName("Devrait supprimer un item de stock avec succès")
    void deleteStockItem_ShouldReturnTrue_WhenStockItemExists() {
        // Given
        when(stockItemRepository.existsById(stockItemId)).thenReturn(true);

        // When
        boolean result = stockItemService.deleteStockItem(stockItemId);

        // Then
        assertTrue(result);
        verify(stockItemRepository, times(1)).existsById(stockItemId);
        verify(stockItemRepository, times(1)).deleteById(stockItemId);
    }

    @Test
    @DisplayName("Devrait retourner false quand l'item de stock à supprimer n'existe pas")
    void deleteStockItem_ShouldReturnFalse_WhenStockItemNotExists() {
        // Given
        when(stockItemRepository.existsById(stockItemId)).thenReturn(false);

        // When
        boolean result = stockItemService.deleteStockItem(stockItemId);

        // Then
        assertFalse(result);
        verify(stockItemRepository, times(1)).existsById(stockItemId);
        verify(stockItemRepository, never()).deleteById(stockItemId);
    }

    @Test
    @DisplayName("Devrait retourner les items de stock avec stock faible")
    void getLowStockItems_ShouldReturnLowStockItems() {
        // Given
        List<StockItem> lowStockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findLowStockItems(clientId)).thenReturn(lowStockItems);

        // When
        List<StockItem> result = stockItemService.getLowStockItems(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(stockItemRepository, times(1)).findLowStockItems(clientId);
    }

    @Test
    @DisplayName("Devrait retourner les items qui arrivent à expiration")
    void getExpiringItems_ShouldReturnExpiringItems() {
        // Given
        int daysUntilExpiration = 7;
        List<StockItem> expiringItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findExpiringItems(eq(clientId), any(Timestamp.class)))
                .thenReturn(expiringItems);

        // When
        List<StockItem> result = stockItemService.getExpiringItems(clientId, daysUntilExpiration);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(stockItemRepository, times(1))
                .findExpiringItems(eq(clientId), any(Timestamp.class));
    }

    @Test
    @DisplayName("Devrait retourner la quantité totale de stock par produit")
    void getTotalStockQuantityByProduct_ShouldReturnTotalQuantity() {
        // Given
        int expectedTotal = 150;
        when(stockItemRepository.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(expectedTotal);

        // When
        int result = stockItemService.getTotalStockQuantityByProduct(clientId, productId);

        // Then
        assertEquals(expectedTotal, result);
        verify(stockItemRepository, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    @DisplayName("Devrait rechercher les items de stock par nom de produit")
    void searchStockItemsByProductName_ShouldReturnMatchingItems() {
        // Given
        String productName = "iPhone";
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(stockItemRepository.findByClientUserIdAndProductNameContainingIgnoreCase(clientId, productName))
                .thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemService.searchStockItemsByProductName(clientId, productName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(stockItemRepository, times(1))
                .findByClientUserIdAndProductNameContainingIgnoreCase(clientId, productName);
    }

    @Test
    @DisplayName("Devrait trouver le stock par code-barres du produit")
    void findStockByProductBarcode_ShouldReturnStockItems_WhenProductExists() {
        // Given
        String barcode = "123456789";
        List<StockItem> stockItems = Arrays.asList(testStockItem);
        when(productRepository.findByClientAndBarcode(clientId, barcode)).thenReturn(testProduct);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);

        // When
        List<StockItem> result = stockItemService.findStockByProductBarcode(clientId, barcode);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStockItem, result.get(0));
        verify(productRepository, times(1)).findByClientAndBarcode(clientId, barcode);
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide quand le produit avec code-barres n'existe pas")
    void findStockByProductBarcode_ShouldReturnEmptyList_WhenProductNotExists() {
        // Given
        String barcode = "999999999";
        when(productRepository.findByClientAndBarcode(clientId, barcode)).thenReturn(null);

        // When
        List<StockItem> result = stockItemService.findStockByProductBarcode(clientId, barcode);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByClientAndBarcode(clientId, barcode);
        verify(stockItemRepository, never()).findByProductProductId(any());
    }

    @Test
    @DisplayName("Devrait gérer les cas où aucun stock n'existe pour un produit")
    void adjustStockQuantity_ShouldReturnFalse_WhenNoStockExists() {
        // Given
        int quantityChange = -5;
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(Arrays.asList());

        // When
        boolean result = stockItemService.adjustStockQuantity(productId, quantityChange);

        // Then
        assertFalse(result);
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
        verify(stockItemRepository, never()).save(any(StockItem.class));
    }

    @Test
    @DisplayName("Devrait gérer la diminution de stock sur plusieurs items")
    void adjustStockQuantity_ShouldAdjustMultipleItems_WhenDecreasingStock() {
        // Given
        int quantityChange = -70; // Plus que ce qu'un seul item peut fournir

        StockItem item1 = new StockItem();
        item1.setQuantity(50);
        item1.setProduct(testProduct);

        StockItem item2 = new StockItem();
        item2.setQuantity(30);
        item2.setProduct(testProduct);

        List<StockItem> stockItems = Arrays.asList(item1, item2);
        when(stockItemRepository.findByProductProductId(productId)).thenReturn(stockItems);
        when(stockItemRepository.save(any(StockItem.class))).thenReturn(new StockItem());

        // When
        boolean result = stockItemService.adjustStockQuantity(productId, quantityChange);

        // Then
        assertTrue(result);
        verify(stockItemRepository, times(1)).findByProductProductId(productId);
        // Devrait sauvegarder les deux items modifiés
        verify(stockItemRepository, times(2)).save(any(StockItem.class));
    }
}