package aka.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRequestForm {

    @NotBlank(message = "Vui lòng chọn loại yêu cầu!")
    private String requestType;

    private String schoolId;
    private String date;

    @NotBlank(message = "Vui lòng chọn ca dạy!")
    private String session;

    @NotBlank(message = "Vui lòng nhập lý do chi tiết!")
    private String reason;
}
