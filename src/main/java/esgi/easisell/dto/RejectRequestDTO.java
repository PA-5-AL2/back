package esgi.easisell.dto;

import lombok.Data;

@Data
public class RejectRequestDTO {
    private String reason;
    private String adminNotes;
}