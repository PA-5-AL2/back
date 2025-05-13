package esgi.easisell.service;

import esgi.easisell.dto.UpdateAdminDTO;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService extends UserService<AdminUser, UpdateAdminDTO> {

    private final AdminUserRepository adminUserRepository;

    @Override
    protected JpaRepository<AdminUser, UUID> getRepository() {
        return adminUserRepository;
    }

    @Override
    public Optional<AdminUser> updateUser(UUID id, UpdateAdminDTO dto) {
        return adminUserRepository.findById(id)
                .map(admin -> {
                    if (dto.getFirstName() != null) admin.setFirstName(dto.getFirstName());
                    if (dto.getUsername() != null) {
                        if (adminUserRepository.findByUsername(dto.getUsername()) == null
                                || adminUserRepository.findByUsername(dto.getUsername()).getUserId().equals(id)) {
                            admin.setUsername(dto.getUsername());
                        } else {
                            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                        }
                    }

                    return adminUserRepository.save(admin);
                });
    }
}