package lifelink.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blood_inventory")
public class BloodInventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String bloodType;
    @Column(nullable = false)
    private int bags;

    public Long getId() { return id; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public int getBags() { return bags; }
    public void setBags(int bags) { this.bags = bags; }
}
