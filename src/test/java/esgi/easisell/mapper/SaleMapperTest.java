/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SaleMapperTest.java
 * @description : Tests unitaires pour le mapper SaleMapper
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 11/07/2025
 * @package     : esgi.easisell.mapper
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.mapper;

import esgi.easisell.dto.SaleResponseDTO;
import esgi.easisell.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SaleMapper
 *
 * Teste la méthode :
 * - toResponseDTO() - Mapping avec protection contre les valeurs null
 */
class SaleMapperTest {

    private Sale sale;
    private Client client;
    private List<SaleItem> saleItems;
    private List<Payment> payments;

    @BeforeEach
    void setUp() {
        // Créer le client
        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setUsername("boutique@easisell.com");
        client.setName("Ma Boutique");

        // Créer la vente
        sale = new Sale();
        sale.setSaleId(UUID.randomUUID());
        sale.setClient(client);
        sale.setSaleTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        sale.setTotalAmount(new BigDecimal("45.75"));
        sale.setIsDeferred(false);

        // Créer les articles de vente
        saleItems = new ArrayList<>();
        SaleItem item1 = new SaleItem();
        item1.setSaleItemId(UUID.randomUUID());
        saleItems.add(item1);
        SaleItem item2 = new SaleItem();
        item2.setSaleItemId(UUID.randomUUID());
        saleItems.add(item2);
        sale.setSaleItems(saleItems);

        // Créer les paiements
        payments = new ArrayList<>();
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payments.add(payment);
        sale.setPayments(payments);
    }

    // ==================== TESTS toResponseDTO() ====================

    /**
     * Test toResponseDTO() avec vente complète
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente complète avec client, articles et paiements")
    void testToResponseDTOComplete() {
        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertNotNull(result);
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(sale.getSaleTimestamp(), result.getSaleTimestamp());
        assertEquals(sale.getTotalAmount(), result.getTotalAmount());
        assertEquals(sale.getIsDeferred(), result.getIsDeferred());

        // Informations client
        assertEquals(client.getUserId(), result.getClientId());
        assertEquals(client.getName(), result.getClientName());
        assertEquals(client.getUsername(), result.getClientUsername());

        // Compteurs
        assertEquals(2, result.getItemCount());
        assertEquals(true, result.getIsPaid());
    }

    /**
     * Test toResponseDTO() avec vente différée
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente différée")
    void testToResponseDTODeferred() {
        // Given
        sale.setIsDeferred(true);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(true, result.getIsDeferred());
    }

    /**
     * Test toResponseDTO() avec vente non payée
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente non payée")
    void testToResponseDTOUnpaid() {
        // Given
        sale.setPayments(new ArrayList<>());

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(false, result.getIsPaid());
    }

    /**
     * Test toResponseDTO() avec sale null
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Gestion de sale null")
    void testToResponseDTOWithNullSale() {
        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(null);

        // Then
        assertNull(result);
    }

    /**
     * Test toResponseDTO() avec client null
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Protection contre client null")
    void testToResponseDTOWithNullClient() {
        // Given
        sale.setClient(null);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertNotNull(result);
        assertEquals(sale.getSaleId(), result.getSaleId());
        assertEquals(sale.getTotalAmount(), result.getTotalAmount());

        // Les champs client doivent être null
        assertNull(result.getClientId());
        assertNull(result.getClientName());
        assertNull(result.getClientUsername());

        // Les autres champs doivent fonctionner
        assertEquals(2, result.getItemCount());
        assertEquals(true, result.getIsPaid());
    }

    /**
     * Test toResponseDTO() avec saleItems null
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Protection contre saleItems null")
    void testToResponseDTOWithNullSaleItems() {
        // Given
        sale.setSaleItems(null);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getItemCount());
        assertEquals(true, result.getIsPaid()); // Toujours payé car payments non null
    }

    /**
     * Test toResponseDTO() avec payments null
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Protection contre payments null")
    void testToResponseDTOWithNullPayments() {
        // Given
        sale.setPayments(null);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertNotNull(result);
        assertEquals(false, result.getIsPaid());
        assertEquals(2, result.getItemCount());
    }

    /**
     * Test toResponseDTO() avec listes vides
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Listes vides")
    void testToResponseDTOWithEmptyLists() {
        // Given
        sale.setSaleItems(new ArrayList<>());
        sale.setPayments(new ArrayList<>());

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(0, result.getItemCount());
        assertEquals(false, result.getIsPaid());
    }

    /**
     * Test toResponseDTO() avec beaucoup d'articles
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente avec beaucoup d'articles")
    void testToResponseDTOWithManyItems() {
        // Given
        List<SaleItem> manyItems = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            SaleItem item = new SaleItem();
            item.setSaleItemId(UUID.randomUUID());
            manyItems.add(item);
        }
        sale.setSaleItems(manyItems);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(50, result.getItemCount());
    }

    /**
     * Test toResponseDTO() avec montants complexes
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Montants avec décimales")
    void testToResponseDTOComplexAmounts() {
        // Given
        sale.setTotalAmount(new BigDecimal("999.999"));

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(new BigDecimal("999.999"), result.getTotalAmount());
    }

    /**
     * Test toResponseDTO() avec timestamp précis
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Timestamp précis")
    void testToResponseDTOTimestamp() {
        // Given
        Timestamp specificTime = Timestamp.valueOf(LocalDateTime.of(2025, 7, 11, 14, 30, 45));
        sale.setSaleTimestamp(specificTime);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(specificTime, result.getSaleTimestamp());
    }

    /**
     * Test toResponseDTO() avec client ayant un nom long
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Client avec nom long")
    void testToResponseDTOLongClientName() {
        // Given
        client.setName("Supermarché de la Place du Marché Central de la Ville");
        client.setUsername("supermarche.place.marche.central@very-long-domain-name.com");

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals("Supermarché de la Place du Marché Central de la Ville", result.getClientName());
        assertEquals("supermarche.place.marche.central@very-long-domain-name.com", result.getClientUsername());
    }

    /**
     * Test toResponseDTO() avec paiements multiples
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente avec paiements multiples")
    void testToResponseDTOMultiplePayments() {
        // Given
        List<Payment> multiplePayments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID());
            multiplePayments.add(payment);
        }
        sale.setPayments(multiplePayments);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(true, result.getIsPaid());
    }

    /**
     * Test toResponseDTO() avec montant zéro
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Vente avec montant zéro")
    void testToResponseDTOZeroAmount() {
        // Given
        sale.setTotalAmount(BigDecimal.ZERO);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertNotNull(result);
    }

    /**
     * Test toResponseDTO() avec identifiants spéciaux
     */
    @Test
    @DisplayName("✅ toResponseDTO() - Identifiants UUID valides")
    void testToResponseDTOValidUUIDs() {
        // Given
        UUID saleId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        sale.setSaleId(saleId);
        client.setUserId(clientId);

        // When
        SaleResponseDTO result = SaleMapper.toResponseDTO(sale);

        // Then
        assertEquals(saleId, result.getSaleId());
        assertEquals(clientId, result.getClientId());
    }
}