package aka.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findFirstByTeacherEmail(String email);
    Optional<User> findFirstByUsernameOrTeacherEmail(String username, String email);
    Optional<User> findByUsernameOrTeacherEmail(String username, String email);
}
