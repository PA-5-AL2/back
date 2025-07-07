package esgi.easisell.service;

import esgi.easisell.entity.Client;
import esgi.easisell.entity.AdminUser;
import esgi.easisell.entity.DeletedUser;
import esgi.easisell.entity.User;
import esgi.easisell.repository.DeletedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeletedUserService {

    private final DeletedUserRepository deletedUserRepository;

    @Transactional
    public void archiveUser(User user, String deletedBy, String reason) {
        DeletedUser.DeletedUserBuilder builder = DeletedUser.builder()
                .originalUserId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .deletedBy(deletedBy)
                .deletionReason(reason);

        if (user instanceof Client client) {
            builder.userType("CLIENT")
                    .clientName(client.getName())
                    .address(client.getAddress())
                    .contractStatus(client.getContractStatus())
                    .currencyPreference(client.getCurrencyPreference());
        } else if (user instanceof AdminUser) {
            builder.userType("ADMIN");
        }

        deletedUserRepository.save(builder.build());
    }

    public List<DeletedUser> getAllDeletedUsers() {
        return deletedUserRepository.findAll();
    }

    public List<DeletedUser> getDeletedUsersByType(String userType) {
        return deletedUserRepository.findByUserTypeOrderByDeletedAtDesc(userType);
    }

    public List<DeletedUser> getDeletedUsersByDeleter(String deletedBy) {
        return deletedUserRepository.findByDeletedByOrderByDeletedAtDesc(deletedBy);
    }
}