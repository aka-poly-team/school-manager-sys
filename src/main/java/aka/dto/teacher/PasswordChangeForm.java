package aka.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {

    @NotBlank(message = "Vui lòng nhập Mật khẩu hiện tại!")
    private String currentPassword;

    @NotBlank(message = "Vui lòng nhập Mật khẩu mới!")
    @Size(min = 6, message = "Mật khẩu mới phải có tối thiểu 6 ký tự!")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại Mật khẩu mới!")
    private String confirmPassword;
}
