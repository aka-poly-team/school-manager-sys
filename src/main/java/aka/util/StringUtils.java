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



    public static boolean isEmpty(String str) {
        return isBlank(str);
    }

    /**
     * Làm sạch URL Referer bằng cách loại bỏ các query param chỉ định (ví dụ các param edit mode như editScheduleId, editAdminId...)
     */
    public static String cleanReferer(String referer, String fallbackPath, String... paramsToRemove) {
        if (isBlank(referer)) {
            return fallbackPath;
        }
        String cleanUrl = referer;
        if (paramsToRemove != null) {
            for (String param : paramsToRemove) {
                cleanUrl = cleanUrl.replaceAll("([?&])" + param + "=[^&]*(&|$)", "$1");
            }
        }
        cleanUrl = cleanUrl.replaceAll("[?&]$", "");
        return cleanUrl;
    }

    /**
     * Chuyển đổi chuỗi thành dạng Title Case chuẩn viết hoa chữ cái đầu từng từ (Ví dụ: "nguyen Canh thang" -> "Nguyen Canh Thang")
     */
    public static String toTitleCase(String input) {
        if (isBlank(input)) {
            return input;
        }
        String[] words = input.trim().replaceAll("\\s+", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
