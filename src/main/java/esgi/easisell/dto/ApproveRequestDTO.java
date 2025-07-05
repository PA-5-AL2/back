package esgi.easisell.dto;

import lombok.Data;

@Data
public class ApproveRequestDTO {
    private String currencyPreference = "EUR";
    private String contractStatus = "ACTIVE";
    private String adminNotes;
}