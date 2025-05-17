package esgi.easisell.dto;

import lombok.Data;

/**
 * DTO pour la création et la mise à jour des catégories
 */
@Data
public class CategoryDTO {
    private String name;
    private String clientId;
}