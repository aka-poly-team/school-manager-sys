package aka.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 1. JPQL Query lọc danh sách Điểm danh thuần túy từ CSDL theo tháng, năm, giáo viên, trường, trạng thái
    @Query("SELECT a FROM Attendance a WHERE (:month IS NULL OR MONTH(a.date) = :month) " +
           "AND (:year IS NULL OR YEAR(a.date) = :year) " +
           "AND (:teacherId IS NULL OR (a.teacher IS NOT NULL AND a.teacher.id = :teacherId)) " +
           "AND (:schoolId IS NULL OR (a.school IS NOT NULL AND a.school.id = :schoolId)) " +
           "AND (:status IS NULL OR :status = '' OR UPPER(a.status) = UPPER(:status)) " +
           "ORDER BY a.id DESC")
    List<Attendance> filterAttendancesByQuery(
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("teacherId") Integer teacherId,
            @Param("schoolId") Integer schoolId,
            @Param("status") String status);

    // 2. JPQL Query tính Thống Kê Số Tiết Dạy Theo Giáo Viên từ CSDL (Không dùng DTO, không dùng lương)
    @Query("SELECT a.teacher.id, a.teacher.name, a.teacher.email, a.teacher.phone, " +
           "COUNT(a), " +
           "SUM(CASE WHEN UPPER(a.status) = 'APPROVED' THEN a.periods ELSE 0 END), " +
           "SUM(CASE WHEN UPPER(a.status) = 'PENDING' THEN a.periods ELSE 0 END) " +
           "FROM Attendance a WHERE a.teacher IS NOT NULL " +
           "AND (:month IS NULL OR MONTH(a.date) = :month) " +
           "AND (:year IS NULL OR YEAR(a.date) = :year) " +
           "AND (:teacherId IS NULL OR a.teacher.id = :teacherId) " +
           "AND (:schoolId IS NULL OR (a.school IS NOT NULL AND a.school.id = :schoolId)) " +
           "AND (:status IS NULL OR :status = '' OR UPPER(a.status) = UPPER(:status)) " +
           "GROUP BY a.teacher.id, a.teacher.name, a.teacher.email, a.teacher.phone")
    List<Object[]> queryTeacherPeriodStats(
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("teacherId") Integer teacherId,
            @Param("schoolId") Integer schoolId,
            @Param("status") String status);
}
