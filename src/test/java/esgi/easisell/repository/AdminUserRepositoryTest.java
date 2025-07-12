/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE  
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : AdminUserRepositoryTest.java
 * @description : Tests unitaires pour AdminUserRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AdminUserRepository utilisant Mockito sans H2
 * Focus sur les méthodes personnalisées et JPA standard
 */
@ExtendWith(MockitoExtension.class)
class AdminUserRepositoryTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    private AdminUser testAdminUser;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();

        testAdminUser = new AdminUser();
        testAdminUser.setUserId(adminId);
        testAdminUser.setUsername("admin@easisell.com");
        testAdminUser.setPassword("hashedAdminPassword");
        testAdminUser.setRole("ADMIN");
        testAdminUser.setFirstName("John");
        testAdminUser.setCreatedAt(LocalDateTime.now());
        // Pas de champ isActive - supprimé
        // Pas de champ permissionLevel - supprimé car pas dans l'entité
    }

    /**
     * Test findByUsername - administrateur trouvé
     */
    @Test
    @DisplayName("✅ findByUsername() - Administrateur trouvé")
    void testFindByUsernameFound() {
        // Given
        String username = "admin@easisell.com";
        when(adminUserRepository.findByUsername(username)).thenReturn(testAdminUser);

        // When
        AdminUser result = adminUserRepository.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(testAdminUser.getUserId(), result.getUserId());
        assertEquals("ADMIN", result.getRole());
        verify(adminUserRepository, times(1)).findByUsername(username);
    }

    /**
     * Test validation des données administrateur obligatoires
     */
    @Test
    @DisplayName("✅ Validation - Données obligatoires")
    void testRequiredAdminData() {
        // Given
        when(adminUserRepository.save(any(AdminUser.class))).thenReturn(testAdminUser);

        // When
        AdminUser savedAdmin = adminUserRepository.save(testAdminUser);

        // Then
        assertNotNull(savedAdmin.getUsername());
        assertNotNull(savedAdmin.getPassword());
        assertNotNull(savedAdmin.getRole());
        assertNotNull(savedAdmin.getFirstName());
        assertFalse(savedAdmin.getUsername().trim().isEmpty());
        assertFalse(savedAdmin.getFirstName().trim().isEmpty());
        assertEquals("ADMIN", savedAdmin.getRole());
    }

    /**
     * Méthode utilitaire pour créer un administrateur de test
     */
    private AdminUser createAdminUser(String email) {
        AdminUser admin = new AdminUser();
        admin.setUserId(UUID.randomUUID());
        admin.setUsername(email);
        admin.setPassword("hashedPassword");
        admin.setRole("ADMIN");
        admin.setFirstName("Test");
        admin.setCreatedAt(LocalDateTime.now());
        return admin;
    }
}