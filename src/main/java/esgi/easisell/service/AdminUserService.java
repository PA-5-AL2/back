package esgi.easisell.service;

import esgi.easisell.dto.AdminChangePasswordDTO;
import esgi.easisell.dto.ChangePasswordDTO;
import esgi.easisell.dto.UpdateAdminDTO;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService extends UserService<AdminUser, UpdateAdminDTO> {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

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
    @Override
    public Optional<AdminUser> changePassword(UUID id, ChangePasswordDTO dto) {
        return adminUserRepository.findById(id)
                .map(admin -> {
                    if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPassword())) {
                        throw new IllegalArgumentException("Mot de passe actuel incorrect");
                    }
                    admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    return adminUserRepository.save(admin);
                });
    }
    @Override
    public Optional<AdminUser> adminChangePassword(UUID id, AdminChangePasswordDTO dto) {
        return adminUserRepository.findById(id)
                .map(admin -> {
                    // Un admin peut changer le mot de passe d'un autre admin
                    admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    return adminUserRepository.save(admin);
                });
    }
}