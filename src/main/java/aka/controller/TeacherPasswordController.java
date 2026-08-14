package aka.controller;

import java.util.Map;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import aka.dto.teacher.PasswordChangeForm;
import aka.model.User;
import aka.service.EmailService;
import aka.service.SystemLogService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import aka.util.ValidationUtils;
import jakarta.servlet.http.HttpSession;
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
    EmailService emailService;

    @GetMapping("/change-password")
    public String index(Model model) {
        return "teacher/change-password/index";
    }

    // 1. ENDPOINT GỬI MÃ OTP (CHỐNG SPAM 60s & KIỂM TRA EMAIL SỞ HỮU)
    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam("email") String email, HttpSession session) {
        User currentUser = SecurityUtils.getUser();

        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phiên làm việc hết hạn. Vui lòng đăng nhập lại!"));
        }

        if (email == null || email.isBlank() || !ValidationUtils.isValidGmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ValidationUtils.MSG_GMAIL));
        }

        // Chống SPAM: Kiểm tra 60 giây giữa các lần gửi
        Long lastSent = (Long) session.getAttribute("OTP_LAST_SENT");
        if (lastSent != null && (System.currentTimeMillis() - lastSent) < 60000) {
            long waitSec = (60000 - (System.currentTimeMillis() - lastSent)) / 1000;
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Vui lòng đợi " + waitSec + " giây nữa trước khi yêu cầu gửi lại mã OTP mới!"
            ));
        }

        String inputEmail = email.trim();
        String userUsername = currentUser.getUsername() != null ? currentUser.getUsername().trim() : "";
        String teacherEmail = (currentUser.getTeacher() != null && currentUser.getTeacher().getEmail() != null) 
                                ? currentUser.getTeacher().getEmail().trim() : "";

        boolean matchesUsername = inputEmail.equalsIgnoreCase(userUsername);
        boolean matchesTeacherEmail = !teacherEmail.isEmpty() && inputEmail.equalsIgnoreCase(teacherEmail);

        if (!matchesUsername && !matchesTeacherEmail) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Email đã nhập không khớp với Email/Tài khoản của bạn!"
            ));
        }

        try {
            // Sinh mã OTP 6 chữ số ngẫu nhiên
            String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // Hiệu lực 5 phút

            session.setAttribute("OTP_CODE", otpCode);
            session.setAttribute("OTP_EMAIL", inputEmail);
            session.setAttribute("OTP_EXPIRY", expiryTime);
            session.setAttribute("OTP_LAST_SENT", System.currentTimeMillis());
            session.setAttribute("OTP_VERIFIED", false);

            // Gửi Email qua SMTP
            emailService.sendOtpEmail(inputEmail, otpCode);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mã OTP xác thực 6 chữ số đã được gửi tới " + inputEmail + ". Vui lòng kiểm tra email!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false, 
                "message", "Lỗi khi gửi mã OTP qua Email: " + e.getMessage()
            ));
        }
    }

    // 2. ENDPOINT XÁC NHẬN MÃ OTP (CHỈ KHI OTP HỢP LỆ MỚI MỞ KHÓA FORM ĐỔI MẬT KHẨU)
    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam("email") String email,
                                                         @RequestParam("otpCode") String otpCode,
                                                         HttpSession session) {
        if (email == null || email.isBlank() || otpCode == null || otpCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập đầy đủ Email và Mã OTP!"));
        }

        String sessionOtp = (String) session.getAttribute("OTP_CODE");
        String sessionEmail = (String) session.getAttribute("OTP_EMAIL");
        Long sessionExpiry = (Long) session.getAttribute("OTP_EXPIRY");

        if (sessionOtp == null || sessionExpiry == null || System.currentTimeMillis() > sessionExpiry) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã OTP đã hết hạn hoặc chưa được tạo. Vui lòng bấm 'Gửi mã OTP' để lấy mã mới!"));
        }

        if (!email.trim().equalsIgnoreCase(sessionEmail) || !otpCode.trim().equals(sessionOtp)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã xác thực OTP không chính xác!"));
        }

        // Đánh dấu đã xác thực OTP thành công trong Session
        session.setAttribute("OTP_VERIFIED", true);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Xác thực mã OTP thành công! Vui lòng nhập mật khẩu mới bên dưới để hoàn tất."
        ));
    }

    // 3. ENDPOINT XÁC NHẬN ĐỔI MẬT KHẨU (BẮT BUỘC ĐÃ VERIFY OTP BƯỚC TRƯỚC)
    @PostMapping("/change-password")
    public String update(@Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                         BindingResult bindingResult,
                         HttpSession session,
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

        // Kiểm tra xem đã qua bước xác thực OTP thành công chưa
        Boolean isVerified = (Boolean) session.getAttribute("OTP_VERIFIED");
        String sessionEmail = (String) session.getAttribute("OTP_EMAIL");

        if (isVerified == null || !isVerified || sessionEmail == null || !sessionEmail.equalsIgnoreCase(form.getEmail().trim())) {
            model.addAttribute("error", "Vui lòng nhập Email và xác nhận mã OTP thành công trước khi đổi mật khẩu!");
            return "teacher/change-password/index";
        }

        // Validate Mật khẩu hiện tại
        if (!passwordEncoder.matches(form.getCurrentPassword(), currentUser.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "teacher/change-password/index";
        }

        // Validate Mật khẩu mới trùng khớp
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "teacher/change-password/index";
        }

        // Cập nhật Mật khẩu thành công
        currentUser.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.save(currentUser);

        // Hủy OTP sau khi sử dụng thành công
        session.removeAttribute("OTP_CODE");
        session.removeAttribute("OTP_EMAIL");
        session.removeAttribute("OTP_EXPIRY");
        session.removeAttribute("OTP_VERIFIED");
        session.removeAttribute("OTP_LAST_SENT");

        systemLogService.log(currentUser, "ĐỔI MẬT KHẨU", "Đổi mật khẩu thành công bằng xác thực mã OTP gửi tới Email: " + form.getEmail().trim());

        model.addAttribute("success", "Đổi mật khẩu thành công! Mật khẩu mới của bạn đã có hiệu lực.");
        return "teacher/change-password/index";
    }
}
