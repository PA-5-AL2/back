package esgi.easisell.service;

import esgi.easisell.dto.*;
import esgi.easisell.entity.DeferredPayment;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Customer;
import esgi.easisell.repository.CustomerRepository;
import esgi.easisell.repository.DeferredPaymentRepository;
import esgi.easisell.repository.SaleRepository;
import esgi.easisell.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeferredPaymentService {

    private final DeferredPaymentRepository deferredPaymentRepository;
    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final EmailService emailService;
    @Autowired
    private CustomerService customerService;
    private final CustomerRepository customerRepository;

    /**
     * Créer un nouveau paiement différé avec reconnaissance automatique du client
     */
    @Transactional
    public DeferredPaymentResponseDTO createDeferredPayment(DeferredPaymentCreateDTO createDTO) {
        log.info("Création d'un paiement différé pour la vente: {} - Client: {}",
                createDTO.getSaleId(), createDTO.getCustomerName());

        // Vérifier que la vente existe
        Sale sale = saleRepository.findById(createDTO.getSaleId())
                .orElseThrow(() -> new RuntimeException("Vente non trouvée: " + createDTO.getSaleId()));

        // 🔍 RECONNAISSANCE AUTOMATIQUE DU CLIENT
        Customer customer = null;
        String effectiveCustomerName = createDTO.getCustomerName();
        String effectiveCustomerPhone = createDTO.getCustomerPhone();

        if (createDTO.getCustomerName() != null && !createDTO.getCustomerName().trim().isEmpty()) {
            // Tenter de reconnaître le client
            CustomerRecognitionResponseDTO recognition = customerService.recognizeCustomer(
                    sale.getClient().getUserId(),
                    createDTO.getCustomerName(),
                    createDTO.getCustomerPhone(),
                    createDTO.getAmount()
            );

            if (recognition.isRecognized()) {
                // Client reconnu - récupérer le profil complet
                customer = customerRepository.findById(recognition.getCustomerId()).orElse(null);
                log.info("Client reconnu: {} ({})", customer.getFullName(), customer.getCustomerType());
            } else {
                // Nouveau client - créer automatiquement le profil
                customer = customerService.createCustomerFromPayment(
                        sale.getClient().getUserId(),
                        createDTO.getCustomerName(),
                        createDTO.getCustomerPhone()
                );
                log.info("Nouveau profil client créé automatiquement: {}", customer.getFullName());
            }
        }

        // Marquer la vente comme différée
        if (!sale.getIsDeferred()) {
            sale.setIsDeferred(true);
            saleRepository.save(sale);
        }

        // Créer le paiement différé avec ou sans customer
        DeferredPayment.DeferredPaymentBuilder builder = DeferredPayment.builder()
                .sale(sale)
                .client(sale.getClient())
                .amount(createDTO.getAmount())
                .dueDate(createDTO.getDueDate())
                .notes(createDTO.getNotes())
                .currency(createDTO.getCurrency() != null ? createDTO.getCurrency() : "EUR")
                .status(DeferredPayment.PaymentStatus.PENDING);

        if (customer != null) {
            // Client avec profil complet
            builder.customer(customer)
                    .customerName(customer.getFullName()) // Pour compatibilité
                    .customerPhone(customer.getPhone());
        } else {
            // Client sans profil (fallback)
            builder.customerName(effectiveCustomerName)
                    .customerPhone(effectiveCustomerPhone);
        }

        DeferredPayment deferredPayment = builder.build();
        DeferredPayment saved = deferredPaymentRepository.save(deferredPayment);

        // Mettre à jour les statistiques du customer si applicable
        if (customer != null) {
            customerService.updateCustomerAfterPurchase(customer.getCustomerId(), createDTO.getAmount());
        }

        log.info("Paiement différé créé avec succès: {} pour {}",
                saved.getDeferredPaymentId(), saved.getEffectiveCustomerName());

        return new DeferredPaymentResponseDTO(saved);
    }

// 4️⃣ AJOUTER CETTE NOUVELLE MÉTHODE pour l'API de reconnaissance :

    /**
     * Reconnaître un client avant de créer le paiement différé
     */
    public CustomerRecognitionResponseDTO recognizeCustomerForPayment(UUID clientId, String customerName, String customerPhone, BigDecimal amount) {
        log.info("Reconnaissance client: {} / {} pour montant: {}€", customerName, customerPhone, amount);

        return customerService.recognizeCustomer(clientId, customerName, customerPhone, amount);
    }

// 5️⃣ AJOUTER CETTE MÉTHODE pour les statistiques enrichies :

    /**
     * Obtenir les statistiques enrichies avec informations clients
     */
    public DeferredPaymentStatsEnrichedDTO getEnrichedDeferredPaymentStats(UUID clientId) {
        // Stats classiques
        DeferredPaymentStatsDTO classicStats = getDeferredPaymentStats(clientId);

        // Stats par type de client
        List<Object[]> statsByCustomerType = deferredPaymentRepository.getPaymentStatsByCustomerType(clientId);

        // Top clients avec paiements différés
        List<Customer> topCustomers = customerRepository.findTopCustomersByDeferredPayments(clientId);

        return DeferredPaymentStatsEnrichedDTO.builder()
                .classicStats(classicStats)
                .statsByCustomerType(statsByCustomerType)
                .topCustomersWithDeferredPayments(topCustomers.stream()
                        .map(CustomerResponseDTO::new)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Récupérer tous les paiements différés d'un client
     */
    public List<DeferredPaymentResponseDTO> getDeferredPaymentsByClient(UUID clientId) {
        return deferredPaymentRepository.findByClientUserIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(DeferredPaymentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements en retard
     */
    public List<DeferredPaymentResponseDTO> getOverduePayments(UUID clientId) {
        return deferredPaymentRepository.findOverduePayments(clientId, LocalDate.now())
                .stream()
                .map(DeferredPaymentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un paiement différé
     */
    @Transactional
    public DeferredPaymentResponseDTO updateDeferredPayment(UUID paymentId, DeferredPaymentUpdateDTO updateDTO) {
        log.info("Mise à jour du paiement différé: {}", paymentId);

        DeferredPayment payment = deferredPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement différé non trouvé: " + paymentId));

        // Mettre à jour les champs modifiables
        if (updateDTO.getCustomerName() != null) {
            payment.setCustomerName(updateDTO.getCustomerName());
        }
        if (updateDTO.getCustomerPhone() != null) {
            payment.setCustomerPhone(updateDTO.getCustomerPhone());
        }
        if (updateDTO.getDueDate() != null) {
            payment.setDueDate(updateDTO.getDueDate());
        }
        if (updateDTO.getNotes() != null) {
            payment.setNotes(updateDTO.getNotes());
        }

        DeferredPayment updated = deferredPaymentRepository.save(payment);
        return new DeferredPaymentResponseDTO(updated);
    }

    /**
     * Enregistrer un paiement partiel ou total
     */
    @Transactional
    public DeferredPaymentResponseDTO recordPayment(UUID paymentId, BigDecimal amountReceived) {
        log.info("Enregistrement d'un paiement de {} pour le paiement différé: {}", amountReceived, paymentId);

        DeferredPayment payment = deferredPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement différé non trouvé: " + paymentId));

        // Vérifier que le montant est valide
        if (amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant doit être supérieur à zéro");
        }

        BigDecimal newAmountPaid = payment.getAmountPaid().add(amountReceived);

        // Vérifier qu'on ne dépasse pas le montant total
        if (newAmountPaid.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Le montant total payé ne peut pas dépasser le montant dû");
        }

        payment.setAmountPaid(newAmountPaid);

        // Mettre à jour le statut
        if (payment.isFullyPaid()) {
            payment.setStatus(DeferredPayment.PaymentStatus.PAID);
        } else {
            payment.setStatus(DeferredPayment.PaymentStatus.PARTIAL);
        }

        DeferredPayment updated = deferredPaymentRepository.save(payment);
        log.info("Paiement enregistré. Nouveau statut: {}, Montant payé: {}/{}",
                updated.getStatus(), updated.getAmountPaid(), updated.getAmount());

        return new DeferredPaymentResponseDTO(updated);
    }

    /**
     * Supprimer un paiement différé
     */
    @Transactional
    public void deleteDeferredPayment(UUID paymentId) {
        log.info("Suppression du paiement différé: {}", paymentId);

        DeferredPayment payment = deferredPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement différé non trouvé: " + paymentId));

        // Remettre la vente en mode normal si c'était le seul paiement différé
        Sale sale = payment.getSale();
        List<DeferredPayment> otherPayments = deferredPaymentRepository.findBySaleSaleId(sale.getSaleId());
        if (otherPayments.size() == 1) { // Le seul paiement est celui qu'on supprime
            sale.setIsDeferred(false);
            saleRepository.save(sale);
        }

        deferredPaymentRepository.delete(payment);
        log.info("Paiement différé supprimé avec succès");
    }

    /**
     * Rechercher des paiements différés
     */
    public List<DeferredPaymentResponseDTO> searchDeferredPayments(UUID clientId, String searchTerm) {
        return deferredPaymentRepository.searchByCustomerName(clientId, searchTerm)
                .stream()
                .map(DeferredPaymentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques des paiements différés
     */
    public DeferredPaymentStatsDTO getDeferredPaymentStats(UUID clientId) {
        List<Object[]> stats = deferredPaymentRepository.getPaymentStatsByStatus(clientId);
        Long overdueCount = deferredPaymentRepository.countOverduePayments(clientId, LocalDate.now());
        BigDecimal totalPending = deferredPaymentRepository.getTotalPendingAmount(clientId);

        return DeferredPaymentStatsDTO.builder()
                .totalPendingAmount(totalPending)
                .overdueCount(overdueCount.intValue())
                .statusStats(stats)
                .build();
    }

    /**
     * Envoyer des rappels de paiement
     */
    @Transactional
    public void sendPaymentReminders(UUID clientId) {
        log.info("Envoi des rappels de paiement pour le client: {}", clientId);

        // Seuil de rappel : pas de rappel depuis 7 jours
        Timestamp reminderThreshold = Timestamp.valueOf(LocalDateTime.now().minusDays(7));

        List<DeferredPayment> paymentsNeedingReminder = deferredPaymentRepository
                .findPaymentsNeedingReminder(clientId, LocalDate.now(), reminderThreshold);

        for (DeferredPayment payment : paymentsNeedingReminder) {
            try {
                // Envoyer l'email de rappel
                emailService.sendPaymentReminder(
                        payment.getClient(),
                        "Paiement différé - " + payment.getCustomerName(),
                        payment.getRemainingAmount(),
                        payment.getCurrency(),
                        payment.getDueDate(),
                        payment.isOverdue()
                );

                // Mettre à jour les informations de rappel
                payment.setLastReminderSent(Timestamp.valueOf(LocalDateTime.now()));
                payment.setReminderCount(payment.getReminderCount() + 1);
                deferredPaymentRepository.save(payment);

                log.info("Rappel envoyé pour le paiement: {}", payment.getDeferredPaymentId());

            } catch (Exception e) {
                log.error("Erreur lors de l'envoi du rappel pour le paiement: {}",
                        payment.getDeferredPaymentId(), e);
            }
        }

        log.info("Envoi des rappels terminé. {} rappels envoyés", paymentsNeedingReminder.size());
    }

    /**
     * Mettre à jour automatiquement les statuts des paiements en retard
     */
    @Transactional
    public void updateOverduePayments() {
        log.info("Mise à jour des statuts des paiements en retard");

        List<DeferredPayment> allPayments = deferredPaymentRepository.findAll();
        int updatedCount = 0;

        for (DeferredPayment payment : allPayments) {
            if (payment.isOverdue() && payment.getStatus() == DeferredPayment.PaymentStatus.PENDING) {
                payment.setStatus(DeferredPayment.PaymentStatus.OVERDUE);
                deferredPaymentRepository.save(payment);
                updatedCount++;
            }
        }

        log.info("Statuts mis à jour pour {} paiements en retard", updatedCount);
    }
}