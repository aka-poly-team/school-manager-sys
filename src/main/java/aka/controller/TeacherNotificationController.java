package aka.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Notification;
import aka.model.Teacher;
import aka.service.NotificationService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherNotificationController {

    UserService userService;
    NotificationService notificationService;

    @GetMapping("/notifications")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Notification> notifications = teacherId != null 
                ? notificationService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("notifications", notifications);
        return "teacher/notifications";
    }
}
