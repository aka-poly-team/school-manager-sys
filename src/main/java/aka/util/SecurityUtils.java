package aka.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import aka.model.Teacher;
import aka.model.User;
import aka.config.CustomUserDetails;

/**
 * Utility library for Security & Auth operations.
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Get authenticated User directly from SecurityContext without DB query.
     */
    public static User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails customUser) {
            return customUser.getUser();
        }
        return null;
    }

    /**
     * Get authenticated Teacher.
     */
    public static Teacher getTeacher() {
        User user = getUser();
        return (user != null) ? user.getTeacher() : null;
    }

    /**
     * Get authenticated Teacher ID (or null if unauthenticated / no teacher profile).
     */
    public static Integer getTeacherId() {
        Teacher teacher = getTeacher();
        return teacher != null ? teacher.getId() : null;
    }
}
