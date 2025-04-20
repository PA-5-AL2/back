package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="ADMIN_USER")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID adminUserId;

    @Column(unique = true)
    private String email;

    private String password;
    private String firstname;
    private String lastName;
    private String userName;
    private LocalDateTime createdAt;
}