package esgi.easisell.dto;

import esgi.easisell.entity.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    private UUID userId;
    private String username;
    private String firstName;
    private String role;
    private LocalDateTime createdAt;

    private String name;
    private String address;
    private String contractStatus;
    private String currencyPreference;

    private UUID adminUserId;
    private String adminUserName;

    public ClientResponseDTO(Client client) {
        this.userId = client.getUserId();
        this.username = client.getUsername();
        this.firstName = client.getFirstName();
        this.role = client.getRole().toString();
        this.createdAt = client.getCreatedAt();

        this.name = client.getName();
        this.address = client.getAddress();
        this.contractStatus = client.getContractStatus();
        this.currencyPreference = client.getCurrencyPreference();

        if (client.getAdminUser() != null) {
            this.adminUserId = client.getAdminUser().getUserId();
            this.adminUserName = client.getAdminUser().getFirstName();
        }

    }
}