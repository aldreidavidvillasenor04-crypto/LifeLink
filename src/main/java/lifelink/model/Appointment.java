package lifelink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "donor_id")
    private Donor donor;
    private String hospitalName;
    private String city;
    private String hospitalType;
    private LocalDateTime appointmentAt;
    private String status = "PENDING";
    private String adminNote;
    private Long previousAppointmentId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }
    public LocalDateTime getAppointmentAt() { return appointmentAt; }
    public void setAppointmentAt(LocalDateTime appointmentAt) { this.appointmentAt = appointmentAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public Long getPreviousAppointmentId() { return previousAppointmentId; }
    public void setPreviousAppointmentId(Long previousAppointmentId) { this.previousAppointmentId = previousAppointmentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
