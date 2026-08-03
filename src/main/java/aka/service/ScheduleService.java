package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Schedule;

public interface ScheduleService {
    List<Schedule> findAll();
    Optional<Schedule> findById(Integer id);
    Schedule save(Schedule schedule);
    void deleteById(Integer id);

    List<Schedule> findByTeacherIdOrderByDayOfWeekAsc(Integer teacherId);
    List<Schedule> findAllByOrderByIdDesc();
    boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(Integer teacherId, Integer schoolId, Integer classId, Integer dayOfWeek, String session);
    boolean existsById(Integer id);
    long count();
}
