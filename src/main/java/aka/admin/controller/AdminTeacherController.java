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

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import aka.dto.admin.TeacherForm;
import aka.model.RoleName;
import aka.model.Teacher;
import aka.model.User;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.ValidationUtils;
import jakarta.validation.Valid;
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

    // 1. TRANG QUẢN LÝ TÀI KHOẢN & PHÂN QUYỀN (GET /admin/teachers)
    @GetMapping("/teachers")
    public String list(@RequestParam(value = "showAdd", required = false) Boolean showAdd,
                       @RequestParam(value = "editId", required = false) Integer editId, 
                       Model model) {
        model.addAttribute("users", userService.findAll());
        if (Boolean.TRUE.equals(showAdd)) {
            model.addAttribute("showAdd", true);
        }
        if (editId != null) {
            User editUser = userService.findById(editId).orElse(null);
            model.addAttribute("editUser", editUser);
        }
        return "admin/teacher/list";
    }

    // 1b. TRANG FORM THÊM MỚI TÀI KHOẢN (GET /admin/teachers/new)
    @GetMapping("/teachers/new")
    public String showCreateForm(Model model) {
        return "admin/teacher/form";
    }

    // 1c. TRANG FORM CHỈNH SỬA TÀI KHOẢN (GET /admin/teachers/edit/{id})
    @GetMapping("/teachers/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User editUser = userService.findById(id).orElse(null);
        if (editUser == null) {
            return "redirect:/admin/teachers";
        }
        model.addAttribute("editUser", editUser);
        return "admin/teacher/form";
    }

    // 2. THÊM MỚI TÀI KHOẢN & HỒ SƠ GIÁO VIÊN (POST /admin/teachers/new)
    @PostMapping("/teachers/new")
    public String create(@Valid @ModelAttribute("teacherForm") TeacherForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/teachers";
        }

        String name = form.getName();
        String email = form.getEmail();
        String phone = form.getPhone();
        String username = form.getUsername();
        String password = form.getPassword();
        String roleNameStr = form.getRole();

        String accountUsername = (username != null && !username.isBlank()) 
                ? username.trim() 
                : ((email != null && !email.isBlank()) ? email.trim() : "user" + System.currentTimeMillis());

        if (userService.existsByUsername(accountUsername)) {
            redirectAttributes.addFlashAttribute("error", "Email / Tên đăng nhập '" + accountUsername + "' đã tồn tại trong hệ thống!");
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

            RoleName role = RoleName.of(roleNameStr, RoleName.ROLE_TEACHER);

            User user = User.builder()
                    .username(accountUsername)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .enabled(true)
                    .teacher(teacher)
                    .build();
            userService.save(user);

            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản và hồ sơ mới cho '" + teacher.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    // 3. CHỈNH SỬA TÀI KHOẢN & HỒ SƠ GIÁO VIÊN (POST /admin/teachers/edit/{id})
    @PostMapping("/teachers/edit/{id}")
    public String edit(@PathVariable("id") Integer id,
                       @Valid @ModelAttribute("teacherForm") TeacherForm form,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/teachers";
        }

        String name = form.getName();
        String email = form.getEmail();
        String phone = form.getPhone();
        String password = form.getPassword();
        String roleNameStr = form.getRole();

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

                RoleName role = RoleName.of(roleNameStr, RoleName.ROLE_TEACHER);
                user.setRole(role);
                userService.save(user);

                redirectAttributes.addFlashAttribute("success", "Cập nhật tài khoản '" + (user.getTeacher() != null ? user.getTeacher().getName() : user.getUsername()) + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    // 4. XÓA TÀI KHOẢN & HỒ SƠ GIÁO VIÊN (POST /admin/teachers/delete/{id})
    @PostMapping("/teachers/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id).orElse(null);
            if (user != null) {
                Teacher teacher = user.getTeacher();
                userService.deleteById(user.getId());
                if (teacher != null) {
                    teacherService.deleteById(teacher.getId());
                }
                redirectAttributes.addFlashAttribute("success", "Xóa tài khoản thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản cần xóa!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản này vì đã có dữ liệu giảng dạy/chấm công liên quan!");
        }

        return "redirect:/admin/teachers";
    }
}
