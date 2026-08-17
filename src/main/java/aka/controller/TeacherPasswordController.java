package aka.controller;

import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.User;
import aka.service.EmailService;
import aka.service.SystemLogService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import aka.util.ValidationUtils;
import jakarta.servlet.http.HttpSession;
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
    public String index(HttpSession session, Model model) {
        User currentUser = SecurityUtils.getUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Boolean isVerified = (Boolean) session.getAttribute("OTP_VERIFIED");
        Boolean isSent = (Boolean) session.getAttribute("OTP_SENT");
        String otpEmail = (String) session.getAttribute("OTP_EMAIL");

        int step = 1;
        if (Boolean.TRUE.equals(isVerified)) {
            step = 3;
        } else if (Boolean.TRUE.equals(isSent)) {
            step = 2;
        }

        model.addAttribute("step", step);
        model.addAttribute("otpEmail", otpEmail != null ? otpEmail : "");
        return "teacher/change-password/index";
    }

    // 1. ENDPOINT GỬI MÃ OTP (PURE HTML FORM SUBMIT)
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtils.getUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (email == null || email.isBlank() || !ValidationUtils.isValidGmail(email)) {
            redirectAttributes.addFlashAttribute("error", ValidationUtils.MSG_GMAIL);
            return "redirect:/teacher/change-password";
        }

        // Chống SPAM: Kiểm tra 60 giây giữa các lần gửi
        Long lastSent = (Long) session.getAttribute("OTP_LAST_SENT");
        if (lastSent != null && (System.currentTimeMillis() - lastSent) < 60000) {
            long waitSec = (60000 - (System.currentTimeMillis() - lastSent)) / 1000;
            redirectAttributes.addFlashAttribute("error", "Vui lòng đợi " + waitSec + " giây nữa trước khi yêu cầu gửi lại mã OTP mới!");
            return "redirect:/teacher/change-password";
        }

        String inputEmail = email.trim();
        String userUsername = currentUser.getUsername() != null ? currentUser.getUsername().trim() : "";
        String teacherEmail = (currentUser.getTeacher() != null && currentUser.getTeacher().getEmail() != null) 
                                ? currentUser.getTeacher().getEmail().trim() : "";

        boolean matchesUsername = inputEmail.equalsIgnoreCase(userUsername);
        boolean matchesTeacherEmail = !teacherEmail.isEmpty() && inputEmail.equalsIgnoreCase(teacherEmail);

        if (!matchesUsername && !matchesTeacherEmail) {
            redirectAttributes.addFlashAttribute("error", "Email đã nhập không khớp với Email/Tài khoản của bạn!");
            return "redirect:/teacher/change-password";
        }

        try {
            String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // Hiệu lực 5 phút

            session.setAttribute("OTP_CODE", otpCode);
            session.setAttribute("OTP_EMAIL", inputEmail);
            session.setAttribute("OTP_EXPIRY", expiryTime);
            session.setAttribute("OTP_LAST_SENT", System.currentTimeMillis());
            session.setAttribute("OTP_SENT", true);
            session.setAttribute("OTP_VERIFIED", false);

            emailService.sendOtpEmail(inputEmail, otpCode);

            redirectAttributes.addFlashAttribute("success", "Mã OTP 6 chữ số đã được gửi tới " + inputEmail + ". Vui lòng kiểm tra email!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi mã OTP qua Email: " + e.getMessage());
        }

        return "redirect:/teacher/change-password";
    }

    // 2. ENDPOINT XÁC NHẬN MÃ OTP (PURE HTML FORM SUBMIT)
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otpCode") String otpCode,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String sessionOtp = (String) session.getAttribute("OTP_CODE");
        Long sessionExpiry = (Long) session.getAttribute("OTP_EXPIRY");

        if (otpCode == null || otpCode.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mã OTP 6 chữ số!");
            return "redirect:/teacher/change-password";
        }

        if (sessionOtp == null || sessionExpiry == null || System.currentTimeMillis() > sessionExpiry) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP đã hết hạn hoặc chưa được tạo. Vui lòng gửi lại mã OTP mới!");
            session.removeAttribute("OTP_SENT");
            return "redirect:/teacher/change-password";
        }

        if (!otpCode.trim().equals(sessionOtp)) {
            redirectAttributes.addFlashAttribute("error", "Mã xác thực OTP không chính xác! Vui lòng kiểm tra lại.");
            return "redirect:/teacher/change-password";
        }

        session.setAttribute("OTP_VERIFIED", true);
        redirectAttributes.addFlashAttribute("success", "Xác thực mã OTP thành công! Vui lòng nhập mật khẩu mới bên dưới.");
        return "redirect:/teacher/change-password";
    }

    // 3. ENDPOINT ĐẶT LẠI TRẠNG THÁI OTP (ĐỂ NHẬP EMAIL KHÁC)
    @PostMapping("/reset-otp")
    public String resetOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("OTP_CODE");
        session.removeAttribute("OTP_EMAIL");
        session.removeAttribute("OTP_EXPIRY");
        session.removeAttribute("OTP_VERIFIED");
        session.removeAttribute("OTP_SENT");
        // Giữ OTP_LAST_SENT để chống SPAM 60s
        return "redirect:/teacher/change-password";
    }

    // 4. ENDPOINT ĐỔI MẬT KHẨU (BẮT BUỘC ĐÃ VERIFY OTP BƯỚC TRƯỚC)
    @PostMapping("/change-password")
    public String update(@RequestParam("currentPassword") String currentPassword,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtils.getUser();

        if (currentUser == null) {
            return "redirect:/login";
        }

        Boolean isVerified = (Boolean) session.getAttribute("OTP_VERIFIED");
        String sessionEmail = (String) session.getAttribute("OTP_EMAIL");

        if (isVerified == null || !isVerified || sessionEmail == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập Email và xác nhận mã OTP thành công trước khi đổi mật khẩu!");
            return "redirect:/teacher/change-password";
        }

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/teacher/change-password";
        }

        if (passwordEncoder.matches(newPassword, currentUser.getPassword()) || currentPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return "redirect:/teacher/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "redirect:/teacher/change-password";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(currentUser);

        session.removeAttribute("OTP_CODE");
        session.removeAttribute("OTP_EMAIL");
        session.removeAttribute("OTP_EXPIRY");
        session.removeAttribute("OTP_VERIFIED");
        session.removeAttribute("OTP_SENT");
        session.removeAttribute("OTP_LAST_SENT");

        systemLogService.log(currentUser, "ĐỔI MẬT KHẨU", "Đổi mật khẩu thành công bằng xác thực mã OTP gửi tới Email: " + sessionEmail);

        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công! Mật khẩu mới của bạn đã có hiệu lực.");
        return "redirect:/teacher/change-password";
    }
}
