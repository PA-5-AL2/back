package esgi.easisell.controller;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.dto.SupplierResponseDTO;
import esgi.easisell.entity.Supplier;
import esgi.easisell.service.SupplierService;
import esgi.easisell.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;
    private final SecurityUtils securityUtils;

    /**
     * 1. Récupérer les fournisseurs d'un client
     * GET /api/clients/{clientId}/suppliers
     */
    @GetMapping("/clients/{clientId}/suppliers")
    public ResponseEntity<?> getSuppliersByClientId(@PathVariable UUID clientId, HttpServletRequest request) {
        // 🛡️ SÉCURITÉ : Vérifier l'accès
        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        List<SupplierResponseDTO> suppliers = supplierService.getSuppliersByClientId(clientId);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * 2. Récupérer un fournisseur par ID
     * GET /api/suppliers/{id}
     */
    @GetMapping("/suppliers/{id}")
    public ResponseEntity<?> getSupplierById(@PathVariable UUID id, HttpServletRequest request) {
        Optional<Supplier> supplierOpt = supplierService.getSupplierById(id);

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Supplier supplier = supplierOpt.get();

        // 🛡️ SÉCURITÉ : Vérifier l'accès
        if (!securityUtils.canAccessClientData(supplier.getClient().getUserId(), request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce fournisseur"));
        }

        // ✅ RETOURNER LE DTO
        return ResponseEntity.ok(new SupplierResponseDTO(supplier));
    }

    /**
     * 3. Créer un fournisseur
     * POST /api/clients/{clientId}/suppliers
     */
    @PostMapping("/clients/{clientId}/suppliers")
    public ResponseEntity<?> createSupplier(
            @PathVariable UUID clientId,
            @RequestBody SupplierDTO supplierDTO,
            HttpServletRequest request) {

        // 🛡️ SÉCURITÉ : Vérifier l'accès
        if (!securityUtils.canAccessClientData(clientId, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce client"));
        }

        try {
            Optional<Supplier> supplierOpt = supplierService.createSupplier(clientId, supplierDTO);

            if (supplierOpt.isPresent()) {
                Supplier supplier = supplierOpt.get();
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(supplier.getSupplierId())
                        .toUri();
                // ✅ RETOURNER LE DTO
                return ResponseEntity.created(location).body(new SupplierResponseDTO(supplier));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Client non trouvé"));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création du fournisseur", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 4. Mettre à jour un fournisseur
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/suppliers/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable UUID id,
                                            @RequestBody SupplierDTO supplierDTO,
                                            HttpServletRequest request) {
        // Vérifier que le fournisseur existe d'abord
        Optional<Supplier> supplierOpt = supplierService.getSupplierById(id);

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Supplier supplier = supplierOpt.get();

        // 🛡️ SÉCURITÉ : Vérifier l'accès
        if (!securityUtils.canAccessClientData(supplier.getClient().getUserId(), request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce fournisseur"));
        }

        // Mettre à jour
        Optional<SupplierResponseDTO> updatedSupplierOpt = supplierService.updateSupplier(id, supplierDTO);

        if (updatedSupplierOpt.isPresent()) {
            return ResponseEntity.ok(updatedSupplierOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 5. Supprimer un fournisseur
     * DELETE /api/suppliers/{id}
     */
    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable UUID id, HttpServletRequest request) {
        Optional<Supplier> supplierOpt = supplierService.getSupplierById(id);

        if (supplierOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Supplier supplier = supplierOpt.get();

        // 🛡️ SÉCURITÉ : Vérifier l'accès
        if (!securityUtils.canAccessClientData(supplier.getClient().getUserId(), request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé à ce fournisseur"));
        }

        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}