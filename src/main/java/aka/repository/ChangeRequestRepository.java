package aka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.ChangeRequest;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Integer> {
    List<ChangeRequest> findByTeacherIdOrderByIdDesc(Integer teacherId);
    long countByTeacherIdAndStatus(Integer teacherId, String status);
    List<ChangeRequest> findAllByOrderByIdDesc();
    long countByStatus(String status);
}
