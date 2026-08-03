package aka.service;

import java.util.List;
import java.util.Optional;

import aka.model.User;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Integer id);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String identifier);
    User save(User user);
    void deleteById(Integer id);
    void updatePassword(User user, String newRawPassword);

    boolean existsByUsername(String username);
    long count();
    Optional<User> findFirstByTeacherEmail(String email);
    Optional<User> findFirstByUsernameOrTeacherEmail(String username, String email);
}
