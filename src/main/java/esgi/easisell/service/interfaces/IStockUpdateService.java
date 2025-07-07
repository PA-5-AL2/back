package esgi.easisell.service.interfaces;

import esgi.easisell.entity.Sale;

public interface IStockUpdateService {
    void decreaseStockForSale(Sale sale);
}