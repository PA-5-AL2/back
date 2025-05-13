package esgi.easisell.dto;

import lombok.Data;

@Data
public class UpdateClientDTO {
    private String name;
    private String address;
    private String contractStatus;
    private String currencyPreference;
}
