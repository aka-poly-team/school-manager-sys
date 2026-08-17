package aka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.Teacher;
import aka.model.User;
import aka.service.CloudinaryService;
import aka.service.SystemLogService;
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
    SystemLogService systemLogService;
    CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String index(Model model) {
        User currentUser = SecurityUtils.getUser();
        Teacher teacher = SecurityUtils.getTeacher();

        String teacherName = (teacher != null && teacher.getName() != null) ? teacher.getName() : "Giáo viên";

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacherName);
        return "teacher/profile/index";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile, RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtils.getUser();

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc hết hạn. Vui lòng đăng nhập lại!");
            return "redirect:/teacher/profile";
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn một tập tin ảnh đại diện!");
            return "redirect:/teacher/profile";
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "Tập tin phải là định dạng hình ảnh (JPG, PNG, WEBP)!");
            return "redirect:/teacher/profile";
        }

        try {
            String avatarUrl = null;
            try {
                // Tải ảnh trực tiếp lên Cloudinary CDN
                avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
            } catch (Exception e) {
                // Fallback nếu Cloudinary bị ngắt kết nối
                avatarUrl = FileUploadUtils.save(avatarFile, "avatars", "avatar");
            }

            currentUser.setAvatarUrl(avatarUrl);
            userService.save(currentUser);

            systemLogService.log(currentUser, "CẬP NHẬT ẢNH ĐẠI DIỆN", "Cập nhật ảnh đại diện lên Cloudinary thành công: " + avatarUrl);

            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện Cloudinary thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh đại diện: " + e.getMessage());
        }

        return "redirect:/teacher/profile";
    }

    @PostMapping("/profile")
    public String update(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Hồ sơ cá nhân chỉ được phép xem và đổi ảnh đại diện!");
        return "redirect:/teacher/profile";
    }
}
