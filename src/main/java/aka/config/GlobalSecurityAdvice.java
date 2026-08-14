package aka.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import aka.model.RoleName;
import aka.model.Teacher;
import aka.model.User;
import aka.service.NotificationService;
import aka.util.SecurityUtils;
import aka.util.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Global ControllerAdvice to automatically populate security & layout model attributes
 * for all Spring MVC controllers across the application.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSecurityAdvice {

    private final NotificationService notificationService;

    @ModelAttribute
    public void populateGlobalSecurityAttributes(Model model) {
        User currentUser = SecurityUtils.getUser();
        Teacher teacher = currentUser != null ? currentUser.getTeacher() : null;
        RoleName role = currentUser != null ? currentUser.getRole() : null;
        if (role != null) {
            model.addAttribute("userRole", role.name());
        }

        String teacherName = (role == RoleName.ROLE_ADMIN) ? "Quản trị viên" : "Giáo viên";
        if (teacher != null && !StringUtils.isBlank(teacher.getName())) {
            String name = teacher.getName().trim();
            if (!name.contains("@")) {
                teacherName = name;
            }
        }
        String avatarUrl = currentUser != null ? currentUser.getAvatarUrl() : null;

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("avatarUrl", avatarUrl);

        long unreadCount = 0;
        if (role != null && notificationService != null) {
            unreadCount = (role == RoleName.ROLE_ADMIN)
                    ? notificationService.countUnreadForAdmin()
                    : (teacher != null ? notificationService.countUnreadForTeacher(teacher.getId()) : 0);
        }
        model.addAttribute("unreadNotificationsCount", unreadCount);
    }
}
