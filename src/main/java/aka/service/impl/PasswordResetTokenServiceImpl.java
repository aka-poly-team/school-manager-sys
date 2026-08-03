package aka.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.PasswordResetToken;
import aka.model.User;
import aka.repository.PasswordResetTokenRepository;
import aka.service.PasswordResetTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PasswordResetToken> findAll() {
        return passwordResetTokenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findById(Integer id) {
        return passwordResetTokenRepository.findById(id);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        return passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public void deleteById(Integer id) {
        passwordResetTokenRepository.deleteById(id);
    }

    @Override
    public PasswordResetToken createTokenForUser(User user) {
        deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        return passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidToken(PasswordResetToken resetToken) {
        if (resetToken == null || resetToken.getExpiryDate() == null) return false;
        return resetToken.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Override
    public void deleteToken(PasswordResetToken token) {
        if (token != null) {
            passwordResetTokenRepository.delete(token);
        }
    }

    @Override
    public void deleteByUser(User user) {
        if (user != null) {
            passwordResetTokenRepository.deleteByUser(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getCooldownSecondsRemaining(User user, int cooldownSeconds) {
        if (user == null) return 0;
        Optional<PasswordResetToken> latestTokenOpt = passwordResetTokenRepository.findByUser(user);
        if (latestTokenOpt.isEmpty()) return 0;

        PasswordResetToken latestToken = latestTokenOpt.get();
        LocalDateTime expiryDate = latestToken.getExpiryDate();
        if (expiryDate == null) return 0;

        LocalDateTime createdAt = expiryDate.minusMinutes(15);
        LocalDateTime nextAllowedTime = createdAt.plusSeconds(cooldownSeconds);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(nextAllowedTime)) {
            return java.time.Duration.between(now, nextAllowedTime).getSeconds();
        }
        return 0;
    }
}
