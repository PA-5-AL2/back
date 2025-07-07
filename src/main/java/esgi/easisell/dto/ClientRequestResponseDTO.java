package esgi.easisell.dto;

import esgi.easisell.entity.ClientRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestResponseDTO {
    private UUID requestId;
    private String companyName;
    private String contactName;
    private String email;
    private String phoneNumber;
    private String address;
    private String message;
    private ClientRequest.RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime responseDate;
    private String adminNotes;

    public ClientRequestResponseDTO(ClientRequest request) {
        this.requestId = request.getRequestId();
        this.companyName = request.getCompanyName();
        this.contactName = request.getContactName();
        this.email = request.getEmail();
        this.phoneNumber = request.getPhoneNumber();
        this.address = request.getAddress();
        this.message = request.getMessage();
        this.status = request.getStatus();
        this.requestDate = request.getRequestDate();
        this.responseDate = request.getResponseDate();
        this.adminNotes = request.getAdminNotes();
    }
}