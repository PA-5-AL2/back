package esgi.easisell.dto;

import lombok.Data;
@Data
public class AuthDTO {
    private String username;
    private String password;
    private String role;
    private String contractStatus;
    private String currencyPreference;
    private String name;
    private String address;
}