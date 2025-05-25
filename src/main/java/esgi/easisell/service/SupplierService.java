package esgi.easisell.service;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Supplier;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ClientRepository clientRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getSuppliersByClientId(UUID clientId) {
        return supplierRepository.findByClientUserId(clientId);
    }

    public Optional<Supplier> getSupplierById(UUID id) {
        return supplierRepository.findById(id);
    }

    @Transactional
    public Optional<Supplier> createSupplier(UUID clientId, SupplierDTO dto) {
        return clientRepository.findById(clientId)
                .map(client -> {
                    Supplier supplier = new Supplier();
                    supplier.setName(dto.getName());
                    supplier.setFirstName(dto.getFirstName());
                    supplier.setDescription(dto.getDescription());
                    supplier.setContactInfo(dto.getContactInfo());
                    supplier.setPhoneNumber(dto.getPhoneNumber());
                    supplier.setClient(client);
                    supplier.setStockItems(new ArrayList<>());
                    return supplierRepository.save(supplier);
                });
    }

    @Transactional
    public Optional<Supplier> updateSupplier(UUID id, SupplierDTO dto) {
        return supplierRepository.findById(id)
                .map(supplier -> {
                    if (dto.getName() != null) supplier.setName(dto.getName());
                    if (dto.getFirstName() != null) supplier.setFirstName(dto.getFirstName());
                    if (dto.getDescription() != null) supplier.setDescription(dto.getDescription());
                    if (dto.getContactInfo() != null) supplier.setContactInfo(dto.getContactInfo());
                    if (dto.getPhoneNumber() != null) supplier.setPhoneNumber(dto.getPhoneNumber());
                    return supplierRepository.save(supplier);
                });
    }

    @Transactional
    public void deleteSupplier(UUID id) {
        supplierRepository.deleteById(id);
    }

    @Transactional
    public boolean isSupplierOwnedByClient(UUID supplierId, UUID clientId) {
        return supplierRepository.findById(supplierId)
                .map(supplier -> supplier.getClient().getUserId().equals(clientId))
                .orElse(false);
    }
}