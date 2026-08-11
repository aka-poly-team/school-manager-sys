package aka.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import aka.dto.teacher.ChangeRequestForm;
import aka.model.ChangeRequest;
import aka.model.Schedule;
import aka.model.School;
import aka.model.Teacher;
import aka.service.ChangeRequestService;
import aka.service.NotificationService;
import aka.service.ScheduleService;
import aka.service.SchoolService;
import aka.service.SystemLogService;
import aka.service.TeacherService;
import aka.util.DateUtils;
import aka.util.SecurityUtils;
import aka.util.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherChangeRequestController {

    ChangeRequestService changeRequestService;
    SchoolService schoolService;
    TeacherService teacherService;
    ScheduleService scheduleService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/change-requests")
    public String index(Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<ChangeRequest> changeRequests = teacherId != null 
                ? changeRequestService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("changeRequests", changeRequests);
        return "teacher/change-request/list";
    }

    @GetMapping("/change-requests/new")
    public String form(Model model) {
        List<School> schools = schoolService.findAll();
        List<Teacher> teachers = teacherService.findAll();

        model.addAttribute("schools", schools);
        model.addAttribute("teachers", teachers);
        return "teacher/change-request/form";
    }

    @PostMapping("/change-requests/new")
    public String submit(@Valid @ModelAttribute("changeRequestForm") ChangeRequestForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Teacher teacher = SecurityUtils.getTeacher();

        if (teacher == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản chưa liên kết với hồ sơ Giáo viên!");
            return "redirect:/teacher/change-requests";
        }

        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
            model.addAttribute("schools", schoolService.findAll());
            model.addAttribute("teachers", teacherService.findAll());
            return "teacher/change-request/form";
        }

        String requestType = form.getRequestType();
        String schoolIdStr = form.getSchoolId();
        String dateStr = form.getDate();
        String session = form.getSession();
        String reason = form.getReason();

        Integer schoolId = (schoolIdStr != null && !schoolIdStr.isBlank()) ? Integer.parseInt(schoolIdStr) : null;
        LocalDate reqDate = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();

        // Tính dayOfWeek của ngày xin nghỉ (2 = Thứ 2, ..., 8 = Chủ nhật)
        int dayOfWeek = DateUtils.toCustomDayOfWeek(reqDate);

        // KIỂM TRA RÀNG BUỘC: Giáo viên BẮT BUỘC phải có Lịch dạy được phân công vào Thứ + Ca dạy (và Trường) này mới được xin nghỉ
        boolean hasSchedule = (schoolId != null) 
                ? scheduleService.existsByTeacherIdAndSchoolIdAndDayOfWeekAndSession(teacher.getId(), schoolId, dayOfWeek, session)
                : scheduleService.existsByTeacherIdAndDayOfWeekAndSession(teacher.getId(), dayOfWeek, session);

        if (!hasSchedule) {
            String dayText = DateUtils.dayText(dayOfWeek);
            model.addAttribute("error", 
                    "KHÔNG CÓ LỊCH DẠY: Bạn không có ca dạy nào được phân công vào " + dayText + " (" + session + ")! Không thể gửi yêu cầu xin nghỉ.");
            model.addAttribute("schools", schoolService.findAll());
            model.addAttribute("teachers", teacherService.findAll());
            return "teacher/change-request/form";
        }

            List<Schedule> teacherSchedules = scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacher.getId());
            Schedule matchedSchedule = teacherSchedules.stream().filter(s -> 
                s.getDayOfWeek() == dayOfWeek && s.getSession() != null && s.getSession().equalsIgnoreCase(session)
            ).findFirst().orElse(null);

            ChangeRequest cr = ChangeRequest.builder()
                    .teacher(teacher)
                    .requestType(requestType)
                    .date(reqDate)
                    .session(session)
                    .schedule(matchedSchedule)
                    .reason(reason)
                    .status("pending")
                    .build();

            changeRequestService.save(cr);

        systemLogService.log(SecurityUtils.getUser(), "TẠO ĐƠN XIN NGHỈ", 
                "Nộp đơn xin nghỉ phép / đổi ca cho ngày " + reqDate + " (" + session + ")");

            // BẮN THÔNG BÁO CHO ADMIN
            String teacherDisplayName = (teacher != null && teacher.getName() != null) ? teacher.getName() : "Giáo viên";
            notificationService.notifyAdmin("Giáo viên " + teacherDisplayName + " vừa nộp đơn xin nghỉ / đổi ca mới cho ngày " + reqDate + ".", "/admin/change-requests");

            redirectAttributes.addFlashAttribute("success", "Gửi đơn xin nghỉ / đổi ca thành công! Vui lòng chờ Admin phê duyệt.");
        return "redirect:/teacher/change-requests";
    }
}
