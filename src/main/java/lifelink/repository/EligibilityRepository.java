package lifelink.repository;

import lifelink.model.EligibilityAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EligibilityRepository extends JpaRepository<EligibilityAssessment, Long> {
    Optional<EligibilityAssessment> findFirstByDonor_IdOrderBySubmittedAtDesc(Long donorId);
    List<EligibilityAssessment> findAllByOrderBySubmittedAtDesc();
}
