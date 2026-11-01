package lifelink.repository;

import lifelink.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequester_IdOrderByCreatedAtDesc(Long userId);
    List<BloodRequest> findAllByOrderByCreatedAtDesc();
    long countByStatusIn(List<String> statuses);
}
