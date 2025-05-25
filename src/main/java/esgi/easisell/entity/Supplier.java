package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SUPPLIER")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(nullable = false)
    private String name;

    private String firstName;

    private String description;

    private String contactInfo;

    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<StockItem> stockItems;
}