package aka.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherProfileForm {

    @NotBlank(message = "Họ và tên Giáo viên không được để trống!")
    private String name;

    private String phone;
}
