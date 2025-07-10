/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : CategoryControllerTest.java
 * @description : Tests unitaires pour le contrôleur des catégories
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import esgi.easisell.dto.CategoryDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.entity.Client;
import esgi.easisell.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le contrôleur des catégories
 * Couverture 100% des lignes de code
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDTO categoryDTO;
    private Category mockCategory;
    private Client mockClient;
    private UUID categoryId;
    private UUID clientId;

    /**
     * Configuration initiale pour chaque test
     */
    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        mockClient = new Client();
        mockClient.setUserId(clientId);
        mockClient.setName("Test Store");

        mockCategory = new Category();
        mockCategory.setCategoryId(categoryId);
        mockCategory.setName("Boissons");
        mockCategory.setClient(mockClient);
        mockCategory.setProducts(new ArrayList<>());

        categoryDTO = new CategoryDTO();
        categoryDTO.setName("Boissons");
        categoryDTO.setClientId(clientId.toString());
    }

    /**
     * Test de création d'une catégorie réussie
     */
    @Test
    @DisplayName("✅ createCategory() - Succès")
    void testCreateCategorySuccess() {
        // Given
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(mockCategory);

        // When
        ResponseEntity<?> response = categoryController.createCategory(categoryDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Test de création d'une catégorie avec exception
     */
    @Test
    @DisplayName("❌ createCategory() - Exception")
    void testCreateCategoryException() {
        // Given
        when(categoryService.createCategory(any(CategoryDTO.class)))
                .thenThrow(new RuntimeException("Erreur création"));

        // When
        ResponseEntity<?> response = categoryController.createCategory(categoryDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de récupération de toutes les catégories
     */
    @Test
    @DisplayName("✅ getAllCategories() - Succès")
    void testGetAllCategories() {
        // Given
        when(categoryService.getAllCategories()).thenReturn(List.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.getAllCategories();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de récupération des catégories par client
     */
    @Test
    @DisplayName("✅ getCategoriesByClient() - Succès")
    void testGetCategoriesByClient() {
        // Given
        when(categoryService.getCategoriesByClient(clientId)).thenReturn(List.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.getCategoriesByClient(clientId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de récupération d'une catégorie par ID - trouvée
     */
    @Test
    @DisplayName("✅ getCategoryById() - Trouvée")
    void testGetCategoryByIdFound() {
        // Given
        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.getCategoryById(categoryId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de récupération d'une catégorie par ID - non trouvée
     */
    @Test
    @DisplayName("❌ getCategoryById() - Non trouvée")
    void testGetCategoryByIdNotFound() {
        // Given
        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = categoryController.getCategoryById(categoryId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de mise à jour d'une catégorie réussie
     */
    @Test
    @DisplayName("✅ updateCategory() - Succès")
    void testUpdateCategorySuccess() {
        // Given
        when(categoryService.updateCategory(categoryId, categoryDTO)).thenReturn(Optional.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.updateCategory(categoryId, categoryDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de mise à jour d'une catégorie non trouvée
     */
    @Test
    @DisplayName("❌ updateCategory() - Non trouvée")
    void testUpdateCategoryNotFound() {
        // Given
        when(categoryService.updateCategory(categoryId, categoryDTO)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = categoryController.updateCategory(categoryId, categoryDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de mise à jour d'une catégorie avec exception
     */
    @Test
    @DisplayName("❌ updateCategory() - Exception")
    void testUpdateCategoryException() {
        // Given
        when(categoryService.updateCategory(categoryId, categoryDTO))
                .thenThrow(new RuntimeException("Erreur mise à jour"));

        // When
        ResponseEntity<?> response = categoryController.updateCategory(categoryId, categoryDTO);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de suppression d'une catégorie réussie
     */
    @Test
    @DisplayName("✅ deleteCategory() - Succès")
    void testDeleteCategorySuccess() {
        // Given
        when(categoryService.deleteCategory(categoryId)).thenReturn(true);

        // When
        ResponseEntity<?> response = categoryController.deleteCategory(categoryId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de suppression d'une catégorie non trouvée
     */
    @Test
    @DisplayName("❌ deleteCategory() - Non trouvée")
    void testDeleteCategoryNotFound() {
        // Given
        when(categoryService.deleteCategory(categoryId)).thenReturn(false);

        // When
        ResponseEntity<?> response = categoryController.deleteCategory(categoryId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de recherche de catégories par nom
     */
    @Test
    @DisplayName("✅ searchCategoriesByName() - Succès")
    void testSearchCategoriesByName() {
        // Given
        when(categoryService.searchCategoriesByName(clientId, "Bois")).thenReturn(List.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.searchCategoriesByName(clientId, "Bois");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de mise à jour partielle d'une catégorie avec nom
     */
    @Test
    @DisplayName("✅ patchCategory() - Succès avec name")
    void testPatchCategorySuccessWithName() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "Nouveau nom");

        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(categoryService.updateCategory(eq(categoryId), any(CategoryDTO.class))).thenReturn(Optional.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.patchCategory(categoryId, updates);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de mise à jour partielle d'une catégorie non trouvée
     */
    @Test
    @DisplayName("❌ patchCategory() - Catégorie non trouvée")
    void testPatchCategoryNotFound() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "Nouveau nom");

        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = categoryController.patchCategory(categoryId, updates);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de mise à jour partielle sans changements
     */
    @Test
    @DisplayName("✅ patchCategory() - Sans changements")
    void testPatchCategoryNoChanges() {
        // Given
        Map<String, String> updates = new HashMap<>();
        // Pas de "name" dans les updates

        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.of(mockCategory));

        // When
        ResponseEntity<?> response = categoryController.patchCategory(categoryId, updates);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test de mise à jour partielle avec exception
     */
    @Test
    @DisplayName("❌ patchCategory() - Exception")
    void testPatchCategoryException() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "Nouveau nom");

        when(categoryService.getCategoryById(categoryId)).thenThrow(new RuntimeException("Erreur patch"));

        // When
        ResponseEntity<?> response = categoryController.patchCategory(categoryId, updates);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test de mise à jour partielle avec échec de mise à jour
     */
    @Test
    @DisplayName("✅ patchCategory() - Update retourne empty")
    void testPatchCategoryUpdateReturnsEmpty() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "Nouveau nom");

        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(categoryService.updateCategory(eq(categoryId), any(CategoryDTO.class))).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = categoryController.patchCategory(categoryId, updates);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test de conversion vers DTO avec produits
     */
    @Test
    @DisplayName("✅ convertToResponseDTO() - Avec produits")
    void testConvertToResponseDTOWithProducts() {
        // Given
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(mockCategory);

        // When
        ResponseEntity<?> response = categoryController.createCategory(categoryDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Test de conversion vers DTO sans produits
     */
    @Test
    @DisplayName("✅ convertToResponseDTO() - Sans produits")
    void testConvertToResponseDTOWithoutProducts() {
        // Given
        mockCategory.setProducts(null); // Cas où products est null
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(mockCategory);

        // When
        ResponseEntity<?> response = categoryController.createCategory(categoryDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}