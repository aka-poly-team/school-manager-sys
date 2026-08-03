package aka.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import aka.model.Teacher;
import aka.model.User;
import aka.service.UserService;

/**
 * Utility library for Security & Auth operations.
 */
public class SecurityUtils {

    public static User getUser(UserService userService) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    public static Teacher getTeacher(UserService userService) {
        User user = getUser(userService);
        return (user != null) ? user.getTeacher() : null;
    }

    public static void populate(Model model, UserService userService) {
        User currentUser = getUser(userService);
        Teacher teacher = (currentUser != null) ? currentUser.getTeacher() : null;
        String teacherName = (teacher != null && teacher.getName() != null) ? teacher.getName() : "Giáo viên";

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacherName);

        if (currentUser != null && currentUser.getRole() != null) {
            model.addAttribute("userRole", currentUser.getRole().name());
        }
    }
}
