package aka.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import aka.model.Attendance;

public interface AttendanceService {
    List<Attendance> findAll();
    Optional<Attendance> findById(Long id);
    Attendance save(Attendance attendance);
    void deleteById(Long id);
    
    List<Attendance> findByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Attendance> findTop5ByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Attendance> findByTeacherIdAndDate(Integer teacherId, LocalDate date);
    List<Attendance> findAllByOrderByIdDesc();
    long countByTeacherIdAndStatus(Integer teacherId, String status);
    long countByStatus(String status);
    boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(Integer teacherId, Integer schoolId, Integer classId, String session, LocalDate date);
    long count();
}
