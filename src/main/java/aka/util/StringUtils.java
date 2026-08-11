package aka.util;

import java.util.Objects;

/**
 * Thư viện tiện ích xử lý Chuỗi & Đối tượng Null-Safe chuẩn Clean Code.
 */
public class StringUtils {

    private StringUtils() {
        // Utility class
    }

    /**
     * Kiểm tra chuỗi bị Null hoặc rỗng
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Kiểm tra chuỗi tồn tại và chứa ký tự hợp lệ (không rỗng)
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Lấy giá trị chuỗi, nếu rỗng thì trả về giá trị mặc định
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isNotBlank(str) ? str.trim() : defaultStr;
    }

    /**
     * Lấy giá trị đối tượng, nếu Null thì trả về giá trị mặc định
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return Objects.requireNonNullElse(value, defaultValue);
    }

    /**
     * Anonymize email cá nhân
     */
    public static String mask(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        return name.length() <= 2 
                ? name.charAt(0) + "****@" + domain 
                : name.substring(0, 2) + "****@" + domain;
    }

    public static boolean isEmpty(String str) {
        return isBlank(str);
    }
}
