package esgi.easisell.dto;

import lombok.Data;

/**
 * DTO pour les demandes de réinitialisation de mot de passe
 */
@Data
public class PasswordResetRequestDTO {
    private String email;
}