package esgi.easisell.dto;

import lombok.Data;

@Data
public class EmailPreInscriptionDTO {
    private String clientId;
    private String activationToken;
}
