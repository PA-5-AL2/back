package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="ADMIN_USER")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_user_id")
    private UUID adminUserId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String userName;

    @OneToMany(mappedBy = "adminUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Client> managedClients;
}
