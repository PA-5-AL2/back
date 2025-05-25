package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "CLIENT")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@DiscriminatorValue("CLIENT")
public class Client extends User {
    private String name;

    private String address;
    @Column(nullable = false)
    private String contractStatus;
    @Column(nullable = false)
    private String currencyPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    @ToString.Exclude
    private AdminUser adminUser;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EmailSend> emailSends = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Supplier> suppliers = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Category> categories = new ArrayList<>(); // MANQUANT

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<StockItem> stockItems = new ArrayList<>(); // MANQUANT

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Sale> sales = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Promotion> promotions = new ArrayList<>(); // MANQUANT
}