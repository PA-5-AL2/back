package esgi.easisell.service.interfaces;

import esgi.easisell.dto.*;
import java.util.List;
import java.util.UUID;

public interface ISaleQueryService {
    SaleDetailsDTO getSaleDetails(UUID saleId);
    SaleTotalDTO calculateSaleTotal(UUID saleId);
    List<SaleResponseDTO> getSalesByClient(UUID clientId, int page, int size);
    List<SaleResponseDTO> getTodaySales(UUID clientId);
    ReceiptDTO generateReceiptData(UUID saleId);
}