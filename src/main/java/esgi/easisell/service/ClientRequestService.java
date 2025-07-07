package esgi.easisell.service;

import esgi.easisell.dto.*;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.ClientRequest;
import esgi.easisell.entity.User;
import esgi.easisell.exception.EmailException;
import esgi.easisell.repository.AdminUserRepository;
import esgi.easisell.repository.ClientRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientRequestService {

    private final ClientRequestRepository clientRequestRepository;
    private final EmailService emailService;
    private final AuthService authService;
    private final AdminUserRepository adminUserRepository;

    /**
     * Soumettre une nouvelle demande
     */
    @Transactional
    public ClientRequest submitRequest(ClientRequestDTO requestDTO) {

        // Vérifier si l'email n'existe pas déjà
        if (!authService.isUsernameAvailable(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email: " + requestDTO.getEmail());
        }

        // Vérifier si une demande n'existe pas déjà avec cet email
        List<ClientRequest> existingRequests = clientRequestRepository.findByEmailIgnoreCase(requestDTO.getEmail());
        boolean hasPendingRequest = existingRequests.stream()
                .anyMatch(req -> req.getStatus() == ClientRequest.RequestStatus.PENDING);

        if (hasPendingRequest) {
            throw new IllegalArgumentException("Une demande est déjà en cours de traitement pour cet email");
        }

        // Créer la demande
        ClientRequest request = ClientRequest.builder()
                .companyName(requestDTO.getCompanyName())
                .contactName(requestDTO.getContactName())
                .email(requestDTO.getEmail())
                .phoneNumber(requestDTO.getPhoneNumber())
                .address(requestDTO.getAddress())
                .message(requestDTO.getMessage())
                .status(ClientRequest.RequestStatus.PENDING)
                .build();

        ClientRequest savedRequest = clientRequestRepository.save(request);
        log.info("Nouvelle demande client créée: {} - {}", savedRequest.getCompanyName(), savedRequest.getEmail());

        // Envoyer email de confirmation au demandeur (async)
        try {
            sendConfirmationEmailToRequester(savedRequest);
        } catch (EmailException e) {
            log.error("Erreur envoi email confirmation à {}", savedRequest.getEmail(), e);
        }

        // Notifier les admins (async)
        try {
            notifyAdmins(savedRequest);
        } catch (Exception e) {
            log.error("Erreur notification admins pour {}", savedRequest.getCompanyName(), e);
        }

        return savedRequest;
    }

    /**
     * Récupérer les demandes en attente
     */
    public List<ClientRequestResponseDTO> getPendingRequests() {
        return clientRequestRepository.findByStatusOrderByRequestDateDesc(ClientRequest.RequestStatus.PENDING)
                .stream()
                .map(ClientRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer toutes les demandes
     */
    public List<ClientRequestResponseDTO> getAllRequests() {
        return clientRequestRepository.findAllByOrderByRequestDateDesc()
                .stream()
                .map(ClientRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Approuver la demande et créer le compte
     */
    @Transactional
    public Client approveRequest(UUID requestId, ApproveRequestDTO approveDTO) {
        ClientRequest request = clientRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + requestId));

        if (request.getStatus() != ClientRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée (statut: " + request.getStatus() + ")");
        }

        // Vérifier à nouveau que l'email est disponible
        if (!authService.isUsernameAvailable(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec cet email: " + request.getEmail());
        }

        // Générer un mot de passe temporaire sécurisé
        String tempPassword = generateTemporaryPassword();

        // Créer le compte client
        AuthDTO authDTO = new AuthDTO();
        authDTO.setUsername(request.getEmail());
        authDTO.setPassword(tempPassword);
        authDTO.setRole("client");
        authDTO.setName(request.getCompanyName());
        authDTO.setAddress(request.getAddress());
        authDTO.setContractStatus(approveDTO.getContractStatus());
        authDTO.setCurrencyPreference(approveDTO.getCurrencyPreference());

        User newUser = authService.registerUser(authDTO);

        // Mettre à jour la demande
        request.setStatus(ClientRequest.RequestStatus.APPROVED);
        request.setResponseDate(LocalDateTime.now());
        request.setAdminNotes(approveDTO.getAdminNotes());
        clientRequestRepository.save(request);

        log.info("Compte client créé pour: {} ({})", request.getCompanyName(), request.getEmail());

        // 📧 Envoyer email d'activation (utilise template existant pre-inscription)
        try {
            emailService.sendPreRegistrationEmail(newUser, tempPassword);
            log.info("📧 Email d'activation envoyé à {}", newUser.getUsername());
        } catch (EmailException e) {
            log.error("Erreur envoi email activation à {}", newUser.getUsername(), e);
        }

        return (Client) newUser;
    }

    /**
     * Rejeter une demande
     */
    @Transactional
    public void rejectRequest(UUID requestId, RejectRequestDTO rejectDTO) {
        ClientRequest request = clientRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + requestId));

        if (request.getStatus() != ClientRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée (statut: " + request.getStatus() + ")");
        }

        request.setStatus(ClientRequest.RequestStatus.REJECTED);
        request.setResponseDate(LocalDateTime.now());
        request.setAdminNotes(rejectDTO.getAdminNotes());
        clientRequestRepository.save(request);

        log.info("Demande rejetée: {} - Raison: {}", request.getCompanyName(), rejectDTO.getReason());

        // Optionnel: Envoyer email de rejet poli au demandeur
        try {
            sendRejectionEmailToRequester(request, rejectDTO.getReason());
        } catch (EmailException e) {
            log.error("Erreur envoi email rejet à {}", request.getEmail(), e);
        }
    }

    /**
     * Statistiques des demandes
     */
    public Map<String, Object> getRequestsStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("pending", clientRequestRepository.countByStatus(ClientRequest.RequestStatus.PENDING));
        stats.put("approved", clientRequestRepository.countByStatus(ClientRequest.RequestStatus.APPROVED));
        stats.put("rejected", clientRequestRepository.countByStatus(ClientRequest.RequestStatus.REJECTED));
        stats.put("total", clientRequestRepository.count());

        return stats;
    }

    // ========== 📧 MÉTHODES EMAIL ==========

    /**
     * Email de confirmation au demandeur
     */
    private void sendConfirmationEmailToRequester(ClientRequest request) throws EmailException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("request", request);
        variables.put("user", createUserForTemplate(request));
        variables.put("supportEmail", "info@easy-sell.net");
        variables.put("contactUrl", "https://deploy.dr8bqsixqjzkl.amplifyapp.com/contact");
        variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

        emailService.sendHtmlEmail(
                request.getEmail(),
                "Demande reçue - EasiSell",
                "emails/client/client-request-confirmation",
                variables
        );
    }

    /**
     * 📧 Notifier les admins
     */
    private void notifyAdmins(ClientRequest request) {
        String[] adminEmails = {
                "info@easy-sell.net",
                "samira@easy-sell.net"
        };

        for (String adminEmail : adminEmails) {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("request", request);
                variables.put("adminEmail", adminEmail);
                variables.put("dashboardUrl", "https://deploy.dr8bqsixqjzkl.amplifyapp.com/admin/requests");
                variables.put("approveUrl", "https://deploy.dr8bqsixqjzkl.amplifyapp.com/admin/requests/" + request.getRequestId() + "/approve");
                variables.put("contactUrl", "mailto:" + request.getEmail() + "?subject=Re: Demande EasiSell - " + request.getCompanyName());
                variables.put("rejectUrl", "https://deploy.dr8bqsixqjzkl.amplifyapp.com/admin/requests/" + request.getRequestId() + "/reject");
                variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

                emailService.sendHtmlEmail(
                        adminEmail,
                        "Nouvelle demande client: " + request.getCompanyName(),
                        "emails/admin/client-request-notification",
                        variables
                );
            } catch (EmailException e) {
                log.error("Erreur envoi email admin: {}", adminEmail, e);
            }
        }
    }

    /**
     * Email de rejet poli
     */
    private void sendRejectionEmailToRequester(ClientRequest request, String reason) throws EmailException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("request", request);
        variables.put("reason", reason);
        variables.put("supportEmail", "info@easy-sell.net");
        variables.put("logoUrl", "https://via.placeholder.com/200x80/4CAF50/FFFFFF?text=EasiSell");

        emailService.sendHtmlEmail(
                request.getEmail(),
                "Mise à jour de votre demande - EasiSell",
                "emails/client/client-request-rejection",
                variables
        );
    }

    /**
     * Créer un objet User temporaire pour compatibilité avec templates existants
     */
    private Map<String, Object> createUserForTemplate(ClientRequest request) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", request.getContactName());
        user.put("username", request.getEmail());
        user.put("createdAt", request.getRequestDate());
        return user;
    }

    /**
     * Génère un mot de passe temporaire sécurisé
     */
    private String generateTemporaryPassword() {
        String prefix = "EasiSell";
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        String suffix = "!";
        return prefix + timestamp + suffix;
    }
}