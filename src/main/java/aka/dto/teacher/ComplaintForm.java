package aka.dto.teacher;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintForm {

    @NotNull(message = "Vui lòng chọn Buổi dạy cần khiếu nại!")
    private Long attendanceId;

    @NotBlank(message = "Nội dung khiếu nại không được để trống!")
    private String content;

    @NotNull(message = "Vui lòng nhập số tiết thực tế yêu cầu!")
    @Min(value = 1, message = "Số tiết tối thiểu là 1!")
    @Max(value = 10, message = "Số tiết tối đa là 10!")
    private Integer expectedPeriods = 2;
}
