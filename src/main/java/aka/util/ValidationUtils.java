package aka.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.validation.BindingResult;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * File Validate chung và tập trung toàn bộ Constants / Quy tắc kiểm tra cho toàn bộ ứng dụng.
 * Sử dụng thư viện Jakarta Bean Validation & Spring BindingResult.
 */
public class ValidationUtils {

    // 1. CHUỖI REGEX VÀ THÔNG BÁO LỖI DÙNG CHUNG CỦA CÁC TRƯỜNG DỮ LIỆU
    public static final String REGEX_USERNAME = "^([a-zA-Z][a-zA-Z0-9._@\\-]{4,})?$";
    public static final String REGEX_GMAIL = "^([a-zA-Z0-9._%+-]+@gmail\\.com)?$";
    public static final String REGEX_PHONE = "^(0[35789][0-9]{8,9})?$";

    public static final String MSG_USERNAME = "Tên đăng nhập phải có ít nhất 5 ký tự và bắt đầu bằng chữ cái!";
    public static final String MSG_NAME = "Họ và tên không được để trống và không được chứa chữ số!";
    public static final String MSG_GMAIL = "Email phải đúng định dạng và có đuôi @gmail.com!";
    public static final String MSG_PHONE = "Số điện thoại phải từ 10-11 chữ số và bắt đầu bằng các đầu số 03, 05, 07, 08, 09!";
    public static final String MSG_PASSWORD = "Mật khẩu là bắt buộc và phải từ 6 ký tự trở lên!";

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Kiểm tra Họ và Tên hợp lệ (không rỗng, không chứa chữ số)
     */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && !name.matches(".*\\d.*");
    }

    /**
     * Kiểm tra Email hợp lệ (đuôi @gmail.com)
     */
    public static boolean isValidGmail(String email) {
        if (email == null || email.isBlank()) return true;
        return email.toLowerCase().endsWith("@gmail.com");
    }

    /**
     * Kiểm tra Số điện thoại hợp lệ (03, 05, 07, 08, 09; 10-11 số)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true;
        String cleanPhone = phone.replaceAll("\\D", "");
        return cleanPhone.matches("^(0[35789][0-9]{8,9})$");
    }

    /**
     * Validate bất kỳ DTO / Object nào sử dụng Annotation Bean Validation (@NotBlank, @NotNull,...)
     * @return Danh sách các thông báo lỗi (trống nếu hợp lệ)
     */
    public static <T> List<String> validate(T object) {
        if (object == null) {
            return List.of("Dữ liệu cần kiểm tra không được để trống!");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getMessage());
        }
        return errors;
    }

    /**
     * Lấy ra thông báo lỗi đầu tiên của DTO / Object
     * @return Chuỗi thông báo lỗi đầu tiên, hoặc null nếu hợp lệ
     */
    public static <T> String getFirstError(T object) {
        List<String> errors = validate(object);
        return errors.isEmpty() ? null : errors.get(0);
    }

    /**
     * Kiểm tra xem DTO / Object có hợp lệ 100% không
     */
    public static <T> boolean isValid(T object) {
        return object != null && VALIDATOR.validate(object).isEmpty();
    }

    /**
     * Lấy ra lỗi đầu tiên từ Spring MVC BindingResult
     */
    public static String getFirstError(BindingResult bindingResult) {
        return (bindingResult != null && bindingResult.hasErrors()) 
                ? bindingResult.getAllErrors().get(0).getDefaultMessage() 
                : null;
    }

    /**
     * Lấy tất cả các câu thông báo lỗi từ Spring MVC BindingResult
     */
    public static List<String> getAllErrors(BindingResult bindingResult) {
        if (bindingResult == null || !bindingResult.hasErrors()) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        return errors;
    }
}
