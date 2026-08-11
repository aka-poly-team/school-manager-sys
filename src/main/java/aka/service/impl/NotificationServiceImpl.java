package aka.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.Notification;
import aka.model.Teacher;
import aka.repository.NotificationRepository;
import aka.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByTeacherIdOrderByIdDesc(Integer teacherId) {
        return notificationRepository.findByTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByForAdminTrueOrderByIdDesc() {
        return notificationRepository.findByForAdminTrueOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadForAdmin() {
        return notificationRepository.countByForAdminTrueAndIsReadFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadForTeacher(Integer teacherId) {
        if (teacherId == null) return 0;
        return notificationRepository.countByTeacherIdAndIsReadFalse(teacherId);
    }

    @Override
    public void notifyTeacher(Teacher teacher, String message, String link) {
        if (teacher == null) return;
        Notification notification = Notification.builder()
                .teacher(teacher)
                .message(message)
                .link(link)
                .forAdmin(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void notifyAdmin(String message, String link) {
        Notification notification = Notification.builder()
                .message(message)
                .link(link)
                .forAdmin(true)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsReadForTeacher(Integer teacherId) {
        if (teacherId == null) return;
        List<Notification> unreadList = notificationRepository.findByTeacherIdOrderByIdDesc(teacherId);
        for (Notification n : unreadList) {
            if (Boolean.FALSE.equals(n.getIsRead())) {
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(unreadList);
    }

    @Override
    public void markAllAsReadForAdmin() {
        List<Notification> unreadList = notificationRepository.findByForAdminTrueOrderByIdDesc();
        for (Notification n : unreadList) {
            if (Boolean.FALSE.equals(n.getIsRead())) {
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(unreadList);
    }
}
