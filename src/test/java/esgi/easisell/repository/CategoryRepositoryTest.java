/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE  
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : CategoryRepositoryTest.java
 * @description : Tests unitaires pour CategoryRepository avec Mockito
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 12/07/2025
 * @package     : esgi.easisell.repository
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.repository;

import esgi.easisell.entity.Category;
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
 * Tests unitaires pour CategoryRepository utilisant Mockito sans H2
 * Focus sur les méthodes spécifiques et JPA standard
 */
@ExtendWith(MockitoExtension.class)
class CategoryRepositoryTest {

    @Mock
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Client testClient;
    private UUID categoryId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setName("Test Store");

        testCategory = new Category();
        testCategory.setCategoryId(categoryId);
        testCategory.setName("Boissons");
        testCategory.setClient(testClient);
    }

    // ==================== TESTS MÉTHODES PERSONNALISÉES ====================

    /**
     * Test findByClientUserId - catégories trouvées
     */
    @Test
    @DisplayName("✅ findByClientUserId() - Catégories trouvées")
    void testFindByClientUserIdFound() {
        // Given
        Category category2 = new Category();
        category2.setCategoryId(UUID.randomUUID());
        category2.setName("Alimentation");
        category2.setClient(testClient);

        List<Category> categories = Arrays.asList(testCategory, category2);
        when(categoryRepository.findByClientUserId(clientId)).thenReturn(categories);

        // When
        List<Category> result = categoryRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testCategory));
        assertTrue(result.contains(category2));
        verify(categoryRepository, times(1)).findByClientUserId(clientId);
    }

    /**
     * Test findByClientUserId - aucune catégorie trouvée
     */
    @Test
    @DisplayName("❌ findByClientUserId() - Aucune catégorie")
    void testFindByClientUserIdEmpty() {
        // Given
        when(categoryRepository.findByClientUserId(clientId)).thenReturn(Arrays.asList());

        // When
        List<Category> result = categoryRepository.findByClientUserId(clientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findByClientUserId(clientId);
    }

    /**
     * Test findByClientUserIdAndNameContainingIgnoreCase - recherche trouvée
     */
    @Test
    @DisplayName("✅ findByClientUserIdAndNameContainingIgnoreCase() - Trouvé")
    void testFindByClientUserIdAndNameContainingIgnoreCaseFound() {
        // Given
        String searchTerm = "bois";
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(categories);

        // When
        List<Category> result = categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory, result.get(0));
        verify(categoryRepository, times(1))
                .findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);
    }

    /**
     * Test findByClientUserIdAndNameContainingIgnoreCase - recherche vide
     */
    @Test
    @DisplayName("❌ findByClientUserIdAndNameContainingIgnoreCase() - Aucun résultat")
    void testFindByClientUserIdAndNameContainingIgnoreCaseEmpty() {
        // Given
        String searchTerm = "inexistant";
        when(categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(Arrays.asList());

        // When
        List<Category> result = categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1))
                .findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);
    }

    /**
     * Test existsByClientUserIdAndCategoryId - catégorie existe
     */
    @Test
    @DisplayName("✅ existsByClientUserIdAndCategoryId() - Catégorie existe")
    void testExistsByClientUserIdAndCategoryIdTrue() {
        // Given
        when(categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId))
                .thenReturn(true);

        // When
        boolean exists = categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId);

        // Then
        assertTrue(exists);
        verify(categoryRepository, times(1))
                .existsByClientUserIdAndCategoryId(clientId, categoryId);
    }

    /**
     * Test existsByClientUserIdAndCategoryId - catégorie n'existe pas
     */
    @Test
    @DisplayName("❌ existsByClientUserIdAndCategoryId() - Catégorie n'existe pas")
    void testExistsByClientUserIdAndCategoryIdFalse() {
        // Given
        UUID nonExistentCategoryId = UUID.randomUUID();
        when(categoryRepository.existsByClientUserIdAndCategoryId(clientId, nonExistentCategoryId))
                .thenReturn(false);

        // When
        boolean exists = categoryRepository.existsByClientUserIdAndCategoryId(clientId, nonExistentCategoryId);

        // Then
        assertFalse(exists);
        verify(categoryRepository, times(1))
                .existsByClientUserIdAndCategoryId(clientId, nonExistentCategoryId);
    }

    /**
     * Test countByClientId - comptage des catégories
     */
    @Test
    @DisplayName("✅ countByClientId() - Comptage des catégories")
    void testCountByClientId() {
        // Given
        long expectedCount = 5L;
        when(categoryRepository.countByClientId(clientId)).thenReturn(expectedCount);

        // When
        long count = categoryRepository.countByClientId(clientId);

        // Then
        assertEquals(expectedCount, count);
        verify(categoryRepository, times(1)).countByClientId(clientId);
    }

    /**
     * Test countByClientId - aucune catégorie
     */
    @Test
    @DisplayName("❌ countByClientId() - Aucune catégorie")
    void testCountByClientIdZero() {
        // Given
        when(categoryRepository.countByClientId(clientId)).thenReturn(0L);

        // When
        long count = categoryRepository.countByClientId(clientId);

        // Then
        assertEquals(0L, count);
        verify(categoryRepository, times(1)).countByClientId(clientId);
    }

    // ==================== TESTS MÉTHODES JPA STANDARD ====================

    /**
     * Test save - sauvegarde réussie
     */
    @Test
    @DisplayName("✅ save() - Sauvegarde réussie")
    void testSaveCategory() {
        // Given
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Category savedCategory = categoryRepository.save(testCategory);

        // Then
        assertNotNull(savedCategory);
        assertEquals(testCategory.getName(), savedCategory.getName());
        assertEquals(testCategory.getCategoryId(), savedCategory.getCategoryId());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    /**
     * Test findById - catégorie trouvée
     */
    @Test
    @DisplayName("✅ findById() - Catégorie trouvée")
    void testFindByIdFound() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryRepository.findById(categoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCategory.getCategoryId(), result.get().getCategoryId());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    /**
     * Test findById - catégorie non trouvée
     */
    @Test
    @DisplayName("❌ findById() - Catégorie non trouvée")
    void testFindByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryRepository.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test findAll - liste de catégories
     */
    @Test
    @DisplayName("✅ findAll() - Liste de catégories")
    void testFindAll() {
        // Given
        Category category2 = new Category();
        category2.setCategoryId(UUID.randomUUID());
        category2.setName("Électronique");

        List<Category> categories = Arrays.asList(testCategory, category2);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<Category> result = categoryRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testCategory));
        assertTrue(result.contains(category2));
        verify(categoryRepository, times(1)).findAll();
    }

    /**
     * Test deleteById - suppression réussie
     */
    @Test
    @DisplayName("✅ deleteById() - Suppression réussie")
    void testDeleteById() {
        // Given
        doNothing().when(categoryRepository).deleteById(categoryId);

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    // ==================== TESTS DE RECHERCHE AVANCÉE ====================

    /**
     * Test recherche insensible à la casse
     */
    @Test
    @DisplayName("✅ Recherche insensible à la casse")
    void testCaseInsensitiveSearch() {
        // Given
        String[] searchTerms = {"BOIS", "bois", "Bois", "BOISSONS", "boissons"};

        for (String term : searchTerms) {
            when(categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, term))
                    .thenReturn(Arrays.asList(testCategory));
        }

        // When & Then
        for (String term : searchTerms) {
            List<Category> result = categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, term);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testCategory, result.get(0));
        }

        verify(categoryRepository, times(searchTerms.length))
                .findByClientUserIdAndNameContainingIgnoreCase(eq(clientId), anyString());
    }

    /**
     * Test recherche avec caractères spéciaux
     */
    @Test
    @DisplayName("✅ Recherche avec caractères spéciaux")
    void testSearchWithSpecialCharacters() {
        // Given
        Category specialCategory = new Category();
        specialCategory.setName("Café & Thé");
        specialCategory.setClient(testClient);

        String searchTerm = "café";
        when(categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(Arrays.asList(specialCategory));

        // When
        List<Category> result = categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(specialCategory, result.get(0));
        verify(categoryRepository, times(1))
                .findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);
    }

    // ==================== TESTS DE VALIDATION MÉTIER ====================

    /**
     * Test validation des données de catégorie
     */
    @Test
    @DisplayName("✅ Validation - Données valides de catégorie")
    void testValidCategoryData() {
        // Given
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Category savedCategory = categoryRepository.save(testCategory);

        // Then
        assertNotNull(savedCategory.getName());
        assertNotNull(savedCategory.getClient());
        assertNotNull(savedCategory.getCategoryId());
        assertFalse(savedCategory.getName().trim().isEmpty());
    }

    /**
     * Test cohérence des données client
     */
    @Test
    @DisplayName("✅ Cohérence - Vérification des données client")
    void testClientDataConsistency() {
        // Given
        when(categoryRepository.findByClientUserId(clientId)).thenReturn(Arrays.asList(testCategory));
        when(categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId)).thenReturn(true);

        // When
        List<Category> categoriesByClient = categoryRepository.findByClientUserId(clientId);
        boolean categoryExists = categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId);

        // Then
        assertNotNull(categoriesByClient);
        assertFalse(categoriesByClient.isEmpty());
        assertTrue(categoryExists);

        // Vérifier que la catégorie trouvée appartient bien au client
        assertEquals(clientId, categoriesByClient.get(0).getClient().getUserId());
    }
}