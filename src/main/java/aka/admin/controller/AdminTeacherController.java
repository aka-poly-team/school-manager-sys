package aka.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import aka.util.StringUtils;
import aka.model.RoleName;
import aka.model.Teacher;
import aka.model.User;
import aka.service.CloudinaryService;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.FileUploadUtils;
import aka.util.ValidationUtils;
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
    CloudinaryService cloudinaryService;

    // 1. TRANG QUẢN LÝ TÀI KHOẢN & PHÂN QUYỀN (GET /admin/teachers - Server-side Pagination)
    @GetMapping("/teachers")
    public String list(@RequestParam(value = "adminPage", defaultValue = "0") int adminPage,
                       @RequestParam(value = "teacherPage", defaultValue = "0") int teacherPage,
                       @RequestParam(value = "adminKeyword", required = false) String adminKeyword,
                       @RequestParam(value = "teacherKeyword", required = false) String teacherKeyword,
                       @RequestParam(value = "editAdminId", required = false) Integer editAdminId,
                       @RequestParam(value = "editTeacherId", required = false) Integer editTeacherId,
                       Model model) {
        
        Pageable adminPageable = PageRequest.of(adminPage, 5, Sort.by("id").descending());
        Pageable teacherPageable = PageRequest.of(teacherPage, 5, Sort.by("id").descending());

        Page<User> adminPageResult = userService.findByRole(RoleName.ROLE_ADMIN, adminKeyword, adminPageable);
        Page<User> teacherPageResult = userService.findByRole(RoleName.ROLE_TEACHER, teacherKeyword, teacherPageable);

        model.addAttribute("adminUsers", adminPageResult.getContent());
        model.addAttribute("adminPageObj", adminPageResult);
        model.addAttribute("adminKeyword", adminKeyword);
        model.addAttribute("editAdminId", editAdminId);

        model.addAttribute("teacherUsers", teacherPageResult.getContent());
        model.addAttribute("teacherPageObj", teacherPageResult);
        model.addAttribute("teacherKeyword", teacherKeyword);
        model.addAttribute("editTeacherId", editTeacherId);

        return "admin/teacher/list";
    }

    @GetMapping("/teachers/new")
    public String showCreateForm(@RequestParam(value = "targetRole", defaultValue = "ROLE_TEACHER") String targetRole, Model model) {
        model.addAttribute("targetRole", targetRole);
        return "admin/teacher/teacher-form";
    }

    @PostMapping("/teachers/new")
    public String create(@RequestParam("username") String username,
                         @RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "dob", required = false) String dob,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam("password") String password,
                         @RequestParam(value = "role", defaultValue = "ROLE_TEACHER") String role,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                         RedirectAttributes redirectAttributes) {

        try {
            if (password == null || password.trim().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu là bắt buộc và phải từ 6 ký tự trở lên!");
                return "redirect:/admin/teachers";
            }
            if (userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại!");
                return "redirect:/admin/teachers";
            }

            LocalDate dobDate = (dob != null && !dob.isBlank()) ? LocalDate.parse(dob) : null;

            Teacher teacher = Teacher.builder()
                    .name(name != null && !name.isBlank() ? StringUtils.toTitleCase(name) : username)
                    .email(email)
                    .phone(phone)
                    .dob(dobDate)
                    .address(address)
                    .status("active")
                    .build();
            teacher = teacherService.save(teacher);

            // Upload ảnh lên Cloudinary nếu có
            String avatarUrl = null;
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                } catch (Exception e) {
                    avatarUrl = FileUploadUtils.save(avatarFile, "avatars", "avatar");
                }
            }

            RoleName roleName = RoleName.of(role, RoleName.ROLE_TEACHER);
            User user = User.builder()
                    .username(username.trim())
                    .password(passwordEncoder.encode(password.trim()))
                    .role(roleName)
                    .teacher(teacher)
                    .avatarUrl(avatarUrl)
                    .enabled(true)
                    .build();
            userService.save(user);

            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản '" + user.getUsername() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    // PURE SPRING BOOT NO-JS INLINE ROW UPDATE
    @PostMapping("/teachers/edit-inline/{id}")
    public String editInline(@PathVariable("id") Integer id,
                             @RequestParam("name") String name,
                             @RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestHeader(value = "Referer", required = false) String referer,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!ValidationUtils.isValidName(name)) {
                redirectAttributes.addFlashAttribute("error", ValidationUtils.MSG_NAME);
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/teachers", "editAdminId", "editTeacherId");
            }

            if (!ValidationUtils.isValidGmail(email)) {
                redirectAttributes.addFlashAttribute("error", ValidationUtils.MSG_GMAIL);
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/teachers", "editAdminId", "editTeacherId");
            }

            if (phone != null && !phone.isBlank()) {
                phone = phone.replaceAll("\\D", "");
                if (phone.length() > 11) {
                    phone = phone.substring(0, 11);
                }
                if (!ValidationUtils.isValidPhone(phone)) {
                    redirectAttributes.addFlashAttribute("error", ValidationUtils.MSG_PHONE);
                    return "redirect:" + StringUtils.cleanReferer(referer, "/admin/teachers", "editAdminId", "editTeacherId");
                }
            }

            User user = userService.findById(id).orElse(null);
            if (user != null) {
                Teacher teacher = user.getTeacher();
                if (teacher == null) {
                    teacher = Teacher.builder()
                            .name(name != null && !name.isBlank() ? StringUtils.toTitleCase(name) : user.getUsername())
                            .email(email)
                            .phone(phone)
                            .status("active")
                            .build();
                    teacher = teacherService.save(teacher);
                    user.setTeacher(teacher);
                    userService.save(user);
                } else {
                    if (name != null && !name.isBlank()) {
                        teacher.setName(StringUtils.toTitleCase(name));
                    }
                    teacher.setEmail(email);
                    teacher.setPhone(phone);
                    teacherService.save(teacher);
                }
                redirectAttributes.addFlashAttribute("success", "Cập nhật tài khoản '" + user.getUsername() + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/teachers", "editAdminId", "editTeacherId");
    }

    @PostMapping("/teachers/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        try {
            User targetUser = userService.findById(id).orElse(null);
            String accountName = (targetUser != null && targetUser.getUsername() != null) ? targetUser.getUsername() : ("#" + id);

            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa tài khoản '" + accountName + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa tài khoản: " + e.getMessage());
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/teachers", "editAdminId", "editTeacherId");
    }
}
