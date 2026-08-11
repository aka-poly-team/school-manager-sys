package aka.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolForm {

    @NotBlank(message = "Tên Trường Mầm Non không được để trống!")
    private String name;

    private String address;
    private String contactPerson;
    private String phone;
}
