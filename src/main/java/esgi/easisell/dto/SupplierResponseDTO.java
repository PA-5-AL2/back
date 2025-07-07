package esgi.easisell.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SupplierResponseDTO {
    private UUID supplierId;
    private String name;
    private String firstName;
    private String description;
    private String contactInfo;
    private String phoneNumber;
    private UUID clientId;

    public SupplierResponseDTO(esgi.easisell.entity.Supplier supplier) {
        this.supplierId = supplier.getSupplierId();
        this.name = supplier.getName();
        this.firstName = supplier.getFirstName();
        this.description = supplier.getDescription();
        this.contactInfo = supplier.getContactInfo();
        this.phoneNumber = supplier.getPhoneNumber();
        this.clientId = supplier.getClient().getUserId();
    }
}