package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.Attendance;
import aka.repository.AttendanceRepository;
import aka.service.AttendanceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    AttendanceRepository attendanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findByTeacherIdOrderByIdDesc(Integer teacherId) {
        return attendanceRepository.findByTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findTop5ByTeacherIdOrderByIdDesc(Integer teacherId) {
        return attendanceRepository.findTop5ByTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findByTeacherIdAndDate(Integer teacherId, java.time.LocalDate date) {
        return attendanceRepository.findByTeacherIdAndDate(teacherId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findAllByOrderByIdDesc() {
        return attendanceRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTeacherIdAndStatus(Integer teacherId, String status) {
        return attendanceRepository.countByTeacherIdAndStatus(teacherId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return attendanceRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(Integer teacherId, Integer schoolId, Integer classId, String session, java.time.LocalDate date) {
        return attendanceRepository.existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(teacherId, schoolId, classId, session, date);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return attendanceRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> filterAttendancesByQuery(Integer month, Integer year, Integer teacherId, Integer schoolId, String status) {
        return attendanceRepository.filterAttendancesByQuery(month, year, teacherId, schoolId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> queryTeacherPeriodStats(Integer month, Integer year, Integer teacherId, Integer schoolId, String status) {
        return attendanceRepository.queryTeacherPeriodStats(month, year, teacherId, schoolId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> queryMonthlyStats(Integer year, Integer teacherId, Integer schoolId) {
        return attendanceRepository.queryMonthlyStats(year, teacherId, schoolId);
    }
}
