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
 * File Validate chung cho toàn bộ ứng dụng.
 * Sử dụng thư viện Jakarta Bean Validation (từ pom.xml) & Spring BindingResult.
 */
public class ValidationUtils {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    private ValidationUtils() {
        // Utility class
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
