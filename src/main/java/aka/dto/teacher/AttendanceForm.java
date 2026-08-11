package aka.dto.teacher;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceForm {

    @NotNull(message = "Vui lòng chọn Trường mầm non!")
    private Integer schoolId;

    @NotNull(message = "Vui lòng chọn Lớp học!")
    private Integer classId;

    @NotBlank(message = "Vui lòng chọn Ca dạy!")
    private String session;

    @NotNull(message = "Vui lòng nhập số tiết đã dạy!")
    @Min(value = 1, message = "Số tiết tối thiểu là 1!")
    @Max(value = 10, message = "Số tiết tối đa là 10!")
    private Integer periods = 2;

    private String notes;

    @NotNull(message = "Vui lòng tải lên Ảnh xác minh điểm danh!")
    private MultipartFile selfieFile;
}
