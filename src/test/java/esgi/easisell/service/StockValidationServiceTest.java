package esgi.easisell.service;

import esgi.easisell.entity.Product;
import esgi.easisell.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockValidationServiceTest {

    @Mock
    private StockItemService stockItemService;

    @InjectMocks
    private StockValidationServiceImpl stockValidationService;

    private UUID clientId;
    private UUID productId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testProduct = new Product();
        testProduct.setProductId(productId);
        testProduct.setName("iPhone 15");
        testProduct.setDescription("Smartphone Apple");
        testProduct.setBarcode("123456789");
        testProduct.setBrand("Apple");
        testProduct.setUnitPrice(new BigDecimal("999.99"));
    }

    @Test
    void validateStockAvailable_ShouldNotThrow_WhenStockIsSufficient() {
        // Given
        int availableStock = 10;
        int requestedQuantity = 5;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        assertDoesNotThrow(() ->
                stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldNotThrow_WhenStockIsExactlyRequested() {
        // Given
        int availableStock = 5;
        int requestedQuantity = 5;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        assertDoesNotThrow(() ->
                stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldThrowException_WhenStockIsInsufficient() {
        // Given
        int availableStock = 3;
        int requestedQuantity = 5;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity)
        );

        String expectedMessage = String.format(
                "Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                testProduct.getName(), availableStock, requestedQuantity
        );
        assertEquals(expectedMessage, exception.getMessage());

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldThrowException_WhenNoStockAvailable() {
        // Given
        int availableStock = 0;
        int requestedQuantity = 1;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity)
        );

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
        assertTrue(exception.getMessage().contains("iPhone 15"));
        assertTrue(exception.getMessage().contains("Disponible: 0"));
        assertTrue(exception.getMessage().contains("Demandé: 1"));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldHandleZeroRequestedQuantity() {
        // Given
        int availableStock = 10;
        int requestedQuantity = 0;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        assertDoesNotThrow(() ->
                stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldThrowException_WhenNegativeStockReturned() {
        // Given
        int availableStock = -1; // Cas d'erreur dans le système
        int requestedQuantity = 1;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity)
        );

        assertTrue(exception.getMessage().contains("Disponible: -1"));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldHandleLargeQuantities() {
        // Given
        int availableStock = 1000000;
        int requestedQuantity = 999999;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When & Then
        assertDoesNotThrow(() ->
                stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity));

        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(clientId, productId);
    }

    @Test
    void validateStockAvailable_ShouldCallStockItemServiceWithCorrectParameters() {
        // Given
        int availableStock = 5;
        int requestedQuantity = 3;

        when(stockItemService.getTotalStockQuantityByProduct(clientId, productId))
                .thenReturn(availableStock);

        // When
        stockValidationService.validateStockAvailable(testProduct, clientId, requestedQuantity);

        // Then
        verify(stockItemService, times(1))
                .getTotalStockQuantityByProduct(eq(clientId), eq(productId));
    }
}