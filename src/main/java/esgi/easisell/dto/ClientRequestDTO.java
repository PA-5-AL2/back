package esgi.easisell.dto;

import lombok.Data;

@Data
public class ClientRequestDTO {
    private String companyName;
    private String contactName;
    private String email;
    private String phoneNumber;
    private String address;
    private String message;
}