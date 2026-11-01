package lifelink.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donation_records")
public class DonationRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "donor_id")
    private Donor donor;
    private LocalDate donationDate;
    private String bloodType;
    private int bags;
    private String status = "PENDING";
    private boolean inventoryApplied;
    private String note;

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public int getBags() { return bags; }
    public void setBags(int bags) { this.bags = bags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isInventoryApplied() { return inventoryApplied; }
    public void setInventoryApplied(boolean inventoryApplied) { this.inventoryApplied = inventoryApplied; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
