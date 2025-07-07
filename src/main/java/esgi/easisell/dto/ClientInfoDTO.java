package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfoDTO {
    private UUID clientId;
    private String username;
    private String firstName;
    private String name;
    private String address;
    private String currencyPreference;
}