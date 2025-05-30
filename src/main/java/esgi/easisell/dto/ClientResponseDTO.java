package esgi.easisell.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    private int totalProducts;
    private int totalSuppliers;
    private int totalCategories;
}