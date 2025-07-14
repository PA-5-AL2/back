package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferredPaymentCreateDTO {

    @NotNull(message = "L'ID de la vente est requis")
    private UUID saleId;

    @NotBlank(message = "Le nom du client est requis")
    @Size(max = 255, message = "Le nom du client ne peut pas dépasser 255 caractères")
    private String customerName;

    @Size(max = 20, message = "Le numéro de téléphone ne peut pas dépasser 20 caractères")
    private String customerPhone;

    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Digits(integer = 8, fraction = 2, message = "Format du montant invalide")
    private BigDecimal amount;

    @NotNull(message = "La date d'échéance est requise")
    @Future(message = "La date d'échéance doit être dans le futur")
    private LocalDate dueDate;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    @Size(max = 3, message = "La devise doit faire 3 caractères")
    private String currency = "EUR";
}