package aka.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.dto.teacher.PasswordChangeForm;
import aka.model.User;
import aka.service.SystemLogService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import aka.util.ValidationUtils;
import jakarta.validation.Valid;
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
    SystemLogService systemLogService;

    @GetMapping("/change-password")
    public String index(Model model) {
        return "teacher/change-password/index";
    }

    @PostMapping("/change-password")
    public String update(@Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                         BindingResult bindingResult,
                         Model model) {
        User currentUser = SecurityUtils.getUser();

        if (currentUser == null) {
            model.addAttribute("error", "Phiên làm việc hết hạn. Vui lòng đăng nhập lại!");
            return "teacher/change-password/index";
        }

        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
            return "teacher/change-password/index";
        }

        if (!passwordEncoder.matches(form.getCurrentPassword(), currentUser.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "teacher/change-password/index";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "teacher/change-password/index";
        }

        currentUser.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.save(currentUser);

        systemLogService.log(currentUser, "ĐỔI MẬT KHẨU", "Đổi mật khẩu tài khoản cá nhân thành công.");

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "teacher/change-password/index";
    }
}
