package aka.dto.admin;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import aka.util.ValidationUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherForm {

    @Pattern(regexp = ValidationUtils.REGEX_USERNAME, message = ValidationUtils.MSG_USERNAME)
    private String username;

    @NotBlank(message = ValidationUtils.MSG_NAME)
    private String name;

    @NotNull(message = "Ngày sinh là bắt buộc, vui lòng chọn ngày sinh!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Email(message = "Email không đúng định dạng!")
    @Pattern(regexp = ValidationUtils.REGEX_GMAIL, message = ValidationUtils.MSG_GMAIL)
    private String email;

    @Pattern(regexp = ValidationUtils.REGEX_PHONE, message = ValidationUtils.MSG_PHONE)
    private String phone;

    private String address;

    private String status = "active";

    private String password;

    private String role = "ROLE_TEACHER";

    private Boolean enabled = true;

    private MultipartFile avatarFile;
}
