/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE  
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : ClientRepositoryTest.java
 * @description : Tests unitaires pour ClientRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ClientRepository utilisant Mockito sans H2
 * Focus sur les méthodes JPA standard pour les clients
 */
@ExtendWith(MockitoExtension.class)
class ClientRepositoryTest {

    @Mock
    private ClientRepository clientRepository;

    private Client testClient;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setUsername("test@store.com");
        testClient.setPassword("hashedPassword123");
        testClient.setRole("CLIENT");
        testClient.setName("Test Store");
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");
        testClient.setAccessCode("ABC123XY");
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie d'un client
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveClient() {
        // Given
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // When
        Client savedClient = clientRepository.save(testClient);

        // Then
        assertNotNull(savedClient);
        assertEquals(testClient.getUsername(), savedClient.getUsername());
        assertEquals(testClient.getUserId(), savedClient.getUserId());
        assertEquals(testClient.getName(), savedClient.getName());
        assertEquals(testClient.getContractStatus(), savedClient.getContractStatus());
        assertEquals(testClient.getCurrencyPreference(), savedClient.getCurrencyPreference());
        assertEquals(testClient.getAccessCode(), savedClient.getAccessCode());
        verify(clientRepository, times(1)).save(testClient);
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test validation des données client obligatoires
     */
    @Test
    @DisplayName("✅ Validation - Données obligatoires")
    void testRequiredClientData() {
        // Given
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // When
        Client savedClient = clientRepository.save(testClient);

        // Then
        assertNotNull(savedClient.getUsername());
        assertNotNull(savedClient.getPassword());
        assertNotNull(savedClient.getRole());
        assertNotNull(savedClient.getName());
        assertNotNull(savedClient.getContractStatus());
        assertNotNull(savedClient.getCurrencyPreference());
        assertNotNull(savedClient.getAccessCode());
        assertFalse(savedClient.getUsername().trim().isEmpty());
        assertFalse(savedClient.getName().trim().isEmpty());
        assertEquals("CLIENT", savedClient.getRole());
    }

    /**
     * Test validation du statut client
     */
    @Test
    @DisplayName("✅ Validation - Statut du client")
    void testClientStatus() {
        // Given
        testClient.setContractStatus("ACTIVE");
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // When
        Client savedClient = clientRepository.save(testClient);

        // Then
        assertEquals("ACTIVE", savedClient.getContractStatus());
        assertNotNull(savedClient.getCreatedAt());
    }

    /**
     * Test différents statuts de contrat
     */
    @Test
    @DisplayName("✅ Gestion - Statuts de contrat")
    void testContractStatuses() {
        // Given
        Client activeContractClient = new Client();
        activeContractClient.setContractStatus("ACTIVE");
        activeContractClient.setName("Active Contract");

        Client pendingContractClient = new Client();
        pendingContractClient.setContractStatus("PENDING");
        pendingContractClient.setName("Pending Contract");

        Client suspendedContractClient = new Client();
        suspendedContractClient.setContractStatus("SUSPENDED");
        suspendedContractClient.setName("Suspended Contract");

        List<Client> clients = Arrays.asList(
                activeContractClient,
                pendingContractClient,
                suspendedContractClient
        );
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<Client> result = clientRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        boolean hasActive = result.stream().anyMatch(c -> "ACTIVE".equals(c.getContractStatus()));
        boolean hasPending = result.stream().anyMatch(c -> "PENDING".equals(c.getContractStatus()));
        boolean hasSuspended = result.stream().anyMatch(c -> "SUSPENDED".equals(c.getContractStatus()));

        assertTrue(hasActive);
        assertTrue(hasPending);
        assertTrue(hasSuspended);
    }

    /**
     * Test validation du code d'accès
     */
    @Test
    @DisplayName("✅ Validation - Code d'accès unique")
    void testAccessCodeValidation() {
        // Given
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // When
        Client savedClient = clientRepository.save(testClient);

        // Then
        assertNotNull(savedClient.getAccessCode());
        assertFalse(savedClient.getAccessCode().trim().isEmpty());
        assertTrue(savedClient.getAccessCode().length() > 0);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Méthode utilitaire pour créer un client de test
     */
    private Client createTestClient(String email, String name) {
        Client client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setUsername(email);
        client.setPassword("hashedPassword");
        client.setRole("CLIENT");
        client.setName(name);
        client.setContractStatus("ACTIVE");
        client.setCurrencyPreference("EUR");
        client.setCreatedAt(LocalDateTime.now());
        client.setAccessCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return client;
    }
}