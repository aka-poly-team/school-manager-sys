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

    @PostMapping("/attendances/approve/{id}")
    public String approve(@PathVariable("id") Long id,
                          @RequestHeader(value = "Referer", required = false) String referer,
                          RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("success", "Đã PHÊ DUYỆT lượt điểm danh #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lượt điểm danh #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/attendances");
    }

    @PostMapping("/attendances/reject/{id}")
    public String reject(@PathVariable("id") Long id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("success", "Đã TỪ CHỐI lượt điểm danh #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lượt điểm danh #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/attendances");
    }

    @PostMapping("/attendances/delete/{id}")
    public String delete(@PathVariable("id") Long id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        try {
            attendanceService.deleteById(id);
            systemLogService.log(SecurityUtils.getUser(), "XÓA CHẤM CÔNG", "Admin vừa XÓA lượt điểm danh #" + id);
            redirectAttributes.addFlashAttribute("success", "Đã XÓA lượt điểm danh #" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa lượt điểm danh #" + id + ": " + e.getMessage());
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/attendances");
    }
}
