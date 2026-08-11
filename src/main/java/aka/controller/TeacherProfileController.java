package aka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.Teacher;
import aka.model.User;
import aka.util.SecurityUtils;
import aka.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherProfileController {

    @GetMapping("/profile")
    public String index(Model model) {
        User currentUser = SecurityUtils.getUser();
        Teacher teacher = SecurityUtils.getTeacher();

        String teacherName = (teacher != null && teacher.getName() != null) ? teacher.getName() : "Giáo viên";
        String maskedEmail = StringUtils.mask(currentUser != null ? currentUser.getUsername() : "");

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("maskedEmail", maskedEmail);
        return "teacher/profile/index";
    }

    @PostMapping("/profile")
    public String update(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Hồ sơ cá nhân chỉ được phép xem, không được chỉnh sửa!");
        return "redirect:/teacher/profile";
    }
}
