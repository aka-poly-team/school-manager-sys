package aka.service;

import static org.springframework.security.core.userdetails.User.withUsername;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import aka.model.User;
import aka.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Tên đăng nhập không được để trống!");
        }

        User user = userRepository.findFirstByUsernameOrTeacherEmail(username.trim(), username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại!"));

        return withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.getEnabled())
                .authorities(user.getRole().name())
                .build();
    }
}
