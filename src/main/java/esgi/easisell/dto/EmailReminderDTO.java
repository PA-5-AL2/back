package esgi.easisell.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmailReminderDTO {
    private String clientId;
    private String serviceName;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    private boolean isLate;
}