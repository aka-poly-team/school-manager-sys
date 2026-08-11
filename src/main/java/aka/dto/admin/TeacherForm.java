package aka.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherForm {

    private String username;

    @NotBlank(message = "Họ và tên Giáo viên không được để trống!")
    private String name;

    @Email(message = "Email không đúng định dạng!")
    private String email;

    private String phone;
    private String password;
    private String role = "ROLE_TEACHER";
}
