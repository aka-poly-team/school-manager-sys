package aka.admin.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.RoleName;
import aka.model.Teacher;
import aka.model.User;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminTeacherController {

    UserService userService;
    TeacherService teacherService;
    PasswordEncoder passwordEncoder;

    @GetMapping("/teachers")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("users", userService.findAll());
        return "admin/teachers";
    }

    @PostMapping({"/teachers/new", "/teachers/create", "/teachers"})
    public String create(@RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "username", required = false) String username,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "role", required = false, defaultValue = "ROLE_TEACHER") String roleNameStr,
                         RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank()) {
            setFlashError(redirectAttributes, "Họ tên Giáo viên không được để trống!");
            return "redirect:/admin/teachers";
        }

        String accountUsername = (username != null && !username.isBlank()) 
                ? username.trim() 
                : ((email != null && !email.isBlank()) ? email.trim() : "user" + System.currentTimeMillis());

        if (userService.existsByUsername(accountUsername)) {
            setFlashError(redirectAttributes, "Email / Tên đăng nhập '" + accountUsername + "' đã tồn tại trong hệ thống!");
            return "redirect:/admin/teachers";
        }

        try {
            Teacher teacher = Teacher.builder()
                    .name(name.trim())
                    .email(email)
                    .phone(phone)
                    .status("active")
                    .build();
            teacher = teacherService.save(teacher);

            String rawPassword = (password != null && !password.isBlank()) ? password.trim() : "123456";

            RoleName role = RoleName.ROLE_TEACHER;
            try {
                role = RoleName.valueOf(roleNameStr);
            } catch (Exception ignored) {}

            User user = User.builder()
                    .username(accountUsername)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .enabled(true)
                    .teacher(teacher)
                    .build();
            userService.save(user);

            setFlashSuccess(redirectAttributes, "Tạo tài khoản và hồ sơ mới cho '" + teacher.getName() + "' thành công!");
        } catch (Exception e) {
            setFlashError(redirectAttributes, "Lỗi khi tạo tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    @PostMapping({"/teachers/{id}/edit", "/teachers/edit/{id}"})
    public String edit(@PathVariable("id") Integer id,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "email", required = false) String email,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "password", required = false) String password,
                       @RequestParam(value = "role", required = false, defaultValue = "ROLE_TEACHER") String roleNameStr,
                       RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findById(id).orElse(null);
            if (user != null) {
                Teacher teacher = user.getTeacher();
                if (teacher == null) {
                    teacher = Teacher.builder()
                            .name(name != null && !name.isBlank() ? name.trim() : user.getUsername())
                            .email(email)
                            .phone(phone)
                            .status("active")
                            .build();
                    teacher = teacherService.save(teacher);
                    user.setTeacher(teacher);
                } else {
                    if (name != null && !name.isBlank()) {
                        teacher.setName(name.trim());
                    }
                    teacher.setEmail(email);
                    teacher.setPhone(phone);
                    teacherService.save(teacher);
                }

                if (password != null && !password.isBlank()) {
                    user.setPassword(passwordEncoder.encode(password.trim()));
                }

                RoleName role = RoleName.ROLE_TEACHER;
                try {
                    role = RoleName.valueOf(roleNameStr);
                } catch (Exception ignored) {}
                user.setRole(role);
                userService.save(user);

                setFlashSuccess(redirectAttributes, "Cập nhật tài khoản '" + (user.getTeacher() != null ? user.getTeacher().getName() : user.getUsername()) + "' thành công!");
            } else {
                setFlashError(redirectAttributes, "Không tìm thấy tài khoản!");
            }
        } catch (Exception e) {
            setFlashError(redirectAttributes, "Lỗi khi cập nhật: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    @PostMapping({"/teachers/{id}/delete", "/teachers/delete/{id}"})
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id).orElse(null);
            if (user != null) {
                Teacher teacher = user.getTeacher();
                userService.deleteById(user.getId());
                if (teacher != null) {
                    teacherService.deleteById(teacher.getId());
                }
                setFlashSuccess(redirectAttributes, "Xóa tài khoản thành công!");
            } else {
                setFlashError(redirectAttributes, "Không tìm thấy tài khoản cần xóa!");
            }
        } catch (Exception e) {
            setFlashError(redirectAttributes, "Không thể xóa tài khoản này vì đã có dữ liệu giảng dạy/chấm công liên quan!");
        }
        return "redirect:/admin/teachers";
    }

    private void setFlashSuccess(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute("successMessage", msg);
        ra.addFlashAttribute("success", msg);
    }

    private void setFlashError(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute("errorMessage", msg);
        ra.addFlashAttribute("error", msg);
    }
}
