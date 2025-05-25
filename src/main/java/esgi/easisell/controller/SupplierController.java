package esgi.easisell.controller;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.entity.Supplier;
import esgi.easisell.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/clients/{clientId}/suppliers")
    public ResponseEntity<List<Supplier>> getSuppliersByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok(supplierService.getSuppliersByClientId(clientId));
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable UUID id) {
        return supplierService.getSupplierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clients/{clientId}/suppliers")
    public ResponseEntity<Supplier> createSupplier(
            @PathVariable UUID clientId,
            @RequestBody SupplierDTO supplierDTO) {
        return supplierService.createSupplier(clientId, supplierDTO)
                .map(supplier -> {
                    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(supplier.getSupplierId())
                            .toUri();
                    return ResponseEntity.created(location).body(supplier);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable UUID id,
            @RequestBody SupplierDTO supplierDTO) {
        return supplierService.updateSupplier(id, supplierDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        if (supplierService.getSupplierById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
