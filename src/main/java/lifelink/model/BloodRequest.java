package lifelink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "requester_user_id")
    private User requester;
    private String requesterName;
    private String requesterPhone;
    private String requesterEmail;
    private String relationshipToPatient;
    private String patientName;
    private Integer patientAge;
    private String patientBloodType;
    private Boolean patientAdmitted;
    private String hospitalName;
    private String hospitalLocation;
    private String wardNumber;
    private String requestedBloodType;
    private int bagsNeeded;
    private LocalDateTime neededAt;
    private String urgency;
    private String reason;
    private String hospitalConfirmation;
    private String transfusionStatus;
    private Boolean supportingDocumentProvided;
    private String supportingDocumentName;
    @Lob private byte[] supportingDocument;
    private String specificBloodTypeRequired;
    private String directedDonorsRequired;
    private Boolean hasDeadline;
    private LocalDateTime deadlineAt;
    private Boolean informationConfirmed;
    private Boolean dataUseAuthorized;
    private String status = "PENDING";
    private String adminNote;
    private boolean inventoryDeducted;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String value) { requesterName = value; }
    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String value) { requesterPhone = value; }
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String value) { requesterEmail = value; }
    public String getRelationshipToPatient() { return relationshipToPatient; }
    public void setRelationshipToPatient(String value) { relationshipToPatient = value; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String value) { patientName = value; }
    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer value) { patientAge = value; }
    public String getPatientBloodType() { return patientBloodType; }
    public void setPatientBloodType(String value) { patientBloodType = value; }
    public Boolean getPatientAdmitted() { return patientAdmitted; }
    public void setPatientAdmitted(Boolean value) { patientAdmitted = value; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String value) { hospitalName = value; }
    public String getHospitalLocation() { return hospitalLocation; }
    public void setHospitalLocation(String value) { hospitalLocation = value; }
    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String value) { wardNumber = value; }
    public String getRequestedBloodType() { return requestedBloodType; }
    public void setRequestedBloodType(String value) { requestedBloodType = value; }
    public int getBagsNeeded() { return bagsNeeded; }
    public void setBagsNeeded(int value) { bagsNeeded = value; }
    public LocalDateTime getNeededAt() { return neededAt; }
    public void setNeededAt(LocalDateTime value) { neededAt = value; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String value) { urgency = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public String getHospitalConfirmation() { return hospitalConfirmation; }
    public void setHospitalConfirmation(String value) { hospitalConfirmation = value; }
    public String getTransfusionStatus() { return transfusionStatus; }
    public void setTransfusionStatus(String value) { transfusionStatus = value; }
    public Boolean getSupportingDocumentProvided() { return supportingDocumentProvided; }
    public void setSupportingDocumentProvided(Boolean value) { supportingDocumentProvided = value; }
    public String getSupportingDocumentName() { return supportingDocumentName; }
    public void setSupportingDocumentName(String value) { supportingDocumentName = value; }
    public byte[] getSupportingDocument() { return supportingDocument; }
    public void setSupportingDocument(byte[] value) { supportingDocument = value; }
    public String getSpecificBloodTypeRequired() { return specificBloodTypeRequired; }
    public void setSpecificBloodTypeRequired(String value) { specificBloodTypeRequired = value; }
    public String getDirectedDonorsRequired() { return directedDonorsRequired; }
    public void setDirectedDonorsRequired(String value) { directedDonorsRequired = value; }
    public Boolean getHasDeadline() { return hasDeadline; }
    public void setHasDeadline(Boolean value) { hasDeadline = value; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime value) { deadlineAt = value; }
    public Boolean getInformationConfirmed() { return informationConfirmed; }
    public void setInformationConfirmed(Boolean value) { informationConfirmed = value; }
    public Boolean getDataUseAuthorized() { return dataUseAuthorized; }
    public void setDataUseAuthorized(Boolean value) { dataUseAuthorized = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String value) { adminNote = value; }
    public boolean isInventoryDeducted() { return inventoryDeducted; }
    public void setInventoryDeducted(boolean value) { inventoryDeducted = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
