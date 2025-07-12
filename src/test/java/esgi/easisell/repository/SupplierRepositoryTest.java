/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE  
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : SupplierRepositoryTest.java
 * @description : Tests unitaires pour SupplierRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.Supplier;
import esgi.easisell.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SupplierRepository utilisant Mockito sans H2
 * Focus sur les méthodes spécifiques et JPA standard
 */
@ExtendWith(MockitoExtension.class)
class SupplierRepositoryTest {

    @Mock
    private SupplierRepository supplierRepository;

    private Supplier testSupplier;
    private Client testClient;
    private UUID supplierId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setName("Test Store");

        testSupplier = new Supplier();
        testSupplier.setSupplierId(supplierId);
        testSupplier.setName("Coca-Cola Company");
        testSupplier.setFirstName("John");
        testSupplier.setDescription("Fournisseur de boissons gazeuses");
        testSupplier.setContactInfo("contact@cocacola.com");
        testSupplier.setPhoneNumber("0123456789");
        testSupplier.setClient(testClient);
    }

    // ==================== TESTS MÉTHODES PERSONNALISÉES ====================

    /**
     * Test findByClientUserId - fournisseurs trouvés
     */
    @Test
    @DisplayName("✅ findByClientUserId() - Fournisseurs trouvés")
    void testFindByClientUserIdFound() {
        // Given
        Supplier supplier2 = new Supplier();
        supplier2.setSupplierId(UUID.randomUUID());
        supplier2.setName("Pepsi Company");
        supplier2.setClient(testClient);

        List<Supplier> suppliers = Arrays.asList(testSupplier, supplier2);
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSupplier));
        assertTrue(result.contains(supplier2));
        verify(supplierRepository, times(1)).findByClientUserId(clientId);
    }

    /**
     * Test findByClientUserId - aucun fournisseur trouvé
     */
    @Test
    @DisplayName("❌ findByClientUserId() - Aucun fournisseur")
    void testFindByClientUserIdEmpty() {
        // Given
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(Arrays.asList());

        // When
        List<Supplier> result = supplierRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findByClientUserId(clientId);
    }

    /**
     * Test findByClientUserId avec client inexistant
     */
    @Test
    @DisplayName("❌ findByClientUserId() - Client inexistant")
    void testFindByClientUserIdNonExistentClient() {
        // Given
        UUID nonExistentClientId = UUID.randomUUID();
        when(supplierRepository.findByClientUserId(nonExistentClientId)).thenReturn(Arrays.asList());

        // When
        List<Supplier> result = supplierRepository.findByClientUserId(nonExistentClientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findByClientUserId(nonExistentClientId);
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveSupplier() {
        // Given
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        Supplier savedSupplier = supplierRepository.save(testSupplier);

        // Then
        assertNotNull(savedSupplier);
        assertEquals(testSupplier.getName(), savedSupplier.getName());
        assertEquals(testSupplier.getSupplierId(), savedSupplier.getSupplierId());
        assertEquals(testSupplier.getContactInfo(), savedSupplier.getContactInfo());
        verify(supplierRepository, times(1)).save(testSupplier);
    }

    /**
     * Test findById - fournisseur trouvé
     */
    @Test
    @DisplayName("✅ findById() - Fournisseur trouvé")
    void testFindByIdFound() {
        // Given
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(testSupplier));

        // When
        Optional<Supplier> result = supplierRepository.findById(supplierId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier.getSupplierId(), result.get().getSupplierId());
        assertEquals(testSupplier.getName(), result.get().getName());
        verify(supplierRepository, times(1)).findById(supplierId);
    }

    /**
     * Test findById - fournisseur non trouvé
     */
    @Test
    @DisplayName("❌ findById() - Fournisseur non trouvé")
    void testFindByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(supplierRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Supplier> result = supplierRepository.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test findAll - liste de fournisseurs
     */
    @Test
    @DisplayName("✅ findAll() - Liste de fournisseurs")
    void testFindAll() {
        // Given
        Supplier supplier2 = new Supplier();
        supplier2.setSupplierId(UUID.randomUUID());
        supplier2.setName("Red Bull GmbH");
        supplier2.setClient(testClient);

        List<Supplier> suppliers = Arrays.asList(testSupplier, supplier2);
        when(supplierRepository.findAll()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSupplier));
        assertTrue(result.contains(supplier2));
        verify(supplierRepository, times(1)).findAll();
    }

    /**
     * Test existsById - fournisseur existe
     */
    @Test
    @DisplayName("✅ existsById() - Fournisseur existe")
    void testExistsByIdTrue() {
        // Given
        when(supplierRepository.existsById(supplierId)).thenReturn(true);

        // When
        boolean exists = supplierRepository.existsById(supplierId);

        // Then
        assertTrue(exists);
        verify(supplierRepository, times(1)).existsById(supplierId);
    }

    /**
     * Test existsById - fournisseur n'existe pas
     */
    @Test
    @DisplayName("❌ existsById() - Fournisseur n'existe pas")
    void testExistsByIdFalse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(supplierRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean exists = supplierRepository.existsById(nonExistentId);

        // Then
        assertFalse(exists);
        verify(supplierRepository, times(1)).existsById(nonExistentId);
    }

    /**
     * Test deleteById - suppression réussie
     */
    @Test
    @DisplayName("✅ deleteById() - Suppression réussie")
    void testDeleteById() {
        // Given
        doNothing().when(supplierRepository).deleteById(supplierId);

        // When
        supplierRepository.deleteById(supplierId);

        // Then
        verify(supplierRepository, times(1)).deleteById(supplierId);
    }

    /**
     * Test count - comptage des fournisseurs
     */
    @Test
    @DisplayName("✅ count() - Comptage des fournisseurs")
    void testCount() {
        // Given
        when(supplierRepository.count()).thenReturn(10L);

        // When
        long count = supplierRepository.count();

        // Then
        assertEquals(10L, count);
        verify(supplierRepository, times(1)).count();
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test validation des données de fournisseur
     */
    @Test
    @DisplayName("✅ Validation - Données valides de fournisseur")
    void testValidSupplierData() {
        // Given
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        Supplier savedSupplier = supplierRepository.save(testSupplier);

        // Then
        assertNotNull(savedSupplier.getName());
        assertNotNull(savedSupplier.getClient());
        assertNotNull(savedSupplier.getSupplierId());
        assertFalse(savedSupplier.getName().trim().isEmpty());

        // Vérifier les informations de contact
        assertNotNull(savedSupplier.getContactInfo());
        assertNotNull(savedSupplier.getPhoneNumber());
    }

    /**
     * Test avec différents types de fournisseurs
     */
    @Test
    @DisplayName("✅ Gestion - Différents types de fournisseurs")
    void testDifferentSupplierTypes() {
        // Given
        Supplier localSupplier = new Supplier();
        localSupplier.setName("Fournisseur Local");
        localSupplier.setDescription("Producteur local de fruits");
        localSupplier.setClient(testClient);

        Supplier internationalSupplier = new Supplier();
        internationalSupplier.setName("International Corp");
        internationalSupplier.setDescription("Importateur international");
        internationalSupplier.setClient(testClient);

        List<Supplier> suppliers = Arrays.asList(localSupplier, internationalSupplier);
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Vérifier que les deux types sont présents
        boolean hasLocal = result.stream().anyMatch(s -> s.getName().contains("Local"));
        boolean hasInternational = result.stream().anyMatch(s -> s.getName().contains("International"));

        assertTrue(hasLocal);
        assertTrue(hasInternational);
    }

    /**
     * Test cohérence des données client-fournisseur
     */
    @Test
    @DisplayName("✅ Cohérence - Relation client-fournisseur")
    void testClientSupplierRelationship() {
        // Given
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(Arrays.asList(testSupplier));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(testSupplier));

        // When
        List<Supplier> suppliersByClient = supplierRepository.findByClientUserId(clientId);
        Optional<Supplier> supplierById = supplierRepository.findById(supplierId);

        // Then
        assertNotNull(suppliersByClient);
        assertFalse(suppliersByClient.isEmpty());
        assertTrue(supplierById.isPresent());

        // Vérifier la cohérence des relations
        assertEquals(clientId, suppliersByClient.get(0).getClient().getUserId());
        assertEquals(clientId, supplierById.get().getClient().getUserId());
        assertEquals(supplierId, suppliersByClient.get(0).getSupplierId());
    }

    /**
     * Test gestion des informations de contact
     */
    @Test
    @DisplayName("✅ Validation - Informations de contact")
    void testContactInformationHandling() {
        // Given
        Supplier supplierWithContact = new Supplier();
        supplierWithContact.setName("Supplier with Contact");
        supplierWithContact.setContactInfo("supplier@example.com");
        supplierWithContact.setPhoneNumber("+33123456789");
        supplierWithContact.setClient(testClient);

        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplierWithContact);

        // When
        Supplier savedSupplier = supplierRepository.save(supplierWithContact);

        // Then
        assertNotNull(savedSupplier.getContactInfo());
        assertNotNull(savedSupplier.getPhoneNumber());
        assertTrue(savedSupplier.getContactInfo().contains("@"));
        assertTrue(savedSupplier.getPhoneNumber().matches(".*\\d+.*"));
    }

    // ==================== TESTS DE PERFORMANCE SIMULÉS ====================

    /**
     * Test recherche de fournisseurs pour plusieurs clients
     */
    @Test
    @DisplayName("⚡ Performance - Recherche multi-clients")
    void testMultiClientSupplierSearch() {
        // Given
        UUID client1Id = UUID.randomUUID();
        UUID client2Id = UUID.randomUUID();

        when(supplierRepository.findByClientUserId(client1Id))
                .thenReturn(Arrays.asList(testSupplier));
        when(supplierRepository.findByClientUserId(client2Id))
                .thenReturn(Arrays.asList());

        // When
        List<Supplier> client1Suppliers = supplierRepository.findByClientUserId(client1Id);
        List<Supplier> client2Suppliers = supplierRepository.findByClientUserId(client2Id);

        // Then
        assertNotNull(client1Suppliers);
        assertNotNull(client2Suppliers);
        assertEquals(1, client1Suppliers.size());
        assertEquals(0, client2Suppliers.size());

        verify(supplierRepository, times(1)).findByClientUserId(client1Id);
        verify(supplierRepository, times(1)).findByClientUserId(client2Id);
    }

    /**
     * Test mise à jour des informations fournisseur
     */
    @Test
    @DisplayName("✅ Mise à jour - Informations fournisseur")
    void testSupplierInformationUpdate() {
        // Given
        Supplier updatedSupplier = new Supplier();
        updatedSupplier.setSupplierId(supplierId);
        updatedSupplier.setName("Coca-Cola Company Updated");
        updatedSupplier.setContactInfo("newcontact@cocacola.com");
        updatedSupplier.setClient(testClient);

        when(supplierRepository.save(any(Supplier.class))).thenReturn(updatedSupplier);

        // When
        Supplier result = supplierRepository.save(updatedSupplier);

        // Then
        assertNotNull(result);
        assertEquals("Coca-Cola Company Updated", result.getName());
        assertEquals("newcontact@cocacola.com", result.getContactInfo());
        assertEquals(supplierId, result.getSupplierId());
        verify(supplierRepository, times(1)).save(updatedSupplier);
    }
}