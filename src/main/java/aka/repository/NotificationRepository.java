package aka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Notification> findByForAdminTrueOrderByIdDesc();
    long countByForAdminTrueAndIsReadFalse();
    long countByTeacherIdAndIsReadFalse(Integer teacherId);
}
