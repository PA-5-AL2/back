package esgi.easisell.service;

import esgi.easisell.dto.UpdateClientDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService extends UserService<Client, UpdateClientDTO> {

    private final ClientRepository clientRepository;

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
}