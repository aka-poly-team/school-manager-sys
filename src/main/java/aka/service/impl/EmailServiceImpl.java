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
}
