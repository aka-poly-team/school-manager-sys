package aka.dto.teacher;

import aka.util.ValidationUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {

    @NotBlank(message = "Vui lòng nhập Email tài khoản!")
    @Email(message = "Email không đúng định dạng!")
    @Pattern(regexp = ValidationUtils.REGEX_GMAIL, message = ValidationUtils.MSG_GMAIL)
    private String email;

    @NotBlank(message = "Vui lòng nhập Mật khẩu hiện tại!")
    private String currentPassword;

    @NotBlank(message = "Vui lòng nhập Mật khẩu mới!")
    @Size(min = 6, message = "Mật khẩu mới phải có tối thiểu 6 ký tự!")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại Mật khẩu mới!")
    private String confirmPassword;
}
