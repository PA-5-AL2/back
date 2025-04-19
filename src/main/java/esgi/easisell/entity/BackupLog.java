package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BackupLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID backupId;

    private Timestamp date;
    private String status; // SUCCÈS/ÉCHEC

    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;
}