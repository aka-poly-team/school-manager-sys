package aka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import aka.model.Teacher;
import aka.model.User;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.FileUploadUtils;
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

    UserService userService;
    TeacherService teacherService;

    @GetMapping("/profile")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        User currentUser = SecurityUtils.getUser(userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);

        String maskedEmail = StringUtils.mask(currentUser != null ? currentUser.getUsername() : "");

        model.addAttribute("teacher", teacher);
        model.addAttribute("maskedEmail", maskedEmail);
        return "teacher/profile";
    }

    @PostMapping("/profile")
    public String update(@RequestParam("name") String name,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                         Model model) {
        SecurityUtils.populate(model, userService);
        User currentUser = SecurityUtils.getUser(userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);

        if (teacher != null) {
            teacher.setName(name);
            teacher.setPhone(phone);
            teacher.setAddress(address);
            teacherService.save(teacher);
        }

        if (currentUser != null && avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarPath = FileUploadUtils.save(avatarFile, "avatars", "avatar");
                if (avatarPath != null) {
                    currentUser.setAvatarUrl(avatarPath);
                    userService.save(currentUser);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Lỗi khi tải ảnh đại diện: " + e.getMessage());
            }
        }

        model.addAttribute("success", "Cập nhật hồ sơ cá nhân thành công!");
        model.addAttribute("teacher", teacher);
        model.addAttribute("maskedEmail", StringUtils.mask(currentUser != null ? currentUser.getUsername() : ""));

        return "teacher/profile";
    }
}
