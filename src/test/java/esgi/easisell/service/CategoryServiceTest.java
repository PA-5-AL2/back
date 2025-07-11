package esgi.easisell.service;

import esgi.easisell.dto.CategoryDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.CategoryRepository;
import esgi.easisell.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID clientId;
    private UUID categoryId;
    private Client testClient;
    private Category testCategory;
    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setFirstName("John");
        testClient.setUsername("john.doe@example.com");
        testClient.setName("Supérette John");


        testCategory = new Category();
        testCategory.setCategoryId(categoryId);
        testCategory.setName("Électronique");
        testCategory.setClient(testClient);

        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setName("Électronique");
        testCategoryDTO.setClientId(clientId.toString());
    }

    @Test
    void createCategory_ShouldReturnCreatedCategory_WhenClientExists() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Category result = categoryService.createCategory(testCategoryDTO);

        // Then
        assertNotNull(result);
        assertEquals("Électronique", result.getName());
        assertEquals(testClient, result.getClient());
        verify(clientRepository, times(1)).findById(clientId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowException_WhenClientNotFound() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryService.createCategory(testCategoryDTO));

        assertTrue(exception.getMessage().contains("Client non trouvé"));
        verify(clientRepository, times(1)).findById(clientId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory, result.get(0));
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoriesByClient_ShouldReturnClientCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByClientUserId(clientId)).thenReturn(categories);

        // When
        List<Category> result = categoryService.getCategoriesByClient(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory, result.get(0));
        verify(categoryRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenExists() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.getCategoryById(categoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCategory, result.get());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void getCategoryById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.getCategoryById(categoryId);

        // Then
        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void updateCategory_ShouldUpdateCategoryName_WhenCategoryExists() {
        // Given
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Électronique Mise à jour");
        updateDTO.setClientId(clientId.toString());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Optional<Category> result = categoryService.updateCategory(categoryId, updateDTO);

        // Then
        assertTrue(result.isPresent());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void updateCategory_ShouldReturnEmpty_WhenCategoryNotExists() {
        // Given
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Électronique Mise à jour");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.updateCategory(categoryId, updateDTO);

        // Then
        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldReturnTrue_WhenCategoryExists() {
        // Given
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // When
        boolean result = categoryService.deleteCategory(categoryId);

        // Then
        assertTrue(result);
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    void deleteCategory_ShouldReturnFalse_WhenCategoryNotExists() {
        // Given
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // When
        boolean result = categoryService.deleteCategory(categoryId);

        // Then
        assertFalse(result);
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(categoryRepository, never()).deleteById(categoryId);
    }

    @Test
    void searchCategoriesByName_ShouldReturnMatchingCategories() {
        // Given
        String searchTerm = "Électr";
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm))
                .thenReturn(categories);

        // When
        List<Category> result = categoryService.searchCategoriesByName(clientId, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory, result.get(0));
        verify(categoryRepository, times(1))
                .findByClientUserIdAndNameContainingIgnoreCase(clientId, searchTerm);
    }

    @Test
    void isCategoryOwnedByClient_ShouldReturnTrue_WhenCategoryBelongsToClient() {
        // Given
        when(categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId))
                .thenReturn(true);

        // When
        boolean result = categoryService.isCategoryOwnedByClient(categoryId, clientId);

        // Then
        assertTrue(result);
        verify(categoryRepository, times(1))
                .existsByClientUserIdAndCategoryId(clientId, categoryId);
    }

    @Test
    void isCategoryOwnedByClient_ShouldReturnFalse_WhenCategoryDoesNotBelongToClient() {
        // Given
        when(categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId))
                .thenReturn(false);

        // When
        boolean result = categoryService.isCategoryOwnedByClient(categoryId, clientId);

        // Then
        assertFalse(result);
        verify(categoryRepository, times(1))
                .existsByClientUserIdAndCategoryId(clientId, categoryId);
    }
}