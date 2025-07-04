package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EMAIL_SEND")
public class EmailSend {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_send_id")
    private UUID emailSendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    @ToString.Exclude
    private Email email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @Column(nullable = false)
    private Timestamp sentAt;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String emailType;
}