package lifelink.service;

import lifelink.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByUser_Username(String username);
    List<Donor> findByArchivedFalseOrderByFullNameAsc();

    @Query("select d from Donor d left join d.user u where d.archived = false and " +
            "(:term = '' or lower(coalesce(d.fullName, '')) like lower(concat('%', :term, '%')) " +
            "or lower(coalesce(u.username, '')) like lower(concat('%', :term, '%')) " +
            "or lower(coalesce(d.bloodType, '')) like lower(concat('%', :term, '%')) " +
            "or lower(coalesce(d.contactNumber, '')) like lower(concat('%', :term, '%')))")
    List<Donor> searchActive(@Param("term") String term);
}
