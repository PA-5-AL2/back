package esgi.easisell.repository;

import esgi.easisell.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    // Rechercher les items d'une vente
    List<SaleItem> findBySaleSaleId(UUID saleId);

    // Rechercher les items par produit
    List<SaleItem> findByProductProductId(UUID productId);

    // Quantité totale vendue d'un produit
    @Query("SELECT COALESCE(SUM(si.quantitySold), 0) FROM SaleItem si " +
            "JOIN si.sale s " +
            "WHERE si.product.productId = :productId " +
            "AND s.client.userId = :clientId")
    Integer getTotalQuantitySoldByProduct(@Param("productId") UUID productId,
                                          @Param("clientId") UUID clientId);
}