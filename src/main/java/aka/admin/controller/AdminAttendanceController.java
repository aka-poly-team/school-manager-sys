package aka.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Attendance;
import aka.service.AttendanceService;
import aka.service.NotificationService;
import aka.service.SystemLogService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAttendanceController {

    AttendanceService attendanceService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/attendances")
    public String list(Model model) {
        List<Attendance> attendances = attendanceService.findAllByOrderByIdDesc();
        model.addAttribute("attendances", attendances);
        return "admin/attendance/list";
    }

    @PostMapping("/attendances/approve/{id}")
    public String approve(@PathVariable("id") Long id) {
        Attendance attendance = attendanceService.findById(id).orElse(null);
        if (attendance != null) {
            attendance.setStatus("APPROVED");
            attendanceService.save(attendance);

            String teacherName = (attendance.getTeacher() != null && attendance.getTeacher().getName() != null) ? attendance.getTeacher().getName() : "Giáo viên";
            systemLogService.log(SecurityUtils.getUser(), "DUYỆT CHẤM CÔNG", 
                    "Admin vừa PHÊ DUYỆT lượt điểm danh #" + id + " của giáo viên " + teacherName + " (Ngày: " + attendance.getDate() + ")");

            if (attendance.getTeacher() != null) {
                String msg = "Lượt điểm danh ngày " + attendance.getDate() + " (" + attendance.getSession() + ") đã được Admin PHÊ DUYỆT.";
                notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/dashboard");
            }
        }
        return "redirect:/admin/attendances";
    }

    @PostMapping("/attendances/reject/{id}")
    public String reject(@PathVariable("id") Long id) {
        Attendance attendance = attendanceService.findById(id).orElse(null);
        if (attendance != null) {
            attendance.setStatus("REJECTED");
            attendanceService.save(attendance);

            String teacherName = (attendance.getTeacher() != null && attendance.getTeacher().getName() != null) ? attendance.getTeacher().getName() : "Giáo viên";
            systemLogService.log(SecurityUtils.getUser(), "TỪ CHỐI CHẤM CÔNG", 
                    "Admin vừa TỪ CHỐI lượt điểm danh #" + id + " của giáo viên " + teacherName + " (Ngày: " + attendance.getDate() + ")");

            if (attendance.getTeacher() != null) {
                String msg = "Lượt điểm danh ngày " + attendance.getDate() + " (" + attendance.getSession() + ") đã bị Admin TỪ CHỐI.";
                notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/dashboard");
            }
        }
        return "redirect:/admin/attendances";
    }
}
