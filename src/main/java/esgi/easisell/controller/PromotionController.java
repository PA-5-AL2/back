package esgi.easisell.controller;

import esgi.easisell.dto.PromotionDTO;
import esgi.easisell.dto.PromotionResponseDTO;
import esgi.easisell.entity.Promotion;
import esgi.easisell.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors; // ⚠️ IMPORT MANQUANT

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  CONTRÔLEUR PROMOTION - GESTION DES OFFRES COMMERCIALES
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : PromotionController.java
 * @description : Contrôleur REST pour la gestion des promotions
 * @author      : SEDDAR SAMIRA
 * @version     : v1.0.0
 * @date        : 17/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    // ========== CRUD BASIQUE ==========

    /**
     * Créer une nouvelle promotion
     * POST /api/promotions
     */
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDTO promotionDTO) {
        log.info(" Création de promotion: {} pour le produit: {}",
                promotionDTO.getName(), promotionDTO.getProductId());

        PromotionResponseDTO result = promotionService.createPromotion(promotionDTO);

        return result != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Erreur lors de la création de la promotion (conflit ou données invalides)"
        ));
    }

    /**
     * Récupérer une promotion par ID
     * GET /api/promotions/{promotionId}
     */
    @GetMapping("/{promotionId}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID promotionId) {
        log.info(" Récupération de la promotion: {}", promotionId);

        PromotionResponseDTO promotion = promotionService.getPromotionById(promotionId);

        return promotion != null
                ? ResponseEntity.ok(promotion)
                : ResponseEntity.notFound().build();
    }

    /**
     * Mettre à jour une promotion
     * PUT /api/promotions/{promotionId}
     */
    @PutMapping("/{promotionId}")
    public ResponseEntity<?> updatePromotion(@PathVariable UUID promotionId,
                                             @RequestBody PromotionDTO promotionDTO) {
        log.info(" Mise à jour de la promotion: {}", promotionId);

        PromotionResponseDTO result = promotionService.updatePromotion(promotionId, promotionDTO);

        return result != null
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Erreur lors de la mise à jour (promotion non trouvée ou conflit)"
        ));
    }

    /**
     * Supprimer une promotion
     * DELETE /api/promotions/{promotionId}
     */
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID promotionId) {
        log.info("🗑 Suppression de la promotion: {}", promotionId);

        boolean deleted = promotionService.deletePromotion(promotionId);

        return deleted
                ? ResponseEntity.ok(Map.of("success", true, "message", "Promotion supprimée avec succès"))
                : ResponseEntity.notFound().build();
    }

    // ========== GESTION PAR CLIENT ==========

    /**
     * Récupérer toutes les promotions d'un client
     * GET /api/promotions/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PromotionResponseDTO>> getPromotionsByClient(@PathVariable UUID clientId) {
        log.info(" Récupération des promotions du client: {}", clientId);

        List<PromotionResponseDTO> promotions = promotionService.getPromotionsByClient(clientId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Récupérer les promotions actives d'un client
     * GET /api/promotions/client/{clientId}/active
     */
    @GetMapping("/client/{clientId}/active")
    public ResponseEntity<List<PromotionResponseDTO>> getActivePromotionsByClient(@PathVariable UUID clientId) {
        log.info(" Récupération des promotions actives du client: {}", clientId);

        List<PromotionResponseDTO> promotions = promotionService.getActivePromotionsByClient(clientId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Récupérer les promotions qui expirent bientôt
     * GET /api/promotions/client/{clientId}/expiring?days=7
     */
    @GetMapping("/client/{clientId}/expiring")
    public ResponseEntity<List<PromotionResponseDTO>> getExpiringSoonPromotions(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "7") int days) {
        log.info(" Recherche des promotions expirant dans {} jours pour le client: {}", days, clientId);

        List<PromotionResponseDTO> promotions = promotionService.getExpiringSoonPromotions(clientId, days);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Récupérer les promotions à venir
     * GET /api/promotions/client/{clientId}/upcoming?days=30
     */
    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<List<PromotionResponseDTO>> getUpcomingPromotions(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "30") int days) {
        log.info(" Recherche des promotions à venir dans {} jours pour le client: {}", days, clientId);

        List<PromotionResponseDTO> promotions = promotionService.getUpcomingPromotions(clientId, days);
        return ResponseEntity.ok(promotions);
    }

    // ========== GESTION PAR PRODUIT ==========

    /**
     * Récupérer les promotions d'un produit
     * GET /api/promotions/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PromotionResponseDTO>> getPromotionsByProduct(@PathVariable UUID productId) {
        log.info(" Récupération des promotions du produit: {}", productId);

        List<PromotionResponseDTO> promotions = promotionService.getPromotionsByProduct(productId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Désactiver toutes les promotions d'un produit
     * PUT /api/promotions/product/{productId}/deactivate-all
     */
    @PutMapping("/product/{productId}/deactivate-all")
    public ResponseEntity<?> deactivateAllPromotionsForProduct(@PathVariable UUID productId) {
        log.info("⏸ Désactivation de toutes les promotions du produit: {}", productId);

        int count = promotionService.deactivateAllPromotionsForProduct(productId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + " promotion(s) désactivée(s)",
                "count", count,
                "productId", productId
        ));
    }

    // ========== ACTIVATION/DÉSACTIVATION ==========

    /**
     * Activer une promotion
     * PUT /api/promotions/{promotionId}/activate
     */
    @PutMapping("/{promotionId}/activate")
    public ResponseEntity<?> activatePromotion(@PathVariable UUID promotionId) {
        log.info(" Activation de la promotion: {}", promotionId);

        PromotionResponseDTO result = promotionService.activatePromotion(promotionId);

        return result != null
                ? ResponseEntity.ok(Map.of("success", true, "promotion", result))
                : ResponseEntity.notFound().build();
    }

    /**
     * Désactiver une promotion
     * PUT /api/promotions/{promotionId}/deactivate
     */
    @PutMapping("/{promotionId}/deactivate")
    public ResponseEntity<?> deactivatePromotion(@PathVariable UUID promotionId) {
        log.info(" Désactivation de la promotion: {}", promotionId);

        PromotionResponseDTO result = promotionService.deactivatePromotion(promotionId);

        return result != null
                ? ResponseEntity.ok(Map.of("success", true, "promotion", result))
                : ResponseEntity.notFound().build();
    }

    // ========== RECHERCHE ET FILTRAGE ==========

    /**
     * Rechercher des promotions par nom de produit
     * GET /api/promotions/search?clientId={uuid}&productName={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<PromotionResponseDTO>> searchPromotionsByProductName(
            @RequestParam UUID clientId,
            @RequestParam String productName) {
        log.info(" Recherche de promotions par nom de produit: '{}' pour le client: {}", productName, clientId);

        List<PromotionResponseDTO> promotions = promotionService.searchPromotionsByProductName(clientId, productName);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Filtrer les promotions par type
     * GET /api/promotions/filter?clientId={uuid}&type=PERCENTAGE&status=ACTIVE
     */
    @GetMapping("/filter")
    public ResponseEntity<List<PromotionResponseDTO>> filterPromotions(
            @RequestParam UUID clientId,
            @RequestParam(required = false) Promotion.PromotionType type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info(" Filtrage des promotions du client: {} (type: {}, statut: {})", clientId, type, status);

        List<PromotionResponseDTO> allPromotions = promotionService.getPromotionsByClient(clientId);

        // Appliquer les filtres
        List<PromotionResponseDTO> filteredPromotions = allPromotions.stream()
                .filter(promo -> type == null || promo.getPromotionType().equals(type))
                .filter(promo -> status == null || promo.getStatus().equalsIgnoreCase(status))
                .filter(promo -> startDate == null || promo.getStartDate().toLocalDateTime().isAfter(startDate))
                .filter(promo -> endDate == null || promo.getEndDate().toLocalDateTime().isBefore(endDate))
                .collect(Collectors.toList()); //  CORRECTION: utilisez collect() au lieu de toList()

        return ResponseEntity.ok(filteredPromotions);
    }

    // ========== STATISTIQUES ==========

    /**
     * Statistiques des promotions d'un client
     * GET /api/promotions/stats/{clientId}
     */
    @GetMapping("/stats/{clientId}")
    public ResponseEntity<?> getPromotionStats(@PathVariable UUID clientId) {
        log.info(" Génération des statistiques promotions pour le client: {}", clientId);

        try {
            Map<String, Object> stats = promotionService.getPromotionStats(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la génération des statistiques: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors de la génération des statistiques: " + e.getMessage()
            ));
        }
    }

    // ========== ACTIONS EN LOT ==========

    /**
     * Créer une promotion flash (24h)
     * POST /api/promotions/flash
     */
    @PostMapping("/flash")
    public ResponseEntity<?> createFlashPromotion(@RequestBody Map<String, Object> flashData) {
        log.info("⚡ Création d'une promotion flash");

        try {
            UUID productId = UUID.fromString((String) flashData.get("productId"));
            UUID clientId = UUID.fromString((String) flashData.get("clientId"));
            String discountType = (String) flashData.get("discountType"); // "percentage" ou "amount"
            Double discountValue = ((Number) flashData.get("discountValue")).doubleValue();
            String name = (String) flashData.getOrDefault("name", "🔥 Promotion Flash 24h");

            PromotionDTO promotionDTO = new PromotionDTO();
            promotionDTO.setName(name);
            promotionDTO.setDescription("Promotion flash valable 24 heures seulement !");
            promotionDTO.setProductId(productId);
            promotionDTO.setClientId(clientId);
            promotionDTO.setStartDate(LocalDateTime.now());
            promotionDTO.setEndDate(LocalDateTime.now().plusHours(24));
            promotionDTO.setIsActive(true);

            if ("percentage".equals(discountType)) {
                promotionDTO.setPromotionType(Promotion.PromotionType.PERCENTAGE);
                promotionDTO.setDiscountPercentage(java.math.BigDecimal.valueOf(discountValue));
            } else {
                promotionDTO.setPromotionType(Promotion.PromotionType.FIXED_AMOUNT);
                promotionDTO.setDiscountAmount(java.math.BigDecimal.valueOf(discountValue));
            }

            PromotionResponseDTO result = promotionService.createPromotion(promotionDTO);

            return result != null
                    ? ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "🔥 Promotion flash créée avec succès !",
                    "promotion", result
            ))
                    : ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors de la création de la promotion flash"
            ));

        } catch (Exception e) {
            log.error(" Erreur création promotion flash: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Dupliquer une promotion existante
     * POST /api/promotions/{promotionId}/duplicate
     */
    @PostMapping("/{promotionId}/duplicate")
    public ResponseEntity<?> duplicatePromotion(
            @PathVariable UUID promotionId,
            @RequestBody Map<String, Object> duplicateData) {
        log.info(" Duplication de la promotion: {}", promotionId);

        try {
            PromotionResponseDTO original = promotionService.getPromotionById(promotionId);
            if (original == null) {
                return ResponseEntity.notFound().build();
            }

            PromotionDTO newPromotionDTO = new PromotionDTO();
            newPromotionDTO.setName((String) duplicateData.getOrDefault("name", original.getName() + " (Copie)"));
            newPromotionDTO.setDescription(original.getDescription());
            newPromotionDTO.setProductId(original.getProductId());
            newPromotionDTO.setClientId(original.getClientId());
            newPromotionDTO.setPromotionType(original.getPromotionType());
            newPromotionDTO.setDiscountPercentage(original.getDiscountPercentage());
            newPromotionDTO.setDiscountAmount(original.getDiscountAmount());
            newPromotionDTO.setPromotionPrice(original.getPromotionPrice());

            // Nouvelles dates (par défaut : commence maintenant, dure 7 jours)
            LocalDateTime newStartDate = duplicateData.containsKey("startDate") ?
                    LocalDateTime.parse((String) duplicateData.get("startDate")) : LocalDateTime.now();
            LocalDateTime newEndDate = duplicateData.containsKey("endDate") ?
                    LocalDateTime.parse((String) duplicateData.get("endDate")) : newStartDate.plusDays(7);

            newPromotionDTO.setStartDate(newStartDate);
            newPromotionDTO.setEndDate(newEndDate);
            newPromotionDTO.setIsActive(true);

            PromotionResponseDTO result = promotionService.createPromotion(newPromotionDTO);

            return result != null
                    ? ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Promotion dupliquée avec succès",
                    "original", original,
                    "duplicate", result
            ))
                    : ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors de la duplication"
            ));

        } catch (Exception e) {
            log.error(" Erreur duplication promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtenir les types de promotion disponibles
     * GET /api/promotions/types
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getPromotionTypes() {
        log.info(" Récupération des types de promotion disponibles");

        Map<String, String> types = new HashMap<>();
        for (Promotion.PromotionType type : Promotion.PromotionType.values()) {
            types.put(type.name(), type.getLabel());
        }

        return ResponseEntity.ok(Map.of(
                "types", types,
                "examples", Map.of(
                        "PERCENTAGE", "Réduction de 20% sur le prix",
                        "FIXED_AMOUNT", "Réduction de 5€ sur le prix",
                        "FIXED_PRICE", "Prix fixe de 2.99€ au lieu du prix normal"
                )
        ));
    }
}