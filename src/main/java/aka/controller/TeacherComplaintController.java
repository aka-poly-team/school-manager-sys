package aka.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.dto.teacher.ComplaintForm;
import aka.model.Attendance;
import aka.model.Complaint;
import aka.model.Teacher;
import aka.service.AttendanceService;
import aka.service.ComplaintService;
import aka.service.NotificationService;
import aka.service.SystemLogService;
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
public class TeacherComplaintController {

    ComplaintService complaintService;
    AttendanceService attendanceService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/complaints")
    public String index(Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Complaint> complaints = teacherId != null 
                ? complaintService.findByAttendanceTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("complaints", complaints);
        return "teacher/complaint/list";
    }

    @GetMapping("/complaints/new")
    public String form(Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Attendance> attendances = teacherId != null 
                ? attendanceService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("attendances", attendances);
        return "teacher/complaint/form";
    }

    @PostMapping("/complaints/new")
    public String submit(@Valid @ModelAttribute("complaintForm") ComplaintForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Teacher teacher = SecurityUtils.getTeacher();

        if (teacher == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản chưa liên kết với hồ sơ Giáo viên!");
            return "redirect:/teacher/complaints";
        }

        String errorMsg = ValidationUtils.getFirstError(bindingResult);
        if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
            model.addAttribute("attendances", attendanceService.findByTeacherIdOrderByIdDesc(teacher.getId()));
            return "teacher/complaint/form";
        }

        Long attendanceId = form.getAttendanceId();
        String content = form.getContent();
        Integer expectedPeriods = form.getExpectedPeriods() != null ? form.getExpectedPeriods() : 2;

        // Kiểm tra CHỐNG TRÙNG KHIẾU NẠI: Buổi dạy này đã được gửi khiếu nại trước đó chưa
        if (attendanceId != null && complaintService.existsByAttendanceId(attendanceId)) {
            model.addAttribute("error", "TRÙNG LỊCH KHIẾU NẠI: Buổi dạy này đã được gửi khiếu nại trước đó! Vui lòng không gửi lặp lại.");
            model.addAttribute("attendances", attendanceService.findByTeacherIdOrderByIdDesc(teacher.getId()));
            return "teacher/complaint/form";
        }

        Attendance attendance = attendanceService.findById(attendanceId).orElse(null);

        Complaint complaint = Complaint.builder()
                .attendance(attendance)
                .content(content)
                .expectedPeriods(expectedPeriods)
                .status(0)
                .build();

        complaintService.save(complaint);

        systemLogService.log(SecurityUtils.getUser(), "GỬI KHIẾU NẠI", 
                "Gửi khiếu nại tiết dạy cho ca ngày " + (attendance != null ? attendance.getDate() : "N/A"));

        // BẮN THÔNG BÁO CHO ADMIN
        String teacherDisplayName = (teacher != null && teacher.getName() != null) ? teacher.getName() : "Giáo viên";
        notificationService.notifyAdmin("Giáo viên " + teacherDisplayName + " vừa gửi đơn khiếu nại tiết dạy mới cho ca dạy ngày " + (attendance != null ? attendance.getDate() : "") + ".", "/admin/complaints");

        redirectAttributes.addFlashAttribute("success", "Gửi khiếu nại thành công! Vui lòng chờ Admin xử lý.");
        return "redirect:/teacher/complaints";
    }
}
