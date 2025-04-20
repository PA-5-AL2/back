package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "EMAIL_SEND")
public class EmailSend {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID emailSendId;

    private Timestamp sentAt;
    private String status;  // SUCCÈS/ÉCHEC

    @ManyToOne
    @JoinColumn(name = "email_id")
    @ToString.Exclude
    private Email email;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;
}