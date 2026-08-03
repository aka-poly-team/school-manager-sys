package aka.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.PasswordResetToken;
import aka.model.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    void deleteByUser(User user);
}
