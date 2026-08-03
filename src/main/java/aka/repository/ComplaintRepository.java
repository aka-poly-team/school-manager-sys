package aka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    List<Complaint> findByAttendanceTeacherIdOrderByIdDesc(Integer teacherId);
    long countByAttendanceTeacherIdAndStatus(Integer teacherId, Integer status);
    List<Complaint> findAllByOrderByIdDesc();
    long countByStatus(Integer status);
}
