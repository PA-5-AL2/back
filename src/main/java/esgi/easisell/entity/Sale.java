package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID saleId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    private Timestamp saleTimestamp;
    private BigDecimal totalAmount;
    private boolean isDeferred;

    // Relations
    @OneToMany(mappedBy = "sale")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SaleItem> saleItems;

    @OneToMany(mappedBy = "sale")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Payment> payments;
}