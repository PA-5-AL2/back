package esgi.easisell.service;

import esgi.easisell.dto.AdminChangePasswordDTO;
import esgi.easisell.dto.ChangePasswordDTO;
import esgi.easisell.dto.UpdateClientDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.ClientRepository;
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

    @Override
    protected JpaRepository<Client, UUID> getRepository() {
        return clientRepository;
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
}