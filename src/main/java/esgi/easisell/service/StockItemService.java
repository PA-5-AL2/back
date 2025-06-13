package esgi.easisell.service;

import esgi.easisell.dto.CreateStockItemDTO;
import esgi.easisell.dto.UpdateStockItemDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Product;
import esgi.easisell.entity.StockItem;
import esgi.easisell.entity.Supplier;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.ProductRepository;
import esgi.easisell.repository.StockItemRepository;
import esgi.easisell.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockItemService {

    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;

    @Transactional
    public StockItem createStockItem(CreateStockItemDTO dto) {
        log.info("Création d'un nouvel item de stock pour le produit ID: {}", dto.getProductId());

        Product product = productRepository.findById(UUID.fromString(dto.getProductId()))
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Client client = clientRepository.findById(UUID.fromString(dto.getClientId()))
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        StockItem stockItem = new StockItem();
        stockItem.setProduct(product);
        stockItem.setClient(client);
        stockItem.setQuantity(dto.getQuantity());
        stockItem.setReorderThreshold(dto.getReorderThreshold());
        stockItem.setPurchasePrice(dto.getPurchasePrice());

        if (dto.getPurchaseDate() != null) {
            stockItem.setPurchaseDate(Timestamp.valueOf(dto.getPurchaseDate()));
        }

        if (dto.getExpirationDate() != null) {
            stockItem.setExpirationDate(Timestamp.valueOf(dto.getExpirationDate()));
        }

        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(UUID.fromString(dto.getSupplierId()))
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
            stockItem.setSupplier(supplier);
        }

        return stockItemRepository.save(stockItem);
    }

    public List<StockItem> getStockItemsByClient(UUID clientId) {
        return stockItemRepository.findByClientUserId(clientId);
    }

    public Optional<StockItem> getStockItemById(UUID stockItemId) {
        return stockItemRepository.findById(stockItemId);
    }

    public List<StockItem> getStockItemsByProduct(UUID productId) {
        return stockItemRepository.findByProductProductId(productId);
    }

    @Transactional
    public Optional<StockItem> updateStockItem(UUID stockItemId, UpdateStockItemDTO dto) {
        log.info("Mise à jour de l'item de stock ID: {}", stockItemId);

        return stockItemRepository.findById(stockItemId)
                .map(stockItem -> {
                    if (dto.getQuantity() != null) {
                        stockItem.setQuantity(dto.getQuantity());
                    }
                    if (dto.getReorderThreshold() != null) {
                        stockItem.setReorderThreshold(dto.getReorderThreshold());
                    }
                    if (dto.getPurchasePrice() != null) {
                        stockItem.setPurchasePrice(dto.getPurchasePrice());
                    }
                    if (dto.getPurchaseDate() != null) {
                        stockItem.setPurchaseDate(Timestamp.valueOf(dto.getPurchaseDate()));
                    }
                    if (dto.getExpirationDate() != null) {
                        stockItem.setExpirationDate(Timestamp.valueOf(dto.getExpirationDate()));
                    }
                    if (dto.getSupplierId() != null) {
                        Supplier supplier = supplierRepository.findById(UUID.fromString(dto.getSupplierId()))
                                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
                        stockItem.setSupplier(supplier);
                    }

                    return stockItemRepository.save(stockItem);
                });
    }

    @Transactional
    public boolean adjustStockQuantity(UUID productId, int quantityChange) {
        log.info("Ajustement du stock pour le produit: {} de {}", productId, quantityChange);

        // Récupérer tous les items de stock pour ce produit
        List<StockItem> stockItems = stockItemRepository.findByProductProductId(productId);

        if (stockItems.isEmpty()) {
            log.error("Aucun stock trouvé pour le produit: {}", productId);
            return false;
        }

        int remainingToDecrease = Math.abs(quantityChange);

        // Si on diminue le stock
        if (quantityChange < 0) {
            for (StockItem item : stockItems) {
                if (remainingToDecrease <= 0) break;

                if (item.getQuantity() > 0) {
                    int toDecrease = Math.min(item.getQuantity(), remainingToDecrease);
                    item.setQuantity(item.getQuantity() - toDecrease);
                    stockItemRepository.save(item);
                    remainingToDecrease -= toDecrease;
                }
            }

            return remainingToDecrease == 0;
        }
        // Si on augmente le stock
        else {
            // Ajouter au premier item trouvé
            StockItem firstItem = stockItems.get(0);
            firstItem.setQuantity(firstItem.getQuantity() + quantityChange);
            stockItemRepository.save(firstItem);
            return true;
        }
    }

    @Transactional
    public boolean deleteStockItem(UUID stockItemId) {
        if (stockItemRepository.existsById(stockItemId)) {
            stockItemRepository.deleteById(stockItemId);
            return true;
        }
        return false;
    }

    public List<StockItem> getLowStockItems(UUID clientId) {
        return stockItemRepository.findLowStockItems(clientId);
    }

    public List<StockItem> getExpiringItems(UUID clientId, int daysUntilExpiration) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(daysUntilExpiration);
        return stockItemRepository.findExpiringItems(clientId, Timestamp.valueOf(futureDate));
    }

    public int getTotalStockQuantityByProduct(UUID clientId, UUID productId) {
        return stockItemRepository.getTotalStockQuantityByProduct(clientId, productId);
    }

    public List<StockItem> searchStockItemsByProductName(UUID clientId, String productName) {
        return stockItemRepository.findByClientUserIdAndProductNameContainingIgnoreCase(clientId, productName);
    }

    public List<StockItem> findStockByProductBarcode(UUID clientId, String barcode) {
        Product product = productRepository.findByClientAndBarcode(clientId, barcode);
        if (product != null) {
            return stockItemRepository.findByProductProductId(product.getProductId());
        }
        return List.of();
    }

    public boolean hasBarcode(UUID productId) {
        return productRepository.findById(productId)
                .map(product -> product.getBarcode() != null && !product.getBarcode().trim().isEmpty())
                .orElse(false);
    }
}