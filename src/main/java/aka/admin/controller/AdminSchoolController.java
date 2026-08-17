package aka.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.School;
import aka.model.SchoolClass;
import aka.repository.SchoolClassRepository;
import aka.repository.SchoolRepository;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.util.StringUtils;
import aka.util.ValidationUtils;
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
    SchoolRepository schoolRepository;
    SchoolClassRepository schoolClassRepository;

    @GetMapping("/schools")
    public String list(@RequestParam(value = "schoolPage", defaultValue = "0") int schoolPage,
                       @RequestParam(value = "classPage", defaultValue = "0") int classPage,
                       @RequestParam(value = "schoolKeyword", required = false) String schoolKeyword,
                       @RequestParam(value = "classKeyword", required = false) String classKeyword,
                       @RequestParam(value = "editSchoolId", required = false) Integer editSchoolId,
                       @RequestParam(value = "editClassId", required = false) Integer editClassId,
                       Model model) {

        Pageable schoolPageable = PageRequest.of(schoolPage, 5, Sort.by("id").descending());
        Pageable classPageable = PageRequest.of(classPage, 5, Sort.by("id").descending());

        Page<School> schoolPageResult = (schoolKeyword != null && !schoolKeyword.isBlank())
                ? schoolRepository.findByNameContainingIgnoreCase(schoolKeyword.trim(), schoolPageable)
                : schoolRepository.findAll(schoolPageable);

        Page<SchoolClass> classPageResult = (classKeyword != null && !classKeyword.isBlank())
                ? schoolClassRepository.findByNameContainingIgnoreCase(classKeyword.trim(), classPageable)
                : schoolClassRepository.findAll(classPageable);

        model.addAttribute("schools", schoolPageResult.getContent());
        model.addAttribute("schoolPageObj", schoolPageResult);
        model.addAttribute("schoolKeyword", schoolKeyword);
        model.addAttribute("editSchoolId", editSchoolId);

        model.addAttribute("classes", classPageResult.getContent());
        model.addAttribute("classPageObj", classPageResult);
        model.addAttribute("classKeyword", classKeyword);
        model.addAttribute("editClassId", editClassId);

        model.addAttribute("allSchools", schoolService.findAll());

        return "admin/school/list";
    }

    @GetMapping("/schools/new")
    public String showCreateSchoolForm(Model model) {
        model.addAttribute("school", null);
        return "admin/school/school-form";
    }

    @GetMapping("/schools/edit/{id}")
    public String showEditSchoolForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        School school = schoolService.findById(id).orElse(null);
        if (school == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy trường mầm non #" + id);
            return "redirect:/admin/schools";
        }
        model.addAttribute("school", school);
        return "admin/school/school-form";
    }

    @PostMapping("/schools/new")
    public String createSchool(@RequestParam("name") String name,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "contactPerson", required = false) String contactPerson,
                               @RequestParam(value = "phone", required = false) String phone,
                               RedirectAttributes redirectAttributes) {

        try {
            if (name != null && name.trim().matches("^\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên trường mầm non phải bắt đầu bằng chữ cái (không được bắt đầu bằng chữ số)!");
                return "redirect:/admin/schools";
            }
            if (contactPerson != null && contactPerson.matches(".*\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên người liên hệ không được chứa chữ số!");
                return "redirect:/admin/schools";
            }

            School school = School.builder()
                    .name(StringUtils.toTitleCase(name))
                    .address(StringUtils.toTitleCase(address))
                    .contactPerson(StringUtils.toTitleCase(contactPerson))
                    .phone(phone)
                    .build();
            schoolService.save(school);
            redirectAttributes.addFlashAttribute("success", "Thêm trường mầm non '" + school.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm trường: " + e.getMessage());
        }

        return "redirect:/admin/schools";
    }

    @PostMapping("/schools/edit/{id}")
    public String updateSchool(@PathVariable("id") Integer id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "contactPerson", required = false) String contactPerson,
                               @RequestParam(value = "phone", required = false) String phone,
                               @RequestHeader(value = "Referer", required = false) String referer,
                               RedirectAttributes redirectAttributes) {
        School school = schoolService.findById(id).orElse(null);
        if (school == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy trường mầm non #" + id);
            return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
        }

        try {
            // 1. Validate Tên trường
            if (name == null || name.isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Tên trường mầm non không được để trống!");
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
            }
            if (name.trim().matches("^\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên trường mầm non phải bắt đầu bằng chữ cái (không được bắt đầu bằng chữ số)!");
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
            }

            // 2. Validate Người đại diện
            if (contactPerson != null && contactPerson.matches(".*\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên người đại diện không được chứa chữ số!");
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
            }

            // 3. Validate Số điện thoại (03, 05, 07, 08, 09; 10-11 số)
            if (phone != null) {
                phone = phone.replaceAll("\\D", "");
                if (phone.length() > 11) {
                    phone = phone.substring(0, 11);
                }
            }
            if (phone != null && !phone.isBlank() && !phone.matches("^(0[35789][0-9]{8,9})$")) {
                redirectAttributes.addFlashAttribute("error", "Số điện thoại phải từ 10-11 chữ số và bắt đầu bằng 03, 05, 07, 08, 09!");
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
            }

            school.setName(StringUtils.toTitleCase(name));
            school.setAddress(StringUtils.toTitleCase(address));
            school.setContactPerson(StringUtils.toTitleCase(contactPerson));
            school.setPhone(phone);
            schoolService.save(school);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin trường '" + school.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trường: " + e.getMessage());
        }

        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
    }

    @PostMapping("/schools/delete/{id}")
    public String deleteSchool(@PathVariable("id") Integer id,
                               @RequestHeader(value = "Referer", required = false) String referer,
                               RedirectAttributes redirectAttributes) {
        try {
            School targetSchool = schoolService.findById(id).orElse(null);
            String schoolName = targetSchool != null ? targetSchool.getName() : ("#" + id);

            schoolService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa trường mầm non '" + schoolName + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa trường #" + id + " (Có thể đang chứa Lớp học hoặc Lịch dạy).");
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editSchoolId");
    }

    @GetMapping("/classes/new")
    public String showCreateClassForm(Model model) {
        model.addAttribute("schoolClass", null);
        model.addAttribute("schools", schoolService.findAll());
        return "admin/school/class-form";
    }

    @GetMapping("/classes/edit/{id}")
    public String showEditClassForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        SchoolClass schoolClass = schoolClassService.findById(id).orElse(null);
        if (schoolClass == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
            return "redirect:/admin/schools";
        }
        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("schools", schoolService.findAll());
        return "admin/school/class-form";
    }

    @PostMapping("/classes/new")
    public String createClass(@RequestParam("schoolId") Integer schoolId,
                               @RequestParam("name") String name,
                               @RequestParam(value = "studentCount", defaultValue = "25") Integer studentCount,
                               @RequestParam(value = "standardPeriods", defaultValue = "1") Integer standardPeriods,
                               RedirectAttributes redirectAttributes) {

        try {
            if (name != null && name.trim().matches("^\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên lớp học phải bắt đầu bằng chữ cái (không được bắt đầu bằng chữ số)!");
                return "redirect:/admin/schools";
            }

            School school = schoolService.findById(schoolId).orElse(null);
            if (school == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn Trường mầm non hợp lệ!");
                return "redirect:/admin/schools";
            }

            SchoolClass schoolClass = SchoolClass.builder()
                    .name(StringUtils.toTitleCase(name))
                    .school(school)
                    .studentCount(studentCount != null ? studentCount : 0)
                    .standardPeriods(standardPeriods != null ? standardPeriods : 1)
                    .build();
            schoolClassService.save(schoolClass);
            redirectAttributes.addFlashAttribute("success", "Thêm lớp học '" + schoolClass.getName() + "' cho trường '" + school.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm lớp học: " + e.getMessage());
        }

        return "redirect:/admin/schools";
    }

    @PostMapping("/classes/edit/{id}")
    public String updateClass(@PathVariable("id") Integer id,
                              @RequestParam("schoolId") Integer schoolId,
                              @RequestParam("name") String name,
                              @RequestParam(value = "studentCount", defaultValue = "25") Integer studentCount,
                              @RequestParam(value = "standardPeriods", defaultValue = "1") Integer standardPeriods,
                              @RequestHeader(value = "Referer", required = false) String referer,
                              RedirectAttributes redirectAttributes) {
        SchoolClass schoolClass = schoolClassService.findById(id).orElse(null);
        if (schoolClass == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học!");
            return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editClassId");
        }

        try {
            if (name != null && name.trim().matches("^\\d.*")) {
                redirectAttributes.addFlashAttribute("error", "Tên lớp học phải bắt đầu bằng chữ cái (không được bắt đầu bằng chữ số)!");
                return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editClassId");
            }
            School school = schoolService.findById(schoolId).orElse(null);
            if (school != null) {
                schoolClass.setSchool(school);
            }
            schoolClass.setName(StringUtils.toTitleCase(name));
            schoolClass.setStudentCount(studentCount);
            schoolClass.setStandardPeriods(standardPeriods);
            schoolClassService.save(schoolClass);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin lớp '" + schoolClass.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật lớp học: " + e.getMessage());
        }

        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editClassId");
    }

    @PostMapping("/classes/delete/{id}")
    public String deleteClass(@PathVariable("id") Integer id,
                              @RequestHeader(value = "Referer", required = false) String referer,
                              RedirectAttributes redirectAttributes) {
        try {
            SchoolClass targetClass = schoolClassService.findById(id).orElse(null);
            String className = targetClass != null ? targetClass.getName() : ("#" + id);

            schoolClassService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa lớp học '" + className + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa lớp #" + id + " (Có thể đang chứa Lịch dạy).");
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schools", "editClassId");
    }
}
