package esgi.easisell.event;

import esgi.easisell.entity.Sale;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SaleCompletedEvent extends ApplicationEvent {
    private final Sale sale;

    public SaleCompletedEvent(Sale sale) {
        super(sale);
        this.sale = sale;
    }
}