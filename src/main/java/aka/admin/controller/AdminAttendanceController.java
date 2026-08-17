package aka.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.model.Attendance;
import aka.repository.AttendanceRepository;
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
    AttendanceRepository attendanceRepository;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/attendances")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Attendance> pageResult = attendanceRepository.findAll(pageable);

        model.addAttribute("attendances", pageResult.getContent());
        model.addAttribute("pageObj", pageResult);
        return "admin/attendance/list";
    }

    // GỘP 2 THAO TÁC (PHÊ DUYỆT & TỪ CHỐI) THÀNH 1 PHƯƠNG THỨC DUY NHẤT DÙNG IF-ELSE
    @PostMapping({"/attendances/approve/{id}", "/attendances/reject/{id}", "/attendances/process/{id}"})
    public String processAttendance(@PathVariable("id") Long id,
                                    @RequestParam(value = "action", required = false) String action,
                                    jakarta.servlet.http.HttpServletRequest request,
                                    @RequestHeader(value = "Referer", required = false) String referer,
                                    RedirectAttributes redirectAttributes) {
        Attendance attendance = attendanceService.findById(id).orElse(null);
        if (attendance == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lượt điểm danh #" + id);
            return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/attendances");
        }

        // Dùng IF-ELSE để phân biệt Duyệt hay Từ chối
        boolean isApprove = "approve".equalsIgnoreCase(action) || request.getRequestURI().contains("/approve");
        String newStatus = isApprove ? "APPROVED" : "REJECTED";
        String actionText = isApprove ? "PHÊ DUYỆT" : "TỪ CHỐI";

        attendance.setStatus(newStatus);
        attendanceService.save(attendance);

        String teacherName = (attendance.getTeacher() != null && attendance.getTeacher().getName() != null) ? attendance.getTeacher().getName() : "Giáo viên";
        systemLogService.log(SecurityUtils.getUser(), actionText + " CHẤM CÔNG", 
                "Admin vừa " + actionText + " lượt điểm danh #" + id + " của giáo viên " + teacherName + " (Ngày: " + attendance.getDate() + ")");

        if (attendance.getTeacher() != null) {
            String msg = "Lượt điểm danh ngày " + attendance.getDate() + " (" + attendance.getSession() + ") đã được Admin " + actionText + ".";
            notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/dashboard");
        }

        redirectAttributes.addFlashAttribute("success", "Đã " + actionText + " lượt điểm danh #" + id + " thành công!");
        return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/attendances");
    }
}
