package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreateDTO {

    @NotBlank(message = "Le prénom est requis")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String lastName;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;

    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    private String email;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String address;

    @DecimalMin(value = "0", message = "Le montant maximum doit être positif")
    @DecimalMax(value = "9999.99", message = "Le montant maximum ne peut pas dépasser 9999.99")
    private BigDecimal maxDeferredAmount = BigDecimal.valueOf(100);

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    private String preferredPaymentMethod;
    private Boolean allowsDeferredPayment = true;
}