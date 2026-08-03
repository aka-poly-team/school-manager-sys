package aka.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import aka.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/auth/login")
    public String login(HttpServletRequest request, Model model) {
        if (request.getParameter("logout") != null) {
            String rememberedUsername = CookieUtils.getCookieValue(request, "remembered_username");
            if (rememberedUsername != null && !rememberedUsername.isBlank()) {
                model.addAttribute("rememberedUsername", rememberedUsername);
            }
            return "auth/login";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return home();
        }

        String rememberedUsername = CookieUtils.getCookieValue(request, "remembered_username");
        if (rememberedUsername != null && !rememberedUsername.isBlank()) {
            model.addAttribute("rememberedUsername", rememberedUsername);
        }
        return "auth/login";
    }

    @GetMapping("/auth/access-denied")
    public String denied() {
        return "auth/access-denied";
    }
}
