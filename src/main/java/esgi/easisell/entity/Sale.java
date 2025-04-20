package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "SALE")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID saleId;

    private Timestamp saleTimestamp;
    private BigDecimal totalAmount;
    private boolean isDeferred;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;
}