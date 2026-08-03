package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.Schedule;
import aka.repository.ScheduleRepository;
import aka.service.ScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    ScheduleRepository scheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Schedule> findById(Integer id) {
        return scheduleRepository.findById(id);
    }

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteById(Integer id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findByTeacherIdOrderByDayOfWeekAsc(Integer teacherId) {
        return scheduleRepository.findByTeacherIdOrderByDayOfWeekAsc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findAllByOrderByIdDesc() {
        return scheduleRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(Integer teacherId, Integer schoolId, Integer classId, Integer dayOfWeek, String session) {
        return scheduleRepository.existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(teacherId, schoolId, classId, dayOfWeek, session);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return scheduleRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return scheduleRepository.count();
    }
}
