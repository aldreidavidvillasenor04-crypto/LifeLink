package lifelink.repository;

import lifelink.model.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
    List<DonationRecord> findByDonor_IdOrderByDonationDateDesc(Long donorId);
    List<DonationRecord> findAllByOrderByDonationDateDesc();
    long countByStatus(String status);
    @Query("select coalesce(sum(d.bags),0) from DonationRecord d where d.status = :status")
    long sumBagsByStatus(@Param("status") String status);
    @Query("select coalesce(sum(d.bags),0) from DonationRecord d where d.status = :status and d.donationDate between :from and :to")
    long sumBagsByStatusAndDonationDateBetween(@Param("status") String status, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
