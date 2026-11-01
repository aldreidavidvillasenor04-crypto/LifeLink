package lifelink.repository;

import lifelink.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDonor_IdOrderByAppointmentAtDesc(Long donorId);
    List<Appointment> findByAppointmentAtAfterOrderByAppointmentAtAsc(LocalDateTime from);
    List<Appointment> findAllByOrderByAppointmentAtDesc();
    long countByStatusInAndAppointmentAtAfter(List<String> statuses, LocalDateTime from);
}
