package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID adminUserId;

    @Column(unique = true)
    private String email;

    private String passwordHash;
    private String name;

    @OneToMany(mappedBy = "adminUser")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Client> managedClients;
}