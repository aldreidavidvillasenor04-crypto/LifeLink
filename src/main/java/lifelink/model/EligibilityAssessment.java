package lifelink.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "eligibility_assessments")
public class EligibilityAssessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "donor_id")
    private Donor donor;
    private int age;
    private double weightKg;
    private boolean feelingWell;
    private boolean donatedBefore;
    private LocalDate lastDonationDate;
    private boolean currentIllness;
    private boolean takingMedication;
    private boolean recentSurgery;
    private boolean recentTattooOrPiercing;
    private boolean medicalCondition;
    private boolean previouslyDeferred;
    private boolean recentVaccination;
    private boolean recentDentalProcedure;
    private boolean recentRiskTravel;
    private boolean priorTransfusionOrTransplant;
    private boolean pregnantOrRecentlyGaveBirth;
    private String resultStatus;
    private int readinessScore;
    @Column(length = 1500)
    private String resultReasons;
    private LocalDate nextEligibleDate;
    private LocalDateTime submittedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public boolean isFeelingWell() { return feelingWell; }
    public void setFeelingWell(boolean feelingWell) { this.feelingWell = feelingWell; }
    public boolean isDonatedBefore() { return donatedBefore; }
    public void setDonatedBefore(boolean donatedBefore) { this.donatedBefore = donatedBefore; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public boolean isCurrentIllness() { return currentIllness; }
    public void setCurrentIllness(boolean value) { currentIllness = value; }
    public boolean isTakingMedication() { return takingMedication; }
    public void setTakingMedication(boolean value) { takingMedication = value; }
    public boolean isRecentSurgery() { return recentSurgery; }
    public void setRecentSurgery(boolean value) { recentSurgery = value; }
    public boolean isRecentTattooOrPiercing() { return recentTattooOrPiercing; }
    public void setRecentTattooOrPiercing(boolean value) { recentTattooOrPiercing = value; }
    public boolean isMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(boolean value) { medicalCondition = value; }
    public boolean isPreviouslyDeferred() { return previouslyDeferred; }
    public void setPreviouslyDeferred(boolean value) { previouslyDeferred = value; }
    public boolean isRecentVaccination() { return recentVaccination; }
    public void setRecentVaccination(boolean value) { recentVaccination = value; }
    public boolean isRecentDentalProcedure() { return recentDentalProcedure; }
    public void setRecentDentalProcedure(boolean value) { recentDentalProcedure = value; }
    public boolean isRecentRiskTravel() { return recentRiskTravel; }
    public void setRecentRiskTravel(boolean value) { recentRiskTravel = value; }
    public boolean isPriorTransfusionOrTransplant() { return priorTransfusionOrTransplant; }
    public void setPriorTransfusionOrTransplant(boolean value) { priorTransfusionOrTransplant = value; }
    public boolean isPregnantOrRecentlyGaveBirth() { return pregnantOrRecentlyGaveBirth; }
    public void setPregnantOrRecentlyGaveBirth(boolean value) { pregnantOrRecentlyGaveBirth = value; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public int getReadinessScore() { return readinessScore; }
    public void setReadinessScore(int readinessScore) { this.readinessScore = readinessScore; }
    public String getResultReasons() { return resultReasons; }
    public void setResultReasons(String resultReasons) { this.resultReasons = resultReasons; }
    public LocalDate getNextEligibleDate() { return nextEligibleDate; }
    public void setNextEligibleDate(LocalDate nextEligibleDate) { this.nextEligibleDate = nextEligibleDate; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
