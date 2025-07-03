/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : EmployeeTest.java
 * @description : Tests unitaires pour l'entité Employee
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * The type Employee test.
 */
class EmployeeTest {

    private Employee employee;
    private Client client;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        employee = new Employee();

        client = new Client();
        client.setUserId(UUID.randomUUID());
        client.setName("Test Store");
    }

    /**
     * Test builder constructor.
     */
    @Test
    @DisplayName("✅ Constructeur Builder")
    void testBuilderConstructor() {
        UUID employeeId = UUID.randomUUID();
        String username = "cashier@store.com";
        String password = "hashedPassword";
        String firstName = "Jean";
        String lastName = "Dupont";
        String role = "CASHIER";
        Boolean isActive = true;

        Employee newEmployee = Employee.builder()
                .employeeId(employeeId)
                .username(username)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .client(client)
                .isActive(isActive)
                .build();

        assertEquals(employeeId, newEmployee.getEmployeeId());
        assertEquals(username, newEmployee.getUsername());
        assertEquals(password, newEmployee.getPassword());
        assertEquals(firstName, newEmployee.getFirstName());
        assertEquals(lastName, newEmployee.getLastName());
        assertEquals(role, newEmployee.getRole());
        assertEquals(client, newEmployee.getClient());
        assertEquals(isActive, newEmployee.getIsActive());
    }

    /**
     * Test employee getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters employé")
    void testEmployeeGettersSetters() {
        UUID employeeId = UUID.randomUUID();
        String username = "manager@store.com";
        String firstName = "Marie";
        String role = "MANAGER";

        employee.setEmployeeId(employeeId);
        employee.setUsername(username);
        employee.setFirstName(firstName);
        employee.setRole(role);
        employee.setClient(client);

        assertEquals(employeeId, employee.getEmployeeId());
        assertEquals(username, employee.getUsername());
        assertEquals(firstName, employee.getFirstName());
        assertEquals(role, employee.getRole());
        assertEquals(client, employee.getClient());
    }

    /**
     * Test default is active.
     */
    @Test
    @DisplayName("✅ Valeur par défaut isActive")
    void testDefaultIsActive() {
        Employee newEmployee = Employee.builder()
                .username("test@store.com")
                .password("password")
                .firstName("Test")
                .role("CASHIER")
                .client(client)
                .build();

        assertTrue(newEmployee.getIsActive());
    }

    /**
     * Test valid roles.
     */
    @Test
    @DisplayName("✅ Rôles d'employé valides")
    void testValidRoles() {
        String[] validRoles = {"CASHIER", "MANAGER", "SUPERVISOR"};

        for (String role : validRoles) {
            employee.setRole(role);
            assertEquals(role, employee.getRole());
        }
    }

    /**
     * Test client relation.
     */
    @Test
    @DisplayName("✅ Relation avec Client")
    void testClientRelation() {
        employee.setClient(client);

        assertEquals(client, employee.getClient());
        assertNotNull(employee.getClient());
    }
}