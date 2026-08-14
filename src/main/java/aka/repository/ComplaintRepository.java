package aka.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    List<Complaint> findByAttendanceTeacherIdOrderByIdDesc(Integer teacherId);
    Page<Complaint> findByAttendanceTeacherId(Integer teacherId, Pageable pageable);
    long countByAttendanceTeacherIdAndStatus(Integer teacherId, Integer status);
    List<Complaint> findAllByOrderByIdDesc();
    long countByStatus(Integer status);

    // Kiểm tra xem Đơn điểm danh/buổi dạy này đã được tạo Khiếu nại chưa
    boolean existsByAttendanceId(Long attendanceId);
}
