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
    boolean existsByTeacherIdAndSchoolIdAndDayOfWeekAndSession(Integer teacherId, Integer schoolId, Integer dayOfWeek, String session);
    boolean existsByTeacherIdAndDayOfWeekAndSession(Integer teacherId, Integer dayOfWeek, String session);
    boolean existsByTeacherIdAndDayOfWeekAndSessionAndIdNot(Integer teacherId, Integer dayOfWeek, String session, Integer id);
    boolean existsBySchoolClassIdAndDayOfWeekAndSession(Integer classId, Integer dayOfWeek, String session);
    boolean existsBySchoolClassIdAndDayOfWeekAndSessionAndIdNot(Integer classId, Integer dayOfWeek, String session, Integer id);
    boolean existsById(Integer id);
    long count();
}
