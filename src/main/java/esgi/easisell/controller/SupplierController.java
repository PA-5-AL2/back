package esgi.easisell.controller;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.entity.Supplier;
import esgi.easisell.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Créer un nouveau fournisseur
     */
    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        try {
            Supplier supplier = supplierService.createSupplier(supplierDTO);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            log.error("Erreur lors de la création du fournisseur", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer tous les fournisseurs
     */
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    /**
     * Récupérer les fournisseurs d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Supplier>> getSuppliersByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(supplierService.getSuppliersByClient(clientId));
    }

    /**
     * Récupérer un fournisseur par ID
     */
    @GetMapping("/{supplierId}")
    public ResponseEntity<?> getSupplierById(@PathVariable UUID supplierId) {
        return supplierService.getSupplierById(supplierId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour un fournisseur
     */
    @PutMapping("/{supplierId}")
    public ResponseEntity<?> updateSupplier(@PathVariable UUID supplierId,
                                            @RequestBody SupplierDTO supplierDTO) {
        try {
            return supplierService.updateSupplier(supplierId, supplierDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du fournisseur", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer un fournisseur
     */
    @DeleteMapping("/{supplierId}")
    public ResponseEntity<?> deleteSupplier(@PathVariable UUID supplierId) {
        boolean deleted = supplierService.deleteSupplier(supplierId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Fournisseur supprimé avec succès"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rechercher des fournisseurs par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliersByName(
            @RequestParam UUID clientId,
            @RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchSuppliersByName(clientId, name));
    }
}