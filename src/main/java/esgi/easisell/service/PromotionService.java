package esgi.easisell.service;

import esgi.easisell.dto.PromotionDTO;
import esgi.easisell.dto.PromotionResponseDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.Promotion;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.ProductRepository;
import esgi.easisell.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  SERVICE PROMOTION - LOGIQUE MÉTIER
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    // ========== CRÉATION ET GESTION DES PROMOTIONS ==========

    /**
     * Créer une nouvelle promotion
     */
    @Transactional
    public PromotionResponseDTO createPromotion(PromotionDTO promotionDTO) {
        log.info(" Création d'une promotion: {} pour le produit: {}",
                promotionDTO.getName(), promotionDTO.getProductId());

        // Validation des données
        if (!isValidPromotionDTO(promotionDTO)) {
            log.error(" Données de promotion invalides");
            return null;
        }

        // Vérifier que le produit existe
        Optional<Product> productOpt = productRepository.findById(promotionDTO.getProductId());
        if (productOpt.isEmpty()) {
            log.error(" Produit non trouvé: {}", promotionDTO.getProductId());
            return null;
        }

        Product product = productOpt.get();

        // Vérifier que le client existe
        Optional<Client> clientOpt = clientRepository.findById(promotionDTO.getClientId());
        if (clientOpt.isEmpty()) {
            log.error(" Client non trouvé: {}", promotionDTO.getClientId());
            return null;
        }

        // Vérifier les conflits de promotion
        if (hasActivePromotionConflict(promotionDTO.getProductId(), Timestamp.valueOf(promotionDTO.getStartDate()), Timestamp.valueOf(promotionDTO.getEndDate()))) {
            log.error(" Conflit de promotion détecté pour le produit: {}", promotionDTO.getProductId());
            return null;
        }

        // Créer la promotion
        Promotion promotion = buildPromotion(promotionDTO, product, clientOpt.get());
        Promotion savedPromotion = promotionRepository.save(promotion);

        log.info(" Promotion créée avec succès. ID: {} - Type: {} - Réduction: {}",
                savedPromotion.getPromotionId(),
                savedPromotion.getPromotionType(),
                savedPromotion.getFormattedDiscount());

        return new PromotionResponseDTO(savedPromotion);
    }

    /**
     * Mettre à jour une promotion
     */
    @Transactional
    public PromotionResponseDTO updatePromotion(UUID promotionId, PromotionDTO promotionDTO) {
        log.info(" Mise à jour de la promotion: {}", promotionId);

        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        if (promotionOpt.isEmpty()) {
            log.error(" Promotion non trouvée: {}", promotionId);
            return null;
        }

        Promotion promotion = promotionOpt.get();
        updatePromotionFields(promotion, promotionDTO);

        // Vérifier les conflits après mise à jour
        if (hasActivePromotionConflict(promotion.getProduct().getProductId(),
                promotion.getStartDate(), promotion.getEndDate(), promotionId)) {
            log.error(" Conflit de promotion après mise à jour: {}", promotionId);
            return null;
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);
        log.info(" Promotion mise à jour: {}", promotionId);

        return new PromotionResponseDTO(updatedPromotion);
    }

    /**
     * Supprimer une promotion
     */
    @Transactional
    public boolean deletePromotion(UUID promotionId) {
        log.info("🗑 Suppression de la promotion: {}", promotionId);

        if (!promotionRepository.existsById(promotionId)) {
            log.error(" Promotion non trouvée: {}", promotionId);
            return false;
        }

        promotionRepository.deleteById(promotionId);
        log.info("Promotion supprimée: {}", promotionId);
        return true;
    }

    // ========== ACTIVATION/DÉSACTIVATION ==========

    /**
     * Activer une promotion
     */
    @Transactional
    public PromotionResponseDTO activatePromotion(UUID promotionId) {
        log.info("▶ Activation de la promotion: {}", promotionId);
        return togglePromotionStatus(promotionId, true);
    }

    /**
     * Désactiver une promotion
     */
    @Transactional
    public PromotionResponseDTO deactivatePromotion(UUID promotionId) {
        log.info("⏸ Désactivation de la promotion: {}", promotionId);
        return togglePromotionStatus(promotionId, false);
    }

    /**
     * Désactiver toutes les promotions d'un produit
     */
    @Transactional
    public int deactivateAllPromotionsForProduct(UUID productId) {
        log.info("⏸ Désactivation de toutes les promotions du produit: {}", productId);

        Timestamp now = getCurrentTimestamp();
        int count = promotionRepository.deactivateAllPromotionsForProduct(productId, now);

        log.info(" {} promotions désactivées pour le produit: {}", count, productId);
        return count;
    }
    private Timestamp getCurrentTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now().withNano(0));
    }

    // ========== RÉCUPÉRATION DES DONNÉES ==========

    /**
     * Récupérer toutes les promotions d'un client
     */
    public List<PromotionResponseDTO> getPromotionsByClient(UUID clientId) {
        log.info(" Récupération des promotions du client: {}", clientId);

        List<Promotion> promotions = promotionRepository.findByClientUserId(clientId);
        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les promotions actives d'un client
     */
    public List<PromotionResponseDTO> getActivePromotionsByClient(UUID clientId) {
        log.info(" Récupération des promotions actives du client: {}", clientId);

        Timestamp now = getCurrentTimestamp();
        List<Promotion> promotions = promotionRepository.findActivePromotionsByClient(clientId, now);

        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les promotions d'un produit
     */
    public List<PromotionResponseDTO> getPromotionsByProduct(UUID productId) {
        log.info(" Récupération des promotions du produit: {}", productId);

        List<Promotion> promotions = promotionRepository.findByProductProductId(productId);
        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une promotion par ID
     */
    public PromotionResponseDTO getPromotionById(UUID promotionId) {
        log.info(" Récupération de la promotion: {}", promotionId);

        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        return promotionOpt.map(PromotionResponseDTO::new).orElse(null);
    }

    // ========== RECHERCHES SPÉCIALISÉES ==========

    /**
     * Trouver les promotions qui expirent bientôt
     */
    public List<PromotionResponseDTO> getExpiringSoonPromotions(UUID clientId, int days) {
        log.info(" Recherche des promotions expirant dans {} jours pour le client: {}", days, clientId);

        Timestamp now = getCurrentTimestamp();
        Timestamp soonDate = Timestamp.valueOf(LocalDateTime.now().plusDays(days));

        List<Promotion> promotions = promotionRepository.findExpiringSoonPromotions(clientId, now, soonDate);
        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Trouver les promotions à venir
     */
    public List<PromotionResponseDTO> getUpcomingPromotions(UUID clientId, int days) {
        log.info(" Recherche des promotions à venir dans {} jours pour le client: {}", days, clientId);

        Timestamp now = getCurrentTimestamp();
        Timestamp futureDate = Timestamp.valueOf(LocalDateTime.now().plusDays(days));

        List<Promotion> promotions = promotionRepository.findUpcomingPromotions(clientId, now, futureDate);
        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des promotions par nom de produit
     */
    public List<PromotionResponseDTO> searchPromotionsByProductName(UUID clientId, String productName) {
        log.info(" Recherche de promotions par nom de produit: '{}' pour le client: {}", productName, clientId);

        List<Promotion> promotions = promotionRepository.findPromotionsByProductName(clientId, productName);
        return promotions.stream()
                .map(PromotionResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ========== STATISTIQUES ==========

    /**
     * Obtenir les statistiques des promotions d'un client
     */
    public Map<String, Object> getPromotionStats(UUID clientId) {
        log.info(" Génération des statistiques promotions pour le client: {}", clientId);

        Timestamp now = getCurrentTimestamp();

        long totalPromotions = promotionRepository.findByClientUserId(clientId).size();
        long activePromotions = promotionRepository.countActivePromotionsByClient(clientId, now);
        long productsOnPromotion = promotionRepository.countProductsOnPromotionByClient(clientId, now);

        List<Promotion> expiringSoon = promotionRepository.findExpiringSoonPromotions(
                clientId, now, Timestamp.valueOf(LocalDateTime.now().plusDays(7)));

        List<Promotion> upcoming = promotionRepository.findUpcomingPromotions(
                clientId, now, Timestamp.valueOf(LocalDateTime.now().plusDays(30)));

        Map<String, Object> stats = new HashMap<>();
        stats.put("clientId", clientId);
        stats.put("totalPromotions", totalPromotions);
        stats.put("activePromotions", activePromotions);
        stats.put("productsOnPromotion", productsOnPromotion);
        stats.put("expiringSoon", expiringSoon.size());
        stats.put("upcomingPromotions", upcoming.size());
        stats.put("promotionCoverage", totalPromotions > 0 ?
                Math.round((activePromotions * 100.0) / totalPromotions) : 0);

        return stats;
    }

    // ========== TÂCHE AUTOMATIQUE ==========

    /**
     * Tâche automatique pour désactiver les promotions expirées
     * Exécutée toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    @Transactional
    public void deactivateExpiredPromotions() {
        log.info(" Nettoyage automatique des promotions expirées");

        Timestamp now = getCurrentTimestamp();
        int deactivated = promotionRepository.deactivateExpiredPromotions(now);

        if (deactivated > 0) {
            log.info(" {} promotions expirées désactivées automatiquement", deactivated);
        }
    }

    // ========== MÉTHODES PRIVÉES ==========

    private boolean isValidPromotionDTO(PromotionDTO dto) {
        return dto.getName() != null && !dto.getName().trim().isEmpty() &&
                dto.getProductId() != null &&
                dto.getClientId() != null &&
                dto.getPromotionType() != null &&
                dto.getStartDate() != null &&
                dto.getEndDate() != null &&
                dto.getStartDate().isBefore(dto.getEndDate());
    }

    private boolean hasActivePromotionConflict(UUID productId, Timestamp startDate, Timestamp endDate) {
        return hasActivePromotionConflict(productId, startDate, endDate, null);
    }

    private boolean hasActivePromotionConflict(UUID productId, Timestamp startDate, Timestamp endDate, UUID excludePromotionId) {
        List<Promotion> existingPromotions = promotionRepository.findByProductProductId(productId);

        return existingPromotions.stream()
                .filter(p -> excludePromotionId == null || !p.getPromotionId().equals(excludePromotionId))
                .filter(Promotion::getIsActive)
                .anyMatch(p -> dateRangesOverlap(startDate, endDate, p.getStartDate(), p.getEndDate()));
    }

    private boolean dateRangesOverlap(Timestamp start1, Timestamp end1, Timestamp start2, Timestamp end2) {
        return start1.compareTo(end2) <= 0 && end1.compareTo(start2) >= 0;
    }

    private Promotion buildPromotion(PromotionDTO dto, Product product, Client client) {
        Promotion promotion = new Promotion();
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setPromotionType(dto.getPromotionType());
        promotion.setOriginalPrice(product.getUnitPrice());
        promotion.setStartDate(Timestamp.valueOf(dto.getStartDate()));
        promotion.setEndDate(Timestamp.valueOf(dto.getEndDate()));
        promotion.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        promotion.setProduct(product);
        promotion.setClient(client);

        // Définir les valeurs selon le type de promotion
        switch (dto.getPromotionType()) {
            case PERCENTAGE:
                promotion.setDiscountPercentage(dto.getDiscountPercentage());
                break;
            case FIXED_AMOUNT:
                promotion.setDiscountAmount(dto.getDiscountAmount());
                break;
            case FIXED_PRICE:
                promotion.setPromotionPrice(dto.getPromotionPrice());
                break;
        }

        return promotion;
    }

    private void updatePromotionFields(Promotion promotion, PromotionDTO dto) {
        if (dto.getName() != null) promotion.setName(dto.getName());
        if (dto.getDescription() != null) promotion.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) promotion.setStartDate(Timestamp.valueOf(dto.getStartDate()));
        if (dto.getEndDate() != null) promotion.setEndDate(Timestamp.valueOf(dto.getEndDate()));
        if (dto.getIsActive() != null) promotion.setIsActive(dto.getIsActive());

        // Mise à jour selon le type (si changé)
        if (dto.getPromotionType() != null && !dto.getPromotionType().equals(promotion.getPromotionType())) {
            promotion.setPromotionType(dto.getPromotionType());
            // Réinitialiser les valeurs
            promotion.setDiscountPercentage(null);
            promotion.setDiscountAmount(null);
            promotion.setPromotionPrice(null);
        }

        // Définir les nouvelles valeurs
        switch (promotion.getPromotionType()) {
            case PERCENTAGE:
                if (dto.getDiscountPercentage() != null) promotion.setDiscountPercentage(dto.getDiscountPercentage());
                break;
            case FIXED_AMOUNT:
                if (dto.getDiscountAmount() != null) promotion.setDiscountAmount(dto.getDiscountAmount());
                break;
            case FIXED_PRICE:
                if (dto.getPromotionPrice() != null) promotion.setPromotionPrice(dto.getPromotionPrice());
                break;
        }
    }

    private PromotionResponseDTO togglePromotionStatus(UUID promotionId, boolean isActive) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        if (promotionOpt.isEmpty()) {
            log.error(" Promotion non trouvée: {}", promotionId);
            return null;
        }

        Promotion promotion = promotionOpt.get();
        promotion.setIsActive(isActive);

        Promotion savedPromotion = promotionRepository.save(promotion);
        log.info(" Statut de promotion changé: {} -> {}", promotionId, isActive ? "ACTIVE" : "INACTIVE");

        return new PromotionResponseDTO(savedPromotion);
    }
}