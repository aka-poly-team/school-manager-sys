package aka;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ApplicationTests {

    @Test
    void printHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123456");
        System.out.println("BCRYPT_HASH_FOR_123456=" + hash);
        System.out.println("MATCHES=" + encoder.matches("123456", hash));
    }
}
