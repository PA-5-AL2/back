package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.service.SaleService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import esgi.easisell.service.OptimisticStockService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@Slf4j
public class SaleController {

    private final SaleService saleService;
    private final SecurityUtils securityUtils;
    private final OptimisticStockService optimisticStockService;

    // Constructeur explicite avec log
    public SaleController(SaleService saleService, SecurityUtils securityUtils, OptimisticStockService optimisticStockService) {
        this.saleService = saleService;
        this.securityUtils = securityUtils;
        this.optimisticStockService = optimisticStockService;
        log.info("========== SaleController initialisé ! ==========");
    }

    // ========== CRÉATION DE VENTE ==========

    /**
     * Créer une nouvelle vente pour le client connecté
     * POST /api/sales/new
     *
     * Endpoint simplifié qui utilise automatiquement l'ID du client connecté
     */
    @PostMapping("/new")
    public ResponseEntity<?> createNewSaleForCurrentClient(HttpServletRequest request) {
        try {
            String userId = securityUtils.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Utilisateur non authentifié"));
            }

            UUID clientId = UUID.fromString(userId);
            log.info("Création d'une nouvelle vente pour le client connecté: {}", clientId);

            SaleResponseDTO sale = saleService.createNewSale(clientId);
            return ResponseEntity.status(HttpStatus.CREATED).body(sale);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la vente", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Créer une nouvelle vente
     * POST /api/sales/client/{clientId}
     */
    @PostMapping("/client/{clientId}")
    public ResponseEntity<?> createNewSale(@PathVariable UUID clientId, HttpServletRequest request) {
        log.info("Création d'une nouvelle vente pour le client: {}", clientId);

        // Vérification de sécurité
        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        try {
            SaleResponseDTO sale = saleService.createNewSale(clientId);
            return ResponseEntity.status(HttpStatus.CREATED).body(sale);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la vente", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== GESTION DES ARTICLES ==========

    /**
     * Ajouter un produit à la vente par scan du code-barres
     * POST /api/sales/{saleId}/items/scan
     */
    @PostMapping("/{saleId}/items/scan")
    public ResponseEntity<?> scanProduct(
            @PathVariable UUID saleId,
            @RequestParam String barcode,
            @RequestParam(defaultValue = "1") BigDecimal quantity,
            HttpServletRequest request) {

        log.info("Scan du produit {} pour la vente {}", barcode, saleId);

        // Vérification de sécurité
        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            SaleItemResponseDTO item = saleService.addProductToSale(saleId, barcode, quantity);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("Erreur lors du scan du produit", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ajouter un produit à la vente par ID (sélection manuelle)
     * POST /api/sales/{saleId}/items/product/{productId}
     */
    @PostMapping("/{saleId}/items/product/{productId}")
    public ResponseEntity<?> addProductById(
            @PathVariable UUID saleId,
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") BigDecimal quantity,
            HttpServletRequest request) {

        log.info("Ajout manuel du produit {} à la vente {}", productId, saleId);

        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            SaleItemResponseDTO item = saleService.addProductByIdToSale(saleId, productId, quantity);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du produit", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Modifier la quantité d'un article
     * PUT /api/sales/items/{saleItemId}
     */
    @PutMapping("/items/{saleItemId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable UUID saleItemId,
            @RequestParam BigDecimal quantity,
            HttpServletRequest request) {

        log.info("Mise à jour de la quantité de l'article {} à {}", saleItemId, quantity);

        try {
            SaleItemResponseDTO item = saleService.updateItemQuantity(saleItemId, quantity);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la quantité", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer un article de la vente
     * DELETE /api/sales/items/{saleItemId}
     */
    @DeleteMapping("/items/{saleItemId}")
    public ResponseEntity<?> removeItem(
            @PathVariable UUID saleItemId,
            HttpServletRequest request) {

        log.info("Suppression de l'article {}", saleItemId);

        try {
            saleService.removeItemFromSale(saleItemId);
            return ResponseEntity.ok(Map.of("message", "Article supprimé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'article", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== PAIEMENT ==========

    /**
     * Finaliser la vente et traiter le paiement
     * POST /api/sales/{saleId}/payment
     */
    @PostMapping("/{saleId}/payment")
    public ResponseEntity<?> processPayment(
            @PathVariable UUID saleId,
            @RequestParam String paymentType,
            @RequestParam BigDecimal amountReceived,
            @RequestParam(defaultValue = "EUR") String currency,
            HttpServletRequest request) {

        log.info("Traitement du paiement pour la vente {} - Type: {}, Montant: {}",
                saleId, paymentType, amountReceived);

        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            PaymentResultDTO result = saleService.processPayment(
                    saleId, paymentType, amountReceived, currency);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du paiement", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== CONSULTATION ==========

    /**
     * Obtenir les détails d'une vente
     * GET /api/sales/{saleId}
     */
    @GetMapping("/{saleId}")
    public ResponseEntity<?> getSaleDetails(
            @PathVariable UUID saleId,
            HttpServletRequest request) {

        log.info("Consultation de la vente {}", saleId);

        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            SaleDetailsDTO details = saleService.getSaleDetails(saleId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des détails", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Calculer le total de la vente
     * GET /api/sales/{saleId}/total
     */
    @GetMapping("/{saleId}/total")
    public ResponseEntity<?> calculateTotal(
            @PathVariable UUID saleId,
            HttpServletRequest request) {

        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            SaleTotalDTO total = saleService.calculateSaleTotal(saleId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            log.error("Erreur lors du calcul du total", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir l'historique des ventes d'un client
     * GET /api/sales/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getSalesByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        try {
            List<SaleResponseDTO> sales = saleService.getSalesByClient(clientId, page, size);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des ventes", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir les ventes du jour
     * GET /api/sales/client/{clientId}/today
     */
    @GetMapping("/client/{clientId}/today")
    public ResponseEntity<?> getTodaySales(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        try {
            List<SaleResponseDTO> sales = saleService.getTodaySales(clientId);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des ventes du jour", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir le total des ventes du jour
     * GET /api/sales/client/{clientId}/today/total
     */
    @GetMapping("/client/{clientId}/today/total")
    public ResponseEntity<?> getTodayTotal(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        try {
            BigDecimal total = saleService.getTodayTotalSales(clientId);
            return ResponseEntity.ok(Map.of(
                    "total", total,
                    "currency", "EUR" // À adapter selon le client
            ));
        } catch (Exception e) {
            log.error("Erreur lors du calcul du total du jour", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Générer les données du ticket de caisse
     * GET /api/sales/{saleId}/receipt
     */
    @GetMapping("/{saleId}/receipt")
    public ResponseEntity<?> getReceipt(
            @PathVariable UUID saleId,
            HttpServletRequest request) {

        if (!canAccessSale(saleId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à cette vente"));
        }

        try {
            ReceiptDTO receipt = saleService.generateReceiptData(saleId);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du ticket", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Vérifier si l'utilisateur peut accéder à une vente
     */
    private boolean canAccessSale(UUID saleId, HttpServletRequest request) {
        try {
            String userId = securityUtils.getCurrentUserId(request);
            if (userId == null) {
                return false;
            }

            if (securityUtils.isCurrentUserAdmin()) {
                return true;
            }

            return saleService.canAccessSale(saleId, UUID.fromString(userId));
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des droits", e);
            return false;
        }
    }

    /**
     * Vérifier la disponibilité d'un produit
     * GET /api/sales/check-availability
     */
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkProductAvailability(
            @RequestParam UUID productId,
            @RequestParam UUID clientId,
            @RequestParam BigDecimal quantity,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé"));
        }

        boolean available = saleService.checkProductAvailability(productId, clientId, quantity);
        int currentStock = optimisticStockService.getTotalStockQuantity(productId, clientId);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "currentStock", currentStock,
                "requestedQuantity", quantity,
                "productId", productId,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Stock temps réel d'un produit
     * GET /api/sales/realtime-stock/{productId}
     */
    @GetMapping("/realtime-stock/{productId}")
    public ResponseEntity<?> getRealtimeStock(
            @PathVariable UUID productId,
            @RequestParam UUID clientId,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé"));
        }

        Map<String, Object> stockInfo = saleService.getRealtimeStockInfo(productId, clientId);
        return ResponseEntity.ok(stockInfo);
    }

    /**
     * Tableau de bord multi-caisses
     * GET /api/sales/client/{clientId}/dashboard
     */
    @GetMapping("/client/{clientId}/dashboard")
    public ResponseEntity<?> getMultiPosDashboard(
            @PathVariable UUID clientId,
            HttpServletRequest request) {

        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé"));
        }

        try {
            // Ventes en cours
            List<SaleResponseDTO> pendingSales = saleService.getPendingSales(clientId);

            // Total du jour
            BigDecimal todayTotal = saleService.getTodayTotalSales(clientId);

            // Produits populaires
            List<Object[]> topProducts = saleService.getTodayTopProducts(clientId, 5);

            // Statistiques par heure
            List<Object[]> hourlyStats = saleService.getTodayHourlySalesStats(clientId);

            Map<String, Object> dashboard = Map.of(
                    "pendingSales", pendingSales,
                    "pendingSalesCount", pendingSales.size(),
                    "todayTotal", todayTotal,
                    "topProducts", topProducts,
                    "hourlyStats", hourlyStats,
                    "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            log.error("Erreur dashboard multi-caisses", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}