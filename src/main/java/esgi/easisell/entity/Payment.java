package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    @ToString.Exclude
    private Sale sale;

    private String type; // Espèces, Carte...
    private BigDecimal amount;
    private String currency; // EUR, USD...
}