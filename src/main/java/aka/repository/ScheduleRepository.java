package aka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByTeacherIdOrderByDayOfWeekAsc(Integer teacherId);
    List<Schedule> findAllByOrderByIdDesc();

    boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(
            Integer teacherId, Integer schoolId, Integer classId, Integer dayOfWeek, String session);
}
