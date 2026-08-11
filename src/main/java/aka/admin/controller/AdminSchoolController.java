package aka.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.dto.admin.SchoolClassForm;
import aka.dto.admin.SchoolForm;
import aka.model.School;
import aka.model.SchoolClass;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.util.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminSchoolController {

    SchoolService schoolService;
    SchoolClassService schoolClassService;

    @GetMapping("/schools")
    public String list(Model model) {
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());
        return "admin/school/list";
    }

    @GetMapping("/schools/new")
    public String showCreateSchoolForm(Model model) {
        return "admin/school/school-form";
    }

    @GetMapping("/classes/new")
    public String showCreateClassForm(Model model) {
        model.addAttribute("schools", schoolService.findAll());
        return "admin/school/class-form";
    }

    @PostMapping("/schools/new")
    public String createSchool(@Valid @ModelAttribute("schoolForm") SchoolForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/schools";
        }

        School school = School.builder()
                .name(form.getName().trim())
                .address(form.getAddress())
                .contactPerson(form.getContactPerson())
                .phone(form.getPhone())
                .build();
        schoolService.save(school);
        redirectAttributes.addFlashAttribute("success", "Thêm trường mầm non mới thành công!");

        return "redirect:/admin/schools";
    }

    @PostMapping("/classes/new")
    public String createClass(@Valid @ModelAttribute("schoolClassForm") SchoolClassForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/schools";
        }

        School school = schoolService.findById(form.getSchoolId()).orElse(null);
        if (school != null) {
            SchoolClass schoolClass = SchoolClass.builder()
                    .school(school)
                    .name(form.getName().trim())
                    .studentCount(form.getStudentCount() != null ? form.getStudentCount() : 20)
                    .standardPeriods(form.getStandardPeriods() != null ? form.getStandardPeriods() : 2)
                    .build();
            schoolClassService.save(schoolClass);
            redirectAttributes.addFlashAttribute("success", "Thêm lớp học mới thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy Trường mầm non đã chọn!");
        }
        return "redirect:/admin/schools";
    }
}
