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

        String teacherName = teacher != null ? StringUtils.defaultIfBlank(teacher.getName(), "Quản trị viên") : "Quản trị viên";
        String avatarUrl = currentUser != null ? currentUser.getAvatarUrl() : null;

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("avatarUrl", avatarUrl);

        RoleName role = currentUser != null ? currentUser.getRole() : null;
        if (role != null) {
            model.addAttribute("userRole", role.name());
        }

        long unreadCount = 0;
        if (role != null && notificationService != null) {
            unreadCount = (role == RoleName.ROLE_ADMIN)
                    ? notificationService.countUnreadForAdmin()
                    : (teacher != null ? notificationService.countUnreadForTeacher(teacher.getId()) : 0);
        }
        model.addAttribute("unreadNotificationsCount", unreadCount);
    }
}
