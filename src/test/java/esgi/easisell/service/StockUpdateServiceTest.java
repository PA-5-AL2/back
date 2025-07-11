package esgi.easisell.service;

import esgi.easisell.entity.Product;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.SaleItem;
import esgi.easisell.exception.StockUpdateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour StockUpdateService")
class StockUpdateServiceTest {

    @Mock
    private StockItemService stockItemService;

    @InjectMocks
    private StockUpdateServiceImpl stockUpdateService;

    private Sale testSale;
    private Product testProduct1;
    private Product testProduct2;
    private SaleItem testSaleItem1;
    private SaleItem testSaleItem2;

    @BeforeEach
    void setUp() {
        // Setup Products
        testProduct1 = new Product();
        testProduct1.setProductId(UUID.randomUUID());
        testProduct1.setName("iPhone 15");

        testProduct2 = new Product();
        testProduct2.setProductId(UUID.randomUUID());
        testProduct2.setName("MacBook Pro");

        // Setup SaleItems
        testSaleItem1 = new SaleItem();
        testSaleItem1.setProduct(testProduct1);
        testSaleItem1.setQuantitySold(BigDecimal.valueOf(2));
        testSaleItem1.setPriceAtSale(new BigDecimal("999.99"));

        testSaleItem2 = new SaleItem();
        testSaleItem2.setProduct(testProduct2);
        testSaleItem2.setQuantitySold(BigDecimal.valueOf(1));
        testSaleItem2.setPriceAtSale(new BigDecimal("2499.99"));

        // Setup Sale
        testSale = new Sale();
        testSale.setSaleId(UUID.randomUUID());
        testSale.setSaleItems(Arrays.asList(testSaleItem1, testSaleItem2));
    }

    @Test
    @DisplayName("Devrait diminuer le stock pour tous les items de vente avec succès")
    void decreaseStockForSale_ShouldDecreaseStock_WhenAllItemsSucceed() {
        // Given
        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), -2))
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> stockUpdateService.decreaseStockForSale(testSale));

        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), -2);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait lever une exception quand la mise à jour du stock échoue")
    void decreaseStockForSale_ShouldThrowException_WhenStockUpdateFails() {
        // Given
        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), -2))
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(false); // Échec pour le deuxième produit

        // When & Then
        StockUpdateException exception = assertThrows(
                StockUpdateException.class,
                () -> stockUpdateService.decreaseStockForSale(testSale)
        );

        assertTrue(exception.getMessage().contains("Erreur lors de la mise à jour du stock"));
        assertTrue(exception.getMessage().contains("MacBook Pro"));

        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), -2);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait lever une exception dès le premier échec")
    void decreaseStockForSale_ShouldThrowException_OnFirstFailure() {
        // Given
        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), -2))
                .thenReturn(false); // Échec pour le premier produit

        // When & Then
        StockUpdateException exception = assertThrows(
                StockUpdateException.class,
                () -> stockUpdateService.decreaseStockForSale(testSale)
        );

        assertTrue(exception.getMessage().contains("iPhone 15"));

        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), -2);
        // Ne devrait pas essayer le deuxième produit
        verify(stockItemService, never())
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait gérer une vente sans items")
    void decreaseStockForSale_ShouldHandleEmptySale_WhenNoItems() {
        // Given
        testSale.setSaleItems(Arrays.asList());

        // When & Then
        assertDoesNotThrow(() -> stockUpdateService.decreaseStockForSale(testSale));

        verify(stockItemService, never()).adjustStockQuantity(any(), anyInt());
    }

    @Test
    @DisplayName("Devrait gérer les quantités décimales correctement")
    void decreaseStockForSale_ShouldHandleDecimalQuantities() {
        // Given
        testSaleItem1.setQuantitySold(BigDecimal.valueOf(2.5)); // Quantité décimale

        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), -2))
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> stockUpdateService.decreaseStockForSale(testSale));

        // Devrait convertir 2.5 en 2 (intValue)
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), -2);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait gérer les quantités nulles")
    void decreaseStockForSale_ShouldHandleNullQuantities() {
        // Given
        testSaleItem1.setQuantitySold(null);

        // When & Then
        assertThrows(NullPointerException.class,
                () -> stockUpdateService.decreaseStockForSale(testSale));
    }

    @Test
    @DisplayName("Devrait gérer les produits null")
    void decreaseStockForSale_ShouldHandleNullProducts() {
        // Given
        testSaleItem1.setProduct(null);

        // When & Then
        assertThrows(NullPointerException.class,
                () -> stockUpdateService.decreaseStockForSale(testSale));
    }

    @Test
    @DisplayName("Devrait traiter tous les items même si certains ont une quantité zéro")
    void decreaseStockForSale_ShouldProcessAllItems_EvenWithZeroQuantity() {
        // Given
        testSaleItem1.setQuantitySold(BigDecimal.ZERO); // Quantité zéro

        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), 0))
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> stockUpdateService.decreaseStockForSale(testSale));

        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), 0);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait gérer les quantités négatives dans les SaleItems")
    void decreaseStockForSale_ShouldHandleNegativeQuantities() {
        // Given
        testSaleItem1.setQuantitySold(BigDecimal.valueOf(-1)); // Quantité négative (cas bizarre)

        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), 1)) // Devient positif
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> stockUpdateService.decreaseStockForSale(testSale));

        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), 1);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }

    @Test
    @DisplayName("Devrait maintenir la transaction et échouer complètement en cas d'erreur")
    void decreaseStockForSale_ShouldMaintainTransaction_OnFailure() {
        // Given
        when(stockItemService.adjustStockQuantity(testProduct1.getProductId(), -2))
                .thenReturn(true);
        when(stockItemService.adjustStockQuantity(testProduct2.getProductId(), -1))
                .thenReturn(false);

        // When & Then
        assertThrows(StockUpdateException.class,
                () -> stockUpdateService.decreaseStockForSale(testSale));

        // Vérifier que les deux appels ont été tentés
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct1.getProductId(), -2);
        verify(stockItemService, times(1))
                .adjustStockQuantity(testProduct2.getProductId(), -1);
    }
}