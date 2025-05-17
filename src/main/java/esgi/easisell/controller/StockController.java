package esgi.easisell.controller;

import esgi.easisell.dto.CreateStockItemDTO;
import esgi.easisell.dto.StockItemResponseDTO;
import esgi.easisell.dto.UpdateStockItemDTO;
import esgi.easisell.entity.StockItem;
import esgi.easisell.service.StockItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final StockItemService stockItemService;

    @PostMapping
    public ResponseEntity<?> createStockItem(@RequestBody CreateStockItemDTO createStockItemDTO) {
        try {
            StockItem stockItem = stockItemService.createStockItem(createStockItemDTO);
            return ResponseEntity.ok(convertToResponseDTO(stockItem));
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'item de stock", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<StockItemResponseDTO>> getStockItemsByClient(@PathVariable UUID clientId) {
        List<StockItem> stockItems = stockItemService.getStockItemsByClient(clientId);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{stockItemId}")
    public ResponseEntity<?> getStockItemById(@PathVariable UUID stockItemId) {
        return stockItemService.getStockItemById(stockItemId)
                .map(stockItem -> ResponseEntity.ok(convertToResponseDTO(stockItem)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockItemResponseDTO>> getStockItemsByProduct(@PathVariable UUID productId) {
        List<StockItem> stockItems = stockItemService.getStockItemsByProduct(productId);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{stockItemId}")
    public ResponseEntity<?> updateStockItem(@PathVariable UUID stockItemId,
                                             @RequestBody UpdateStockItemDTO updateStockItemDTO) {
        return stockItemService.updateStockItem(stockItemId, updateStockItemDTO)
                .map(stockItem -> ResponseEntity.ok(convertToResponseDTO(stockItem)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{stockItemId}/adjust-quantity")
    public ResponseEntity<?> adjustStockQuantity(@PathVariable UUID stockItemId,
                                                 @RequestParam int quantityChange) {
        boolean success = stockItemService.adjustStockQuantity(stockItemId, quantityChange);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Quantité ajustée avec succès"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Impossible d'ajuster la quantité"));
        }
    }

    @DeleteMapping("/{stockItemId}")
    public ResponseEntity<?> deleteStockItem(@PathVariable UUID stockItemId) {
        boolean deleted = stockItemService.deleteStockItem(stockItemId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Item de stock supprimé avec succès"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/client/{clientId}/low-stock")
    public ResponseEntity<List<StockItemResponseDTO>> getLowStockItems(@PathVariable UUID clientId) {
        List<StockItem> stockItems = stockItemService.getLowStockItems(clientId);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/client/{clientId}/expiring")
    public ResponseEntity<List<StockItemResponseDTO>> getExpiringItems(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "7") int daysUntilExpiration) {
        List<StockItem> stockItems = stockItemService.getExpiringItems(clientId, daysUntilExpiration);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/client/{clientId}/product/{productId}/total-quantity")
    public ResponseEntity<Map<String, Integer>> getTotalStockQuantity(
            @PathVariable UUID clientId,
            @PathVariable UUID productId) {
        int totalQuantity = stockItemService.getTotalStockQuantityByProduct(clientId, productId);
        return ResponseEntity.ok(Map.of("totalQuantity", totalQuantity));
    }

    @GetMapping("/client/{clientId}/search")
    public ResponseEntity<List<StockItemResponseDTO>> searchStockItems(
            @PathVariable UUID clientId,
            @RequestParam String productName) {
        List<StockItem> stockItems = stockItemService.searchStockItemsByProductName(clientId, productName);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/client/{clientId}/barcode/{barcode}")
    public ResponseEntity<List<StockItemResponseDTO>> findStockByBarcode(
            @PathVariable UUID clientId,
            @PathVariable String barcode) {
        List<StockItem> stockItems = stockItemService.findStockByProductBarcode(clientId, barcode);
        List<StockItemResponseDTO> responseDTOs = stockItems.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    private StockItemResponseDTO convertToResponseDTO(StockItem stockItem) {
        StockItemResponseDTO dto = new StockItemResponseDTO();
        dto.setStockItemId(stockItem.getStockItemId().toString());
        dto.setProductId(stockItem.getProduct().getProductId().toString());
        dto.setProductName(stockItem.getProduct().getName());
        dto.setProductBarcode(stockItem.getProduct().getBarcode()); // Peut être null
        dto.setHasBarcode(stockItem.getProduct().getBarcode() != null &&
                !stockItem.getProduct().getBarcode().trim().isEmpty());
        dto.setClientId(stockItem.getClient().getUserId().toString()); // Corrigé : getUserId()
        dto.setQuantity(stockItem.getQuantity());
        dto.setReorderThreshold(stockItem.getReorderThreshold());
        dto.setPurchaseDate(stockItem.getPurchaseDate());
        dto.setExpirationDate(stockItem.getExpirationDate());
        dto.setPurchasePrice(stockItem.getPurchasePrice());

        if (stockItem.getSupplier() != null) {
            dto.setSupplierId(stockItem.getSupplier().getSupplierId().toString());
            dto.setSupplierName(stockItem.getSupplier().getName());
        }

        // Calculer les indicateurs
        dto.setLowStock(stockItem.getReorderThreshold() != null &&
                stockItem.getQuantity() <= stockItem.getReorderThreshold());

        // Vérifier si l'item expire bientôt (7 jours par défaut)
        if (stockItem.getExpirationDate() != null) {
            LocalDateTime expirationTime = stockItem.getExpirationDate().toLocalDateTime();
            LocalDateTime nowPlus7Days = LocalDateTime.now().plusDays(7);
            dto.setExpiringSoon(expirationTime.isBefore(nowPlus7Days));
        }

        return dto;
    }
}