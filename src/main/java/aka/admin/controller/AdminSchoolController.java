package aka.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.School;
import aka.model.SchoolClass;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminSchoolController {

    UserService userService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;

    @GetMapping("/schools")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());
        return "admin/schools";
    }

    @PostMapping("/schools/new")
    public String createSchool(@RequestParam("name") String name,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "contactPerson", required = false) String contactPerson,
                               @RequestParam(value = "phone", required = false) String phone,
                               RedirectAttributes redirectAttributes) {
        if (name != null && !name.isBlank()) {
            School school = School.builder()
                    .name(name.trim())
                    .address(address)
                    .contactPerson(contactPerson)
                    .phone(phone)
                    .build();
            schoolService.save(school);
            redirectAttributes.addFlashAttribute("success", "Thêm trường mầm non mới thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Tên trường mầm non không được để trống!");
        }
        return "redirect:/admin/schools";
    }

    @PostMapping("/classes/new")
    public String createClass(@RequestParam("schoolId") Integer schoolId,
                              @RequestParam("name") String name,
                              @RequestParam(value = "studentCount", required = false, defaultValue = "20") Integer studentCount,
                              @RequestParam(value = "standardPeriods", required = false, defaultValue = "2") Integer standardPeriods,
                              RedirectAttributes redirectAttributes) {
        School school = schoolService.findById(schoolId).orElse(null);
        if (school != null && name != null && !name.isBlank()) {
            SchoolClass schoolClass = SchoolClass.builder()
                    .school(school)
                    .name(name.trim())
                    .studentCount(studentCount)
                    .standardPeriods(standardPeriods)
                    .build();
            schoolClassService.save(schoolClass);
            redirectAttributes.addFlashAttribute("success", "Thêm lớp học mới thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu trường hoặc tên lớp không hợp lệ!");
        }
        return "redirect:/admin/schools";
    }
}
