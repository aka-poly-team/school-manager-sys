package aka.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Attendance> findTop5ByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Attendance> findByTeacherIdAndDate(Integer teacherId, LocalDate date);
    List<Attendance> findAllByOrderByIdDesc();
    long countByTeacherIdAndStatus(Integer teacherId, String status);
    long countByStatus(String status);

    boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(
            Integer teacherId, Integer schoolId, Integer classId, String session, LocalDate date);
}
