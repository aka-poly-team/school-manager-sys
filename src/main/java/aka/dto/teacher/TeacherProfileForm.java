package aka.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherProfileForm {

    @NotBlank(message = "Họ và tên Giáo viên không được để trống!")
    private String name;

    @Pattern(regexp = "^(0[35789][0-9]{8,9})?$", message = "Số điện thoại phải từ 10-11 chữ số và bắt đầu bằng các đầu số 03, 05, 07, 08, 09!")
    private String phone;
}
