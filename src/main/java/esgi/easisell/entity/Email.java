package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EMAIL")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_id")
    private UUID emailId;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String type;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EmailSend> emailSends = new ArrayList<>();
}