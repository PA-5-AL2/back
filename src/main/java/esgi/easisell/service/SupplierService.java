package esgi.easisell.service;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Supplier;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ClientRepository clientRepository;

    /**
     * Créer un nouveau fournisseur
     */
    @Transactional
    public Supplier createSupplier(SupplierDTO supplierDTO) {
        log.info("Création d'un nouveau fournisseur : {}", supplierDTO.getName());

        Client client = clientRepository.findById(UUID.fromString(supplierDTO.getClientId()))
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + supplierDTO.getClientId()));

        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setContactInfo(supplierDTO.getContactInfo());
        supplier.setClient(client);

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Fournisseur créé avec succès. ID: {}", savedSupplier.getSupplierId());

        return savedSupplier;
    }

    /**
     * Récupérer tous les fournisseurs
     */
    public List<Supplier> getAllSuppliers() {
        log.info("Récupération de tous les fournisseurs");
        return supplierRepository.findAll();
    }

    /**
     * Récupérer les fournisseurs d'un client
     */
    public List<Supplier> getSuppliersByClient(UUID clientId) {
        log.info("Récupération des fournisseurs pour le client ID: {}", clientId);
        return supplierRepository.findByClientUserId(clientId);
    }

    /**
     * Récupérer un fournisseur par ID
     */
    public Optional<Supplier> getSupplierById(UUID supplierId) {
        log.info("Récupération du fournisseur ID: {}", supplierId);
        return supplierRepository.findById(supplierId);
    }

    /**
     * Mettre à jour un fournisseur
     */
    @Transactional
    public Optional<Supplier> updateSupplier(UUID supplierId, SupplierDTO supplierDTO) {
        log.info("Mise à jour du fournisseur ID: {}", supplierId);

        return supplierRepository.findById(supplierId)
                .map(supplier -> {
                    if (supplierDTO.getName() != null) {
                        supplier.setName(supplierDTO.getName());
                    }
                    if (supplierDTO.getContactInfo() != null) {
                        supplier.setContactInfo(supplierDTO.getContactInfo());
                    }

                    Supplier updated = supplierRepository.save(supplier);
                    log.info("Fournisseur mis à jour avec succès. ID: {}", supplierId);
                    return updated;
                });
    }

    /**
     * Supprimer un fournisseur
     */
    @Transactional
    public boolean deleteSupplier(UUID supplierId) {
        log.info("Suppression du fournisseur ID: {}", supplierId);

        if (supplierRepository.existsById(supplierId)) {
            supplierRepository.deleteById(supplierId);
            log.info("Fournisseur supprimé avec succès. ID: {}", supplierId);
            return true;
        }

        log.warn("Fournisseur non trouvé pour la suppression. ID: {}", supplierId);
        return false;
    }

    /**
     * Rechercher des fournisseurs par nom
     */
    public List<Supplier> searchSuppliersByName(UUID clientId, String name) {
        log.info("Recherche de fournisseurs contenant '{}' pour le client ID: {}", name, clientId);
        return supplierRepository.findByClientUserIdAndNameContainingIgnoreCase(clientId, name);
    }
}