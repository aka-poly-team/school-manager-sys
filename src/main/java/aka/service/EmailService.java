package aka.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetUrl);
}
