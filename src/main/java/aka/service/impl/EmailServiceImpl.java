package aka.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import aka.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@akaschool.edu.vn}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[AKA School Manager] Yêu cầu đặt lại mật khẩu tài khoản");

            String content = "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">" +
                    "<h2>Khôi phục mật khẩu tài khoản AKA</h2>" +
                    "<p>Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản hệ thống AKA School Manager.</p>" +
                    "<p>Vui lòng nhấp vào liên kết bên dưới để tạo mật khẩu mới (Liên kết có hiệu lực trong <b>15 phút</b>):</p>" +
                    "<p><a href=\"" + resetUrl + "\" style=\"display: inline-block; padding: 10px 20px; background-color: #2563eb; color: #ffffff; text-decoration: none; border-radius: 5px;\">Đặt lại mật khẩu</a></p>" +
                    "<br><p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Email đặt lại mật khẩu đã gửi thành công tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu tới {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Gửi email thất bại. Vui lòng kiểm tra lại cấu hình SMTP server.", e);
        }
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[AKA System] Mã OTP xác nhận đổi mật khẩu");

            String content = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 24px; max-width: 550px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;\">" +
                    "<h2 style=\"color: #0f172a; margin-top: 0;\">🔐 Mã Xác Thực OTP Đổi Mật Khẩu</h2>" +
                    "<p style=\"color: #334155; font-size: 15px;\">Bạn đang thực hiện yêu cầu đổi mật khẩu trên hệ thống <b>AKA School Manager</b>.</p>" +
                    "<p style=\"color: #334155; font-size: 15px;\">Mã OTP xác thực 6 chữ số của bạn là:</p>" +
                    "<div style=\"text-align: center; margin: 20px 0; padding: 16px; background-color: #f1f5f9; border-radius: 8px;\">" +
                    "<span style=\"font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #2563eb;\">" + otpCode + "</span>" +
                    "</div>" +
                    "<p style=\"color: #64748b; font-size: 14px;\">⚠️ Mã OTP này có hiệu lực trong vòng <b>5 phút</b>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\">" +
                    "<p style=\"color: #94a3b8; font-size: 12px; margin-bottom: 0;\">Nếu bạn không thực hiện yêu cầu đổi mật khẩu, vui lòng liên hệ Ban quản trị ngay lập tức.</p>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Email chứa mã OTP đổi mật khẩu đã gửi thành công tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email OTP tới {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Gửi email chứa mã OTP thất bại. Vui lòng kiểm tra lại địa chỉ email hoặc SMTP server.", e);
        }
    }
}
