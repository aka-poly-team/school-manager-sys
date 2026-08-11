package aka.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Notification;
import aka.model.Teacher;
import aka.service.NotificationService;
import aka.util.SecurityUtils;
import aka.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherNotificationController {

    NotificationService notificationService;

    @GetMapping("/notifications")
    public String index(Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Notification> notifications = teacherId != null 
                ? notificationService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("notifications", notifications);
        return "teacher/notification/list";
    }

    @PostMapping("/notifications/read/{id}")
    public String markRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/teacher/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead() {
        Teacher teacher = SecurityUtils.getTeacher();
        if (teacher != null) {
            notificationService.markAllAsReadForTeacher(teacher.getId());
        }
        return "redirect:/teacher/notifications";
    }

    @GetMapping("/notifications/click/{id}")
    public String clickNotification(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        Optional<Notification> notifOpt = notificationService.findById(id);
        if (notifOpt.isPresent()) {
            String link = notifOpt.get().getLink();
            if (StringUtils.isNotBlank(link)) {
                return "redirect:" + link;
            }
        }
        return "redirect:/teacher/notifications";
    }
}
