package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "PAYMENT")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    private String type;  // Espèces, Carte...
    private BigDecimal amount;
    private String currency;  // EUR, USD, etc.

    @ManyToOne
    @JoinColumn(name = "sale_id")
    @ToString.Exclude
    private Sale sale;
}