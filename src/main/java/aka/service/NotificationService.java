package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Notification;
import aka.model.Teacher;

public interface NotificationService {
    List<Notification> findAll();
    Optional<Notification> findById(Long id);
    Notification save(Notification notification);
    void deleteById(Long id);
    
    List<Notification> findByTeacherIdOrderByIdDesc(Integer teacherId);
    List<Notification> findByForAdminTrueOrderByIdDesc();
    long countUnreadForAdmin();
    long countUnreadForTeacher(Integer teacherId);

    void notifyTeacher(Teacher teacher, String message, String link);
    void notifyAdmin(String message, String link);
    void markAsRead(Long id);
    void markAllAsReadForTeacher(Integer teacherId);
    void markAllAsReadForAdmin();
}
