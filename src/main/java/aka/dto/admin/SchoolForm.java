package aka.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolForm {

    @NotBlank(message = "Tên Trường Mầm Non không được để trống!")
    private String name;

    private String address;
    private String contactPerson;

    @Pattern(regexp = "^(0[35789][0-9]{8,9})?$", message = "Số điện thoại phải từ 10-11 chữ số và bắt đầu bằng các đầu số 03, 05, 07, 08, 09!")
    private String phone;
}
