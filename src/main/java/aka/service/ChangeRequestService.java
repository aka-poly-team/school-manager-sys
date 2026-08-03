package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.ChangeRequest;

public interface ChangeRequestService {
    List<ChangeRequest> findAll();
    Optional<ChangeRequest> findById(Integer id);
    ChangeRequest save(ChangeRequest changeRequest);
    void deleteById(Integer id);

    List<ChangeRequest> findByTeacherIdOrderByIdDesc(Integer teacherId);
    long countByTeacherIdAndStatus(Integer teacherId, String status);
    List<ChangeRequest> findAllByOrderByIdDesc();
    long countByStatus(String status);
    long count();
}
