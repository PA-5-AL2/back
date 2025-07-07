package esgi.easisell.service;

import esgi.easisell.dto.AdminChangePasswordDTO;
import esgi.easisell.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import esgi.easisell.dto.ChangePasswordDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class UserService<T extends User, D> {

    protected abstract JpaRepository<T, UUID> getRepository();

    public List<T> getAllUsers() {
        return getRepository().findAll();
    }

    public Optional<T> getUserById(UUID id) {
        return getRepository().findById(id);
    }

    @Transactional
    public void deleteUser(UUID id) {
        getRepository().deleteById(id);
    }

    @Transactional
    public abstract Optional<T> updateUser(UUID id, D dto);

    @Transactional
    public abstract Optional<T> changePassword(UUID id, ChangePasswordDTO dto);
    @Transactional
    public abstract Optional<T> adminChangePassword(UUID id, AdminChangePasswordDTO dto);
}
