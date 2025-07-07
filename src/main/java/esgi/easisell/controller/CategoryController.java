package esgi.easisell.controller;

import esgi.easisell.dto.CategoryDTO;
import esgi.easisell.dto.CategoryResponseDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Créer une nouvelle catégorie
     */
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            Category category = categoryService.createCategory(categoryDTO);
            return ResponseEntity.ok(convertToResponseDTO(category));
        } catch (Exception e) {
            log.error("Erreur lors de la création de la catégorie", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer toutes les catégories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    /**
     * Récupérer les catégories d'un client spécifique
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByClient(@PathVariable UUID clientId) {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesByClient(clientId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    /**
     * Récupérer une catégorie par ID
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable UUID categoryId) {
        return categoryService.getCategoryById(categoryId)
                .map(category -> ResponseEntity.ok(convertToResponseDTO(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour une catégorie
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable UUID categoryId,
                                            @RequestBody CategoryDTO categoryDTO) {
        try {
            return categoryService.updateCategory(categoryId, categoryDTO)
                    .map(category -> ResponseEntity.ok(convertToResponseDTO(category)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la catégorie", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer une catégorie
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable UUID categoryId) {
        boolean deleted = categoryService.deleteCategory(categoryId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Catégorie supprimée avec succès"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rechercher des catégories par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDTO>> searchCategoriesByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        List<CategoryResponseDTO> categories = categoryService.searchCategoriesByName(clientId, name).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    /**
     * Mettre à jour partiellement une catégorie (PATCH)
     */
    @PatchMapping("/{categoryId}")
    public ResponseEntity<?> patchCategory(@PathVariable UUID categoryId,
                                           @RequestBody Map<String, String> updates) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Category category = categoryOpt.get();

            // Appliquer seulement les champs fournis
            if (updates.containsKey("name")) {
                CategoryDTO dto = new CategoryDTO();
                dto.setName(updates.get("name"));
                dto.setClientId(category.getClient().getUserId().toString());

                return categoryService.updateCategory(categoryId, dto)
                        .map(cat -> ResponseEntity.ok(convertToResponseDTO(cat)))
                        .orElse(ResponseEntity.notFound().build());
            }

            return ResponseEntity.ok(convertToResponseDTO(category));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour partielle de la catégorie", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Convertir une entité Category en DTO
     */
    private CategoryResponseDTO convertToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setClientId(category.getClient().getUserId());
        dto.setClientName(category.getClient().getName());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return dto;
    }
}