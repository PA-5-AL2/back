package esgi.easisell.controller;

import esgi.easisell.dto.ClientResponseDTO;
import esgi.easisell.dto.UpdateAdminDTO;
import esgi.easisell.dto.UpdateClientDTO;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.Client;
import esgi.easisell.service.AdminUserService;
import esgi.easisell.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ClientService clientService;
    private final AdminUserService adminUserService;

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
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        if (!clientService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        clientService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id) {
        if (!adminUserService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        adminUserService.deleteUser(id);
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
}