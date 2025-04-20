package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Table(name="CLIENT")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID clientId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;
    private String address;
    private String contractStatus;
    private String currencyPreference;
}