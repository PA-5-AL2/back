package esgi.easisell.dto;

import lombok.Data;

/**
 * DTO fournisseurs
 */
@Data
public class SupplierDTO {
    private String name;
    private String contactInfo;
    private String clientId;
}