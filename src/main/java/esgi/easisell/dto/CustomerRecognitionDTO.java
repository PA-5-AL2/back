package esgi.easisell.dto;

import esgi.easisell.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 DTO pour reconnaître/analyser un client
// ═══════════════════════════════════════════════════════════════════════════════
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRecognitionDTO {

    @NotBlank(message = "Le nom du client est requis")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String fullName;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;

    private UUID clientId; // ID de la boutique
}