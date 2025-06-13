package esgi.easisell.service.interfaces;

import esgi.easisell.dto.SaleResponseDTO;
import java.util.UUID;

public interface ISaleCreationService {
    SaleResponseDTO createNewSale(UUID clientId);
}