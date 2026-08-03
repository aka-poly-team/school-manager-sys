package aka.admin.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.Schedule;
import aka.model.School;
import aka.model.SchoolClass;
import aka.model.Teacher;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminScheduleController {

    UserService userService;
    ScheduleService scheduleService;
    TeacherService teacherService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;

    @GetMapping("/schedules")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        List<Schedule> schedules = scheduleService.findAllByOrderByIdDesc();
        
        model.addAttribute("schedules", schedules);
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("schools", schoolService.findAll());
        model.addAttribute("classes", schoolClassService.findAll());

        return "admin/schedules";
    }

    @PostMapping("/schedules/new")
    public String create(@RequestParam("teacherId") Integer teacherId,
                         @RequestParam("schoolId") Integer schoolId,
                         @RequestParam("classId") Integer classId,
                         @RequestParam("dayOfWeek") Integer dayOfWeek,
                         @RequestParam("session") String session,
                         @RequestParam(value = "periods", required = false, defaultValue = "2") Integer periods,
                         RedirectAttributes redirectAttributes) {

        boolean exists = scheduleService.existsByTeacherIdAndSchoolIdAndSchoolClassIdAndDayOfWeekAndSession(
                teacherId, schoolId, classId, dayOfWeek, session);
        if (exists) {
            redirectAttributes.addFlashAttribute("error", "Phân công lịch dạy này đã tồn tại!");
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
            redirectAttributes.addFlashAttribute("success", "Tạo phân công lịch dạy thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
        }

        return "redirect:/admin/schedules";
    }

    @PostMapping("/schedules/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (scheduleService.existsById(id)) {
                scheduleService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Xóa lịch dạy thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch dạy!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa lịch dạy này vì đã có dữ liệu điểm danh!");
        }
        return "redirect:/admin/schedules";
    }

    private LocalTime startTime(String session) {
        if (session == null) return LocalTime.of(8, 0);
        return switch (session.trim().toLowerCase()) {
            case "ca 1" -> LocalTime.of(8, 0);
            case "ca 2" -> LocalTime.of(10, 0);
            case "ca 3" -> LocalTime.of(14, 0);
            default -> LocalTime.of(8, 0);
        };
    }

    private LocalTime endTime(String session) {
        if (session == null) return LocalTime.of(9, 30);
        return switch (session.trim().toLowerCase()) {
            case "ca 1" -> LocalTime.of(9, 30);
            case "ca 2" -> LocalTime.of(11, 30);
            case "ca 3" -> LocalTime.of(15, 30);
            default -> LocalTime.of(9, 30);
        };
    }
}
