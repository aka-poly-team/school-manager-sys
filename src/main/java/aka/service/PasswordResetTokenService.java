package aka.service;

import java.util.List;
import java.util.Optional;

import aka.model.PasswordResetToken;
import aka.model.User;

public interface PasswordResetTokenService {
    List<PasswordResetToken> findAll();
    Optional<PasswordResetToken> findById(Integer id);
    PasswordResetToken save(PasswordResetToken passwordResetToken);
    void deleteById(Integer id);

    PasswordResetToken createTokenForUser(User user);
    Optional<PasswordResetToken> findByToken(String token);
    boolean isValidToken(PasswordResetToken resetToken);
    void deleteToken(PasswordResetToken token);
    void deleteByUser(User user);
    long getCooldownSecondsRemaining(User user, int cooldownSeconds);
}
