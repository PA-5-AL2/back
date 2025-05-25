package esgi.easisell.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private UUID categoryId;
    private String name;
    private UUID clientId;
    private String clientName;
    private int productCount;
}