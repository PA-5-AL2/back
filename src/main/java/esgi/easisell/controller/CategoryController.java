package esgi.easisell.controller;

import esgi.easisell.dto.CategoryDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            return ResponseEntity.ok(category);
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
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Récupérer les catégories d'un client spécifique
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Category>> getCategoriesByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(categoryService.getCategoriesByClient(clientId));
    }

    /**
     * Récupérer une catégorie par ID
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable UUID categoryId) {
        return categoryService.getCategoryById(categoryId)
                .map(ResponseEntity::ok)
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
                    .map(ResponseEntity::ok)
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
    public ResponseEntity<List<Category>> searchCategoriesByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        return ResponseEntity.ok(categoryService.searchCategoriesByName(clientId, name));
    }

    /**
     * Vérifier si une catégorie appartient à un client
     */
    @GetMapping("/verify-ownership")
    public ResponseEntity<Map<String, Boolean>> verifyCategoryOwnership(
            @RequestParam UUID categoryId,
            @RequestParam UUID clientId) {
        boolean isOwned = categoryService.isCategoryOwnedByClient(categoryId, clientId);
        return ResponseEntity.ok(Map.of("isOwned", isOwned));
    }
}