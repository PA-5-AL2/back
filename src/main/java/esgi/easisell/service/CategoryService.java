package esgi.easisell.service;

import esgi.easisell.dto.CategoryDTO;
import esgi.easisell.entity.Category;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.CategoryRepository;
import esgi.easisell.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;

    /**
     * Créer une nouvelle catégorie
     */
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        log.info("Création d'une nouvelle catégorie : {}", categoryDTO.getName());

        Client client = clientRepository.findById(UUID.fromString(categoryDTO.getClientId()))
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + categoryDTO.getClientId()));

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setClient(client);

        Category savedCategory = categoryRepository.save(category);
        log.info("Catégorie créée avec succès. ID: {}", savedCategory.getCategoryId());

        return savedCategory;
    }

    /**
     * Récupérer toutes les catégories
     */
    public List<Category> getAllCategories() {
        log.info("Récupération de toutes les catégories");
        return categoryRepository.findAll();
    }

    /**
     * Récupérer les catégories d'un client spécifique
     */
    public List<Category> getCategoriesByClient(UUID clientId) {
        log.info("Récupération des catégories pour le client ID: {}", clientId);
        return categoryRepository.findByClientUserId(clientId);
    }

    /**
     * Récupérer une catégorie par ID
     */
    public Optional<Category> getCategoryById(UUID categoryId) {
        log.info("Récupération de la catégorie ID: {}", categoryId);
        return categoryRepository.findById(categoryId);
    }

    /**
     * Mettre à jour une catégorie
     */
    @Transactional
    public Optional<Category> updateCategory(UUID categoryId, CategoryDTO categoryDTO) {
        log.info("Mise à jour de la catégorie ID: {}", categoryId);

        return categoryRepository.findById(categoryId)
                .map(category -> {
                    if (categoryDTO.getName() != null) {
                        category.setName(categoryDTO.getName());
                    }

                    if (categoryDTO.getClientId() != null &&
                            !category.getClient().getUserId().toString().equals(categoryDTO.getClientId())) {

                        Client newClient = clientRepository.findById(UUID.fromString(categoryDTO.getClientId()))
                                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + categoryDTO.getClientId()));

                        category.setClient(newClient);
                    }

                    Category updated = categoryRepository.save(category);
                    log.info("Catégorie mise à jour avec succès. ID: {}", categoryId);
                    return updated;
                });
    }

    /**
     * Supprimer une catégorie
     */
    @Transactional
    public boolean deleteCategory(UUID categoryId) {
        log.info("Suppression de la catégorie ID: {}", categoryId);

        if (categoryRepository.existsById(categoryId)) {
            categoryRepository.deleteById(categoryId);
            log.info("Catégorie supprimée avec succès. ID: {}", categoryId);
            return true;
        }

        log.warn("Catégorie non trouvée pour la suppression. ID: {}", categoryId);
        return false;
    }

    /**
     * Rechercher des catégories par nom
     */
    public List<Category> searchCategoriesByName(UUID clientId, String name) {
        log.info("Recherche de catégories contenant '{}' pour le client ID: {}", name, clientId);
        return categoryRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, name);
    }

    /**
     * Vérifier si une catégorie appartient à un client
     */
    public boolean isCategoryOwnedByClient(UUID categoryId, UUID clientId) {
        log.info("Vérification de la propriété de la catégorie ID: {} pour le client ID: {}", categoryId, clientId);
        return categoryRepository.existsByClientUserIdAndCategoryId(clientId, categoryId);
    }
}