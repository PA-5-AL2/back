package esgi.easisell.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmailCancellationDTO {
    private String clientId;
    private String serviceName;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private String reference;
}