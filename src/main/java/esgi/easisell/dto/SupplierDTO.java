package esgi.easisell.dto;

import esgi.easisell.entity.Supplier;
import lombok.Data;
import java.util.UUID;

@Data
public class SupplierDTO {
    private UUID supplierId;
    private String name;
    private String firstName;
    private String description;
    private String contactInfo;
    private String phoneNumber;

    public SupplierDTO(Supplier supplier) {
        this.supplierId = supplier.getSupplierId();
        this.name = supplier.getName();
        this.firstName = supplier.getFirstName();
        this.description = supplier.getDescription();
        this.contactInfo = supplier.getContactInfo();
        this.phoneNumber = supplier.getPhoneNumber();
    }

    public SupplierDTO() {}
}