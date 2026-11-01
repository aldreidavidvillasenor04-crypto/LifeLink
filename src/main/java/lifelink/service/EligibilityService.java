package lifelink.service;

import lifelink.model.EligibilityAssessment;
import lifelink.repository.EligibilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {
    private final EligibilityRepository repository;

    public EligibilityService(EligibilityRepository repository) { this.repository = repository; }

    @Transactional
    public EligibilityAssessment evaluate(EligibilityAssessment a, String gender) {
        List<String> hardReasons = new ArrayList<>();
        List<String> screeningReasons = new ArrayList<>();
        int applicable = 3;
        int passed = 0;

        if (a.isFeelingWell()) passed++; else hardReasons.add("You are not currently feeling well.");
        if (a.getAge() >= 18) passed++; else hardReasons.add("You must be at least 18 years old.");
        if (a.getWeightKg() >= 50) passed++; else hardReasons.add("You must weigh at least 50 kg.");

        if (a.isDonatedBefore()) {
            applicable++;
            if (a.getLastDonationDate() == null) {
                hardReasons.add("Add the date of your most recent donation.");
            } else if (!a.getLastDonationDate().plusDays(90).isAfter(LocalDate.now())) {
                passed++;
            } else {
                hardReasons.add("The 90-day waiting period after your last donation has not passed.");
                a.setNextEligibleDate(a.getLastDonationDate().plusDays(90));
            }
        }

        boolean pregnancyQuestionApplies = "FEMALE".equalsIgnoreCase(gender);
        boolean[] flags = {a.isCurrentIllness(), a.isTakingMedication(), a.isRecentSurgery(),
                a.isRecentTattooOrPiercing(), a.isMedicalCondition(), a.isPreviouslyDeferred(),
                a.isRecentVaccination(), a.isRecentDentalProcedure(), a.isRecentRiskTravel(),
                a.isPriorTransfusionOrTransplant()};
        String[] reasons = {"Current illness requires further screening.", "Medication use requires further screening.",
                "Recent surgery or hospitalization requires further screening.", "A recent tattoo or piercing requires further screening.",
                "A reported medical condition requires further screening.", "Previous medical advice requires further screening.",
                "A recent vaccination requires further screening.", "A recent dental procedure requires further screening.",
                "Recent travel or exposure requires further screening.", "A prior transfusion or transplant requires further screening."};
        applicable += flags.length;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) screeningReasons.add(reasons[i]); else passed++;
        }
        if (pregnancyQuestionApplies) {
            applicable++;
            if (a.isPregnantOrRecentlyGaveBirth()) screeningReasons.add("Pregnancy or recent childbirth requires further screening.");
            else passed++;
        }

        a.setReadinessScore(applicable == 0 ? 0 : Math.round((passed * 100f) / applicable));
        if (!hardReasons.isEmpty()) {
            a.setResultStatus("NOT ELIGIBLE");
            a.setResultReasons(String.join(" ", hardReasons));
        } else if (!screeningReasons.isEmpty()) {
            a.setResultStatus("IN PROCESS");
            a.setResultReasons(String.join(" ", screeningReasons));
        } else {
            a.setResultStatus("ELIGIBLE");
            a.setResultReasons("You passed all applicable preliminary eligibility checks.");
        }
        return repository.save(a);
    }
}
