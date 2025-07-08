package esgi.easisell.service;

import esgi.easisell.dto.AdminChangePasswordDTO;
import esgi.easisell.dto.ChangePasswordDTO;
import esgi.easisell.dto.UpdateClientDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService extends UserService<Client, UpdateClientDTO> {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeletedUserService deletedUserService;

    @Override
    protected JpaRepository<Client, UUID> getRepository() {
        return clientRepository;
    }
    @Override
    @Transactional
    public void deleteUserWithArchive(UUID id, String deletedBy, String reason) {
        clientRepository.findById(id).ifPresent(client -> {
            deletedUserService.archiveUser(client, deletedBy, reason);
            clientRepository.deleteById(id);
        });
    }

    @Override
    public Optional<Client> updateUser(UUID id, UpdateClientDTO dto) {
        return clientRepository.findById(id)
                .map(client -> {
                    if (dto.getName() != null) client.setName(dto.getName());
                    if (dto.getAddress() != null) client.setAddress(dto.getAddress());
                    if (dto.getContractStatus() != null) client.setContractStatus(dto.getContractStatus());
                    if (dto.getCurrencyPreference() != null) client.setCurrencyPreference(dto.getCurrencyPreference());
                    return clientRepository.save(client);
                });
    }
    @Override
    public Optional<Client> changePassword(UUID id, ChangePasswordDTO dto) {
        return clientRepository.findById(id)
                .map(client -> {

                    if (!passwordEncoder.matches(dto.getCurrentPassword(), client.getPassword())) {
                        throw new IllegalArgumentException("Mot de passe actuel incorrect");
                    }

                    client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    return clientRepository.save(client);
                });
    }
    @Override
    public Optional<Client> adminChangePassword(UUID id, AdminChangePasswordDTO dto) {
        return clientRepository.findById(id)
                .map(client -> {
                    // L'admin peut changer sans vérifier l'ancien mot de passe
                    client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    return clientRepository.save(client);
                });
    }
    @Transactional
    public Optional<Client> regenerateAccessCode(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(client -> {
                    client.setAccessCode(generateRandomCode());
                    return clientRepository.save(client);
                });
    }

    public boolean verifyAccessCode(UUID clientId, String accessCode) {
        return clientRepository.findById(clientId)
                .map(client -> client.getAccessCode().equals(accessCode))
                .orElse(false);
    }

    public Optional<String> getAccessCode(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(Client::getAccessCode);
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Transactional
    public Optional<Client> setCustomAccessCode(UUID clientId, String customCode) {
        return clientRepository.findById(clientId)
                .map(client -> {
                    client.setAccessCode(customCode.toUpperCase());
                    return clientRepository.save(client);
                });
    }

}