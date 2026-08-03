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
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAttendanceController {

    UserService userService;
    AttendanceService attendanceService;

    @GetMapping("/attendances")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        List<Attendance> attendances = attendanceService.findAllByOrderByIdDesc();
        model.addAttribute("attendances", attendances);
        return "admin/attendances";
    }

    @PostMapping("/attendances/{id}/approve")
    public String approve(@PathVariable("id") Long id) {
        Attendance attendance = attendanceService.findById(id).orElse(null);
        if (attendance != null) {
            attendance.setStatus("APPROVED");
            attendanceService.save(attendance);
        }
        return "redirect:/admin/attendances";
    }

    @PostMapping("/attendances/{id}/reject")
    public String reject(@PathVariable("id") Long id) {
        Attendance attendance = attendanceService.findById(id).orElse(null);
        if (attendance != null) {
            attendance.setStatus("REJECTED");
            attendanceService.save(attendance);
        }
        return "redirect:/admin/attendances";
    }
}
