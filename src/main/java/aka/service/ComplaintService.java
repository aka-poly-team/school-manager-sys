package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Complaint;

public interface ComplaintService {
    List<Complaint> findAll();
    Optional<Complaint> findById(Integer id);
    Complaint save(Complaint complaint);
    void deleteById(Integer id);

    List<Complaint> findByAttendanceTeacherIdOrderByIdDesc(Integer teacherId);
    long countByAttendanceTeacherIdAndStatus(Integer teacherId, Integer status);
    List<Complaint> findAllByOrderByIdDesc();
    long countByStatus(Integer status);
    boolean existsByAttendanceId(Long attendanceId);
    boolean existsById(Integer id);
    long count();
}
