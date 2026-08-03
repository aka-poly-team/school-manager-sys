package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Notification;

public interface NotificationService {
    List<Notification> findAll();
    Optional<Notification> findById(Long id);
    Notification save(Notification notification);
    void deleteById(Long id);
    
    List<Notification> findByTeacherIdOrderByIdDesc(Integer teacherId);
}
