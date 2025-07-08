package esgi.easisell.controller;

import esgi.easisell.dto.*;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.DeletedUser;
import esgi.easisell.service.AdminUserService;
import esgi.easisell.service.ClientService;
import esgi.easisell.service.DeletedUserService;
import esgi.easisell.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final ClientService clientService;
    private final AdminUserService adminUserService;
    private final DeletedUserService deletedUserService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        List<Client> clients = clientService.getAllUsers();

        List<ClientResponseDTO> clientDTOs = clients.stream()
                .map(ClientResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientDTOs);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable UUID id) {
        return clientService.getUserById(id)
                .map(ClientResponseDTO::new)  // Conversion en DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminUser>> getAllAdmins() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<AdminUser> getAdminById(@PathVariable UUID id) {
        return adminUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id,
                                             @RequestParam(required = false) String reason,
                                             Authentication authentication) {
        if (!clientService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        String deletedBy = authentication.getName();
        clientService.deleteUserWithArchive(id, deletedBy, reason != null ? reason : "Suppression manuelle");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id,
                                            @RequestParam(required = false) String reason,
                                            Authentication authentication) {
        if (!adminUserService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        String deletedBy = authentication.getName();
        adminUserService.deleteUserWithArchive(id, deletedBy, reason != null ? reason : "Suppression manuelle");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable UUID id, @RequestBody UpdateClientDTO dto) {
        return clientService.updateUser(id, dto)
                .map(ClientResponseDTO::new)  // Conversion en DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<AdminUser> updateAdmin(@PathVariable UUID id, @RequestBody UpdateAdminDTO dto) {
        return adminUserService.updateUser(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/clients/{id}/password")
    public ResponseEntity<String> changeClientPassword(@PathVariable UUID id, @RequestBody ChangePasswordDTO dto) {
        try {
            return clientService.changePassword(id, dto)
                    .map(client -> ResponseEntity.ok("Mot de passe modifié avec succès"))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/admins/{id}/password")
    public ResponseEntity<String> changeAdminPassword(@PathVariable UUID id, @RequestBody ChangePasswordDTO dto) {
        try {
            return adminUserService.changePassword(id, dto)
                    .map(admin -> ResponseEntity.ok("Mot de passe modifié avec succès"))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/admin/clients/{id}/password")
    public ResponseEntity<String> adminChangeClientPassword(@PathVariable UUID id, @RequestBody AdminChangePasswordDTO dto) {
        try {
            Optional<Client> clientOpt = clientService.adminChangePassword(id, dto);

            if (clientOpt.isPresent()) {
                // 📧 Envoyer email de notification à l'utilisateur
                try {
                    passwordResetService.notifyPasswordChanged(clientOpt.get().getUsername());
                    log.info("📧 Email de confirmation envoyé au client: {}", clientOpt.get().getUsername());
                } catch (Exception emailError) {
                    log.error("❌ Erreur envoi email confirmation changement mot de passe", emailError);
                    // On continue même si l'email échoue
                }

                return ResponseEntity.ok("Mot de passe du client modifié avec succès et email de confirmation envoyé");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la modification du mot de passe client", e);
            return ResponseEntity.badRequest().body("Erreur lors de la modification");
        }
    }

    @PutMapping("/admin/admins/{id}/password")
    public ResponseEntity<String> adminChangeAdminPassword(@PathVariable UUID id, @RequestBody AdminChangePasswordDTO dto) {
        try {
            Optional<AdminUser> adminOpt = adminUserService.adminChangePassword(id, dto);

            if (adminOpt.isPresent()) {
                // 📧 Envoyer email de notification à l'utilisateur
                try {
                    passwordResetService.notifyPasswordChanged(adminOpt.get().getUsername());
                    log.info("📧 Email de confirmation envoyé à l'admin: {}", adminOpt.get().getUsername());
                } catch (Exception emailError) {
                    log.error("❌ Erreur envoi email confirmation changement mot de passe", emailError);
                    // On continue même si l'email échoue
                }

                return ResponseEntity.ok("Mot de passe de l'admin modifié avec succès et email de confirmation envoyé");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la modification du mot de passe admin", e);
            return ResponseEntity.badRequest().body("Erreur lors de la modification");
        }
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<DeletedUser>> getAllDeletedUsers() {
        return ResponseEntity.ok(deletedUserService.getAllDeletedUsers());
    }

    @GetMapping("/deleted/clients")
    public ResponseEntity<List<DeletedUser>> getDeletedClients() {
        return ResponseEntity.ok(deletedUserService.getDeletedUsersByType("CLIENT"));
    }

    @GetMapping("/deleted/admins")
    public ResponseEntity<List<DeletedUser>> getDeletedAdmins() {
        return ResponseEntity.ok(deletedUserService.getDeletedUsersByType("ADMIN"));
    }

    @GetMapping("/deleted/by/{username}")
    public ResponseEntity<List<DeletedUser>> getDeletedUsersByDeleter(@PathVariable String username) {
        return ResponseEntity.ok(deletedUserService.getDeletedUsersByDeleter(username));
    }
    @GetMapping("/clients/{id}/access-code")
    public ResponseEntity<AccessCodeDTO> getClientAccessCode(@PathVariable UUID id) {
        return clientService.getAccessCode(id)
                .map(code -> {
                    AccessCodeDTO dto = new AccessCodeDTO();
                    dto.setAccessCode(code);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/clients/{id}/access-code/regenerate")
    public ResponseEntity<AccessCodeDTO> regenerateClientAccessCode(@PathVariable UUID id) {
        return clientService.regenerateAccessCode(id)
                .map(client -> {
                    AccessCodeDTO dto = new AccessCodeDTO();
                    dto.setAccessCode(client.getAccessCode());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clients/{id}/verify-access-code")
    public ResponseEntity<Map<String, Boolean>> verifyAccessCode(
            @PathVariable UUID id,
            @RequestBody AccessCodeDTO dto) {
        boolean isValid = clientService.verifyAccessCode(id, dto.getAccessCode());
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
    // Client définit son propre code d'accès
    @PutMapping("/clients/{id}/access-code/custom")
    public ResponseEntity<AccessCodeDTO> setCustomAccessCode(
            @PathVariable UUID id,
            @RequestBody AccessCodeDTO dto) {
        return clientService.setCustomAccessCode(id, dto.getAccessCode())
                .map(client -> {
                    AccessCodeDTO response = new AccessCodeDTO();
                    response.setAccessCode(client.getAccessCode());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}