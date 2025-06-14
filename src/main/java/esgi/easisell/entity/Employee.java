package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "EMPLOYEE")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(nullable = false)
    private String username; // email ou identifiant

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String role; // CASHIER, MANAGER, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client; // La supérette pour laquelle il travaille

    private Boolean isActive = true;
}