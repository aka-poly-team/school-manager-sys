package aka.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByTeacherIdOrderByDayOfWeekAsc(Integer teacherId);
    Page<Schedule> findByTeacherId(Integer teacherId, Pageable pageable);
    List<Schedule> findAllByOrderByIdDesc();

    boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(
            Integer teacherId, Integer schoolId, Integer classId, Integer dayOfWeek, String session);

    // Kiểm tra Giáo viên có Lịch dạy tại Trường vào Thứ + Ca dạy không
    boolean existsByTeacherIdAndSchoolIdAndDayOfWeekAndSession(Integer teacherId, Integer schoolId, Integer dayOfWeek, String session);

    // Kiểm tra trùng lịch của Giáo viên (Cùng Thứ + Ca dạy)
    boolean existsByTeacherIdAndDayOfWeekAndSession(Integer teacherId, Integer dayOfWeek, String session);
    boolean existsByTeacherIdAndDayOfWeekAndSessionAndIdNot(Integer teacherId, Integer dayOfWeek, String session, Integer id);

    // Kiểm tra trùng lịch của Lớp học (Cùng Thứ + Ca dạy)
    boolean existsBySchoolClassIdAndDayOfWeekAndSession(Integer schoolClassId, Integer dayOfWeek, String session);
    boolean existsBySchoolClassIdAndDayOfWeekAndSessionAndIdNot(Integer schoolClassId, Integer dayOfWeek, String session, Integer id);
}
