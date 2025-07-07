package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "DELETED_USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Informations de l'utilisateur supprimé
    @Column(name = "original_user_id")
    private UUID originalUserId;

    @Column(nullable = false)
    private String username;

    private String firstName;

    @Column(nullable = false)
    private String userType; // "CLIENT" ou "ADMIN"

    // Informations spécifiques aux clients
    private String clientName;
    private String address;
    private String contractStatus;
    private String currencyPreference;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    private String deletionReason;

    @PrePersist
    protected void onCreate() {
        this.deletedAt = LocalDateTime.now();
    }
}