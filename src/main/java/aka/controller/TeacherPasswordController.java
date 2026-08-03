package aka.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.User;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherPasswordController {

    UserService userService;
    PasswordEncoder passwordEncoder;

    @GetMapping("/change-password")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        return "teacher/change-password";
    }

    @PostMapping("/change-password")
    public String update(@RequestParam("currentPassword") String currentPassword,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         Model model) {
        SecurityUtils.populate(model, userService);
        User currentUser = SecurityUtils.getUser(userService);

        if (currentUser == null) {
            model.addAttribute("error", "Phiên làm việc hết hạn. Vui lòng đăng nhập lại!");
            return "teacher/change-password";
        }

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "teacher/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "teacher/change-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            return "teacher/change-password";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(currentUser);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "teacher/change-password";
    }
}
