package aka.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolClassForm {

    @NotNull(message = "Vui lòng chọn Trường mầm non!")
    private Integer schoolId;

    @NotBlank(message = "Tên Lớp học không được để trống!")
    private String name;

    @Min(value = 1, message = "Sĩ số học sinh phải lớn hơn 0!")
    private Integer studentCount = 25;

    @Min(value = 1, message = "Số tiết chuẩn phải lớn hơn 0!")
    private Integer standardPeriods = 2;
}
