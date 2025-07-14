package esgi.easisell.service;

import esgi.easisell.dto.DeferredPaymentCreateDTO;
import esgi.easisell.dto.DeferredPaymentResponseDTO;
import esgi.easisell.dto.DeferredPaymentUpdateDTO;
import esgi.easisell.dto.DeferredPaymentStatsDTO;
import esgi.easisell.entity.DeferredPayment;
import esgi.easisell.entity.Sale;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.DeferredPaymentRepository;
import esgi.easisell.repository.SaleRepository;
import esgi.easisell.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Créer un nouveau paiement différé
     */
    @Transactional
    public DeferredPaymentResponseDTO createDeferredPayment(DeferredPaymentCreateDTO createDTO) {
        log.info("Création d'un paiement différé pour la vente: {}", createDTO.getSaleId());

        // Vérifier que la vente existe
        Sale sale = saleRepository.findById(createDTO.getSaleId())
                .orElseThrow(() -> new RuntimeException("Vente non trouvée: " + createDTO.getSaleId()));

        // Marquer la vente comme différée si pas déjà fait
        if (!sale.getIsDeferred()) {
            sale.setIsDeferred(true);
            saleRepository.save(sale);
        }

        // Créer le paiement différé
        DeferredPayment deferredPayment = DeferredPayment.builder()
                .sale(sale)
                .client(sale.getClient())
                .customerName(createDTO.getCustomerName())
                .customerPhone(createDTO.getCustomerPhone())
                .amount(createDTO.getAmount())
                .dueDate(createDTO.getDueDate())
                .notes(createDTO.getNotes())
                .currency(createDTO.getCurrency() != null ? createDTO.getCurrency() : "EUR")
                .status(DeferredPayment.PaymentStatus.PENDING)
                .build();

        DeferredPayment saved = deferredPaymentRepository.save(deferredPayment);
        log.info("Paiement différé créé avec succès: {}", saved.getDeferredPaymentId());

        return new DeferredPaymentResponseDTO(saved);
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