package aka.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.PasswordResetToken;
import aka.model.User;
import aka.service.EmailService;
import aka.service.PasswordResetTokenService;
import aka.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PasswordResetController {

    UserService userService;
    PasswordResetTokenService passwordResetTokenService;
    EmailService emailService;

    @GetMapping("/forgot-password")
    public String forgot() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String send(@RequestParam("identifier") String identifier,
                       HttpServletRequest request,
                       Model model) {
        if (identifier == null || identifier.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập tên đăng nhập hoặc email!");
            return "auth/forgot-password";
        }

        String cleanIdentifier = identifier.trim();
        Optional<User> userOpt = userService.findByUsernameOrEmail(cleanIdentifier);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy tài khoản tương ứng với thông tin bạn cung cấp.");
            model.addAttribute("identifier", cleanIdentifier);
            return "auth/forgot-password";
        }

        User user = userOpt.get();
        String recipientEmail = null;

        if (user.getTeacher() != null && user.getTeacher().getEmail() != null && !user.getTeacher().getEmail().isBlank()) {
            recipientEmail = user.getTeacher().getEmail().trim();
        } else if (user.getUsername() != null && user.getUsername().contains("@")) {
            recipientEmail = user.getUsername().trim();
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            model.addAttribute("error", "Tài khoản của bạn chưa đăng ký email khôi phục. Vui lòng liên hệ Quản trị viên.");
            model.addAttribute("identifier", cleanIdentifier);
            return "auth/forgot-password";
        }

        long cooldownRemaining = passwordResetTokenService.getCooldownSecondsRemaining(user, 60);
        if (cooldownRemaining > 0) {
            model.addAttribute("error", "Bạn thao tác quá nhanh. Vui lòng chờ " + cooldownRemaining + " giây trước khi gửi lại yêu cầu!");
            model.addAttribute("identifier", cleanIdentifier);
            return "auth/forgot-password";
        }

        try {
            PasswordResetToken tokenEntity = passwordResetTokenService.createTokenForUser(user);

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }

            String resetUrl = baseUrl + "/auth/reset-password?token=" + tokenEntity.getToken();
            emailService.sendPasswordResetEmail(recipientEmail, resetUrl);

            model.addAttribute("success", "Chúng tôi đã gửi hướng dẫn đặt lại mật khẩu tới email: " + recipientEmail + ". Vui lòng kiểm tra hộp thư.");
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu: {}", e.getMessage(), e);
            model.addAttribute("error", "Gửi email thất bại: " + e.getMessage());
            model.addAttribute("identifier", cleanIdentifier);
        }

        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String reset(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Đường dẫn không hợp lệ hoặc thiếu mã xác thực.");
            return "auth/reset-password";
        }

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.findByToken(token);
        if (tokenOpt.isEmpty() || !passwordResetTokenService.isValidToken(tokenOpt.get())) {
            model.addAttribute("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn (sau 15 phút). Vui lòng yêu cầu lại.");
            return "auth/reset-password";
        }

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String update(@RequestParam("token") String token,
                         @RequestParam("password") String password,
                         @RequestParam("confirmPassword") String confirmPassword,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Mã xác thực không hợp lệ.");
            return "auth/reset-password";
        }

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.findByToken(token);
        if (tokenOpt.isEmpty() || !passwordResetTokenService.isValidToken(tokenOpt.get())) {
            model.addAttribute("error", "Liên kết đặt lại mật khẩu đã hết hạn hoặc không tồn tại.");
            return "auth/reset-password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Mật khẩu mới phải có tối thiểu 6 ký tự.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Xác nhận mật khẩu mới không trùng khớp.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        userService.updatePassword(user, password);
        passwordResetTokenService.deleteToken(resetToken);

        redirectAttributes.addFlashAttribute("resetSuccessMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        return "redirect:/auth/login?resetSuccess";
    }
}
