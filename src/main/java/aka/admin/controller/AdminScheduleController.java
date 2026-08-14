package aka.admin.controller;

import java.time.LocalTime;

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

import aka.dto.admin.ScheduleForm;
import aka.model.Schedule;
import aka.model.School;
import aka.model.SchoolClass;
import aka.model.Teacher;
import aka.repository.ScheduleRepository;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.TeacherService;
import aka.util.DateUtils;
import aka.util.StringUtils;
import aka.util.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminScheduleController {

    ScheduleService scheduleService;
    TeacherService teacherService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;
    ScheduleRepository scheduleRepository;
    @GetMapping("/schedules")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "editScheduleId", required = false) Integer editScheduleId,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Schedule> pageResult = scheduleRepository.findAll(pageable);
        
        model.addAttribute("schedules", pageResult.getContent());
        model.addAttribute("pageObj", pageResult);
        model.addAttribute("editScheduleId", editScheduleId);
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());

        return "admin/schedule/list";
    }

    @PostMapping("/schedules/edit-inline/{id}")
    public String editInline(@PathVariable("id") Integer id,
                             @RequestParam("teacherId") Integer teacherId,
                             @RequestParam("schoolId") Integer schoolId,
                             @RequestParam("classId") Integer classId,
                             @RequestParam("dayOfWeek") Integer dayOfWeek,
                             @RequestParam("session") String session,
                             @RequestParam(value = "periods", required = false, defaultValue = "2") Integer periods,
                             @RequestHeader(value = "Referer", required = false) String referer,
                             RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleService.findById(id).orElse(null);
            if (schedule != null) {
                // 1. Kiểm tra TRÙNG LỊCH GIÁO VIÊN
                boolean teacherConflict = scheduleService.existsByTeacherIdAndDayOfWeekAndSessionAndIdNot(teacherId, dayOfWeek, session, id);
                if (teacherConflict) {
                    Teacher t = teacherService.findById(teacherId).orElse(null);
                    String teacherName = t != null ? t.getName() : "Giáo viên";
                    redirectAttributes.addFlashAttribute("error", 
                        "TRÙNG LỊCH GIÁO VIÊN: Giáo viên '" + teacherName + "' đã có ca dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")!");
                    return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schedules", "editScheduleId");
                }

                // 2. Kiểm tra TRÙNG LỊCH LỚP HỌC
                boolean classConflict = scheduleService.existsBySchoolClassIdAndDayOfWeekAndSessionAndIdNot(classId, dayOfWeek, session, id);
                if (classConflict) {
                    SchoolClass c = schoolClassService.findById(classId).orElse(null);
                    String className = c != null ? c.getName() : "Lớp học";
                    redirectAttributes.addFlashAttribute("error", 
                        "TRÙNG LỊCH LỚP HỌC: Lớp '" + className + "' đã được xếp lịch dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")!");
                    return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schedules", "editScheduleId");
                }

                Teacher teacher = teacherService.findById(teacherId).orElse(null);
                School school = schoolService.findById(schoolId).orElse(null);
                SchoolClass schoolClass = schoolClassService.findById(classId).orElse(null);
                
                schedule.setTeacher(teacher);
                schedule.setSchool(school);
                schedule.setSchoolClass(schoolClass);
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setSession(session);
                schedule.setPeriods(periods != null && periods > 0 ? periods : 2);
                scheduleService.save(schedule);
                redirectAttributes.addFlashAttribute("success", "Cập nhật lịch dạy #" + id + " thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật lịch dạy: " + e.getMessage());
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schedules", "editScheduleId");
    }

    @GetMapping("/schedules/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());
        return "admin/schedule/form";
    }

    @PostMapping("/schedules/new")
    public String create(@Valid @ModelAttribute("scheduleForm") ScheduleForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/schedules";
        }

        Integer teacherId = form.getTeacherId();
        Integer schoolId = form.getSchoolId();
        Integer classId = form.getClassId();
        Integer dayOfWeek = form.getDayOfWeek();
        String session = form.getSession();
        Integer periods = form.getPeriods() != null ? form.getPeriods() : 2;

        // 1. Kiểm tra TRÙNG LỊCH GIÁO VIÊN: 1 Giáo viên không thể dạy 2 nơi/lớp cùng 1 Thứ & Ca dạy
        boolean teacherConflict = scheduleService.existsByTeacherIdAndDayOfWeekAndSession(teacherId, dayOfWeek, session);
        if (teacherConflict) {
            Teacher t = teacherService.findById(teacherId).orElse(null);
            String teacherName = t != null ? t.getName() : "Giáo viên";
            redirectAttributes.addFlashAttribute("error", 
                "TRÙNG LỊCH GIÁO VIÊN: Giáo viên '" + teacherName + "' đã có ca dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")! Vui lòng chọn ca khác.");
            return "redirect:/admin/schedules";
        }

        // 2. Kiểm tra TRÙNG LỊCH LỚP HỌC: 1 Lớp học không thể xếp 2 ca/giáo viên cùng 1 Thứ & Ca dạy
        boolean classConflict = scheduleService.existsBySchoolClassIdAndDayOfWeekAndSession(classId, dayOfWeek, session);
        if (classConflict) {
            SchoolClass c = schoolClassService.findById(classId).orElse(null);
            String className = c != null ? c.getName() : "Lớp học";
            redirectAttributes.addFlashAttribute("error", 
                "TRÙNG LỊCH LỚP HỌC: Lớp '" + className + "' đã được xếp lịch dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")! Vui lòng chọn ca khác.");
            return "redirect:/admin/schedules";
        }

        Teacher teacher = teacherService.findById(teacherId).orElse(null);
        School school = schoolService.findById(schoolId).orElse(null);
        SchoolClass schoolClass = schoolClassService.findById(classId).orElse(null);

        if (teacher != null && school != null && schoolClass != null) {
            Schedule schedule = Schedule.builder()
                    .teacher(teacher)
                    .school(school)
                    .schoolClass(schoolClass)
                    .dayOfWeek(dayOfWeek)
                    .session(session)
                    .periods(periods)
                    .startTime(startTime(session))
                    .endTime(endTime(session))
                    .build();

            scheduleService.save(schedule);
            redirectAttributes.addFlashAttribute("success", "Tạo phân công lịch dạy mới thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu phân công không hợp lệ!");
        }

        return "redirect:/admin/schedules";
    }

    @GetMapping("/schedules/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Schedule schedule = scheduleService.findById(id).orElse(null);
        if (schedule == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy phân công lịch dạy!");
            return "redirect:/admin/schedules";
        }

        ScheduleForm form = new ScheduleForm();
        form.setTeacherId(schedule.getTeacher() != null ? schedule.getTeacher().getId() : null);
        form.setSchoolId(schedule.getSchool() != null ? schedule.getSchool().getId() : null);
        form.setClassId(schedule.getSchoolClass() != null ? schedule.getSchoolClass().getId() : null);
        form.setDayOfWeek(schedule.getDayOfWeek());
        form.setSession(schedule.getSession());
        form.setPeriods(schedule.getPeriods());

        model.addAttribute("editSchedule", schedule);
        model.addAttribute("scheduleForm", form);
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());
        return "admin/schedule/form";
    }

    @PostMapping("/schedules/edit/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Valid @ModelAttribute("scheduleForm") ScheduleForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/admin/schedules";
        }

        Schedule schedule = scheduleService.findById(id).orElse(null);
        if (schedule == null) {
            redirectAttributes.addFlashAttribute("error", "Lịch dạy không tồn tại!");
            return "redirect:/admin/schedules";
        }

        Integer teacherId = form.getTeacherId();
        Integer schoolId = form.getSchoolId();
        Integer classId = form.getClassId();
        Integer dayOfWeek = form.getDayOfWeek();
        String session = form.getSession();
        Integer periods = form.getPeriods() != null ? form.getPeriods() : 2;

        boolean teacherConflict = scheduleService.existsByTeacherIdAndDayOfWeekAndSessionAndIdNot(teacherId, dayOfWeek, session, id);
        if (teacherConflict) {
            Teacher t = teacherService.findById(teacherId).orElse(null);
            String teacherName = t != null ? t.getName() : "Giáo viên";
            redirectAttributes.addFlashAttribute("error", 
                "TRÙNG LỊCH GIÁO VIÊN: Giáo viên '" + teacherName + "' đã có ca dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")!");
            return "redirect:/admin/schedules";
        }

        boolean classConflict = scheduleService.existsBySchoolClassIdAndDayOfWeekAndSessionAndIdNot(classId, dayOfWeek, session, id);
        if (classConflict) {
            SchoolClass c = schoolClassService.findById(classId).orElse(null);
            String className = c != null ? c.getName() : "Lớp học";
            redirectAttributes.addFlashAttribute("error", 
                "TRÙNG LỊCH LỚP HỌC: Lớp '" + className + "' đã được xếp lịch dạy vào " + DateUtils.dayText(dayOfWeek) + " (" + session + ")!");
            return "redirect:/admin/schedules";
        }

        Teacher teacher = teacherService.findById(teacherId).orElse(null);
        School school = schoolService.findById(schoolId).orElse(null);
        SchoolClass schoolClass = schoolClassService.findById(classId).orElse(null);

        if (teacher != null && school != null && schoolClass != null) {
            schedule.setTeacher(teacher);
            schedule.setSchool(school);
            schedule.setSchoolClass(schoolClass);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setSession(session);
            schedule.setPeriods(periods);
            schedule.setStartTime(startTime(session));
            schedule.setEndTime(endTime(session));

            scheduleService.save(schedule);
            redirectAttributes.addFlashAttribute("success", "Cập nhật phân công lịch dạy thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu cập nhật không hợp lệ!");
        }

        return "redirect:/admin/schedules";
    }

    @PostMapping("/schedules/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        try {
            if (scheduleService.existsById(id)) {
                scheduleService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Xóa phân công lịch dạy thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch dạy!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa lịch dạy này vì đã có dữ liệu điểm danh!");
        }
        return "redirect:" + StringUtils.cleanReferer(referer, "/admin/schedules", "editScheduleId");
    }

    private LocalTime startTime(String session) {
        if (session == null) return LocalTime.of(8, 0);
        return switch (session.trim().toLowerCase()) {
            case "ca 1" -> LocalTime.of(8, 0);
            case "ca 2" -> LocalTime.of(9, 30);
            case "ca 3" -> LocalTime.of(14, 0);
            case "ca 4" -> LocalTime.of(15, 30);
            default -> LocalTime.of(8, 0);
        };
    }

    private LocalTime endTime(String session) {
        if (session == null) return LocalTime.of(9, 30);
        return switch (session.trim().toLowerCase()) {
            case "ca 1" -> LocalTime.of(9, 30);
            case "ca 2" -> LocalTime.of(11, 0);
            case "ca 3" -> LocalTime.of(15, 30);
            case "ca 4" -> LocalTime.of(17, 0);
            default -> LocalTime.of(9, 30);
        };
    }
}
