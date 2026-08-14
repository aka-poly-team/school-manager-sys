package aka.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.ChangeRequest;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Integer> {
    List<ChangeRequest> findByTeacherIdOrderByIdDesc(Integer teacherId);
    Page<ChangeRequest> findByTeacherId(Integer teacherId, Pageable pageable);
    long countByTeacherIdAndStatus(Integer teacherId, String status);
    List<ChangeRequest> findAllByOrderByIdDesc();
    long countByStatus(String status);
}
