package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "EMAIL")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID emailId;

    private String subject;
    private String content;
    private String type;  // Type (Rappel, Promotion)
    @CreationTimestamp
    private LocalDateTime createdAt;
}