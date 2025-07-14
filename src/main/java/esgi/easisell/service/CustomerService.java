package esgi.easisell.service;

import esgi.easisell.dto.*;
import esgi.easisell.entity.Customer;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.CustomerRepository;
import esgi.easisell.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ClientRepository clientRepository;

    /**
     * 🔍 FONCTION CLÉ : Reconnaître un client pour le paiement différé
     */
    public CustomerRecognitionResponseDTO recognizeCustomer(UUID clientId, String fullName, String phone, BigDecimal requestedAmount) {
        log.info("Reconnaissance client pour: {} / {} dans la boutique: {}", fullName, phone, clientId);

        // Rechercher par nom ET téléphone en priorité
        List<Customer> candidates = customerRepository.findByNameOrPhone(clientId, fullName, phone);

        if (!candidates.isEmpty()) {
            Customer customer = candidates.get(0); // Prendre le plus récent
            log.info("Client reconnu: {} (Type: {}, Trust: {})",
                    customer.getFullName(), customer.getCustomerType(), customer.getTrustLevel());
            return new CustomerRecognitionResponseDTO(customer, requestedAmount);
        }

        // Client non reconnu
        log.info("Client non reconnu: {}", fullName);
        return CustomerRecognitionResponseDTO.forUnknownCustomer(fullName, phone);
    }

    /**
     * 🆕 Créer automatiquement un profil client lors du premier paiement différé
     */
    @Transactional
    public Customer createCustomerFromPayment(UUID clientId, String fullName, String phone) {
        log.info("Création automatique du profil client: {} / {}", fullName, phone);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client (boutique) non trouvé: " + clientId));

        // Séparer prénom et nom (approximatif)
        String[] nameParts = fullName.trim().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        Customer customer = Customer.builder()
                .client(client)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .customerType(Customer.CustomerType.NOUVEAU)
                .trustLevel(1)
                .totalPurchasesCount(0)
                .totalAmountSpent(BigDecimal.ZERO)
                .loyaltyPoints(0)
                .maxDeferredAmount(BigDecimal.valueOf(50)) // Limite par défaut pour nouveaux
                .allowsDeferredPayment(true)
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Profil client créé automatiquement: {}", saved.getCustomerId());
        return saved;
    }

    /**
     * 📊 Mettre à jour les statistiques d'un client après un achat
     */
    @Transactional
    public void updateCustomerAfterPurchase(UUID customerId, BigDecimal amount) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.updateAfterPurchase(amount);
            customerRepository.save(customer);
            log.info("Statistiques mises à jour pour: {} (Total: {} achats, {} €)",
                    customer.getFullName(), customer.getTotalPurchasesCount(), customer.getTotalAmountSpent());
        }
    }

    /**
     * 👥 Récupérer tous les clients d'une boutique
     */
    public List<CustomerResponseDTO> getAllCustomers(UUID clientId) {
        return customerRepository.findByClientUserIdOrderByLastVisitDateDesc(clientId)
                .stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ⭐ Récupérer les clients VIP
     */
    public List<CustomerResponseDTO> getVipCustomers(UUID clientId) {
        return customerRepository.findVipCustomers(clientId)
                .stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 🚨 Récupérer les clients à risque
     */
    public List<CustomerResponseDTO> getRiskyCustomers(UUID clientId) {
        return customerRepository.findRiskyCustomers(clientId)
                .stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 🔍 Rechercher des clients
     */
    public List<CustomerResponseDTO> searchCustomers(UUID clientId, String searchTerm) {
        return customerRepository.searchCustomers(clientId, searchTerm)
                .stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 📝 Créer un nouveau client manuellement
     */
    @Transactional
    public CustomerResponseDTO createCustomer(UUID clientId, CustomerCreateDTO createDTO) {
        log.info("Création manuelle d'un client: {} {}", createDTO.getFirstName(), createDTO.getLastName());

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client (boutique) non trouvé: " + clientId));

        Customer customer = Customer.builder()
                .client(client)
                .firstName(createDTO.getFirstName())
                .lastName(createDTO.getLastName())
                .phone(createDTO.getPhone())
                .email(createDTO.getEmail())
                .address(createDTO.getAddress())
                .maxDeferredAmount(createDTO.getMaxDeferredAmount())
                .notes(createDTO.getNotes())
                .preferredPaymentMethod(createDTO.getPreferredPaymentMethod())
                .allowsDeferredPayment(createDTO.getAllowsDeferredPayment())
                .customerType(Customer.CustomerType.NOUVEAU)
                .trustLevel(1)
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        Customer saved = customerRepository.save(customer);
        return new CustomerResponseDTO(saved);
    }

    /**
     * ✏️ Modifier un client existant
     */
    @Transactional
    public CustomerResponseDTO updateCustomer(UUID customerId, CustomerCreateDTO updateDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + customerId));

        customer.setFirstName(updateDTO.getFirstName());
        customer.setLastName(updateDTO.getLastName());
        customer.setPhone(updateDTO.getPhone());
        customer.setEmail(updateDTO.getEmail());
        customer.setAddress(updateDTO.getAddress());
        customer.setMaxDeferredAmount(updateDTO.getMaxDeferredAmount());
        customer.setNotes(updateDTO.getNotes());
        customer.setPreferredPaymentMethod(updateDTO.getPreferredPaymentMethod());
        customer.setAllowsDeferredPayment(updateDTO.getAllowsDeferredPayment());

        Customer saved = customerRepository.save(customer);
        return new CustomerResponseDTO(saved);
    }

    /**
     * 🚫 Blacklister un client
     */
    @Transactional
    public void blacklistCustomer(UUID customerId, String reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + customerId));

        customer.setCustomerType(Customer.CustomerType.BLACKLIST);
        customer.setStatus(Customer.CustomerStatus.BLACKLISTED);
        customer.setAllowsDeferredPayment(false);
        customer.setNotes(customer.getNotes() + "\n[BLACKLISTÉ] " + reason);

        customerRepository.save(customer);
        log.warn("Client blacklisté: {} - Raison: {}", customer.getFullName(), reason);
    }

    /**
     * ✅ Réhabiliter un client blacklisté
     */
    @Transactional
    public void rehabilitateCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + customerId));

        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setAllowsDeferredPayment(true);
        // Recalculer le type basé sur l'historique
        customer.setCustomerType(customer.calculateCustomerType());

        customerRepository.save(customer);
        log.info("Client réhabilité: {}", customer.getFullName());
    }

    /**
     * 📊 Statistiques globales des clients
     */
    public CustomerStatsDTO getCustomerStats(UUID clientId) {
        List<Object[]> statsByType = customerRepository.getCustomerStatsByType(clientId);

        int totalCustomers = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Object[] stat : statsByType) {
            totalCustomers += ((Long) stat[1]).intValue();
            totalSpent = totalSpent.add((BigDecimal) stat[2]);
        }

        return CustomerStatsDTO.builder()
                .totalCustomers(totalCustomers)
                .totalAmountSpent(totalSpent)
                .statsByType(statsByType)
                .build();
    }

    /**
     * 🔍 Obtenir un client par ID
     */
    public CustomerResponseDTO getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + customerId));
        return new CustomerResponseDTO(customer);
    }
}
