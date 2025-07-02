package esgi.easisell.dto;

import lombok.Data;

@Data
public class ActivationDTO {
    private String token;
    private String password;
}