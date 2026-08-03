package aka.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Attendance;
import aka.model.Complaint;
import aka.service.AttendanceService;
import aka.service.ComplaintService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminComplaintController {

    UserService userService;
    ComplaintService complaintService;
    AttendanceService attendanceService;

    @GetMapping("/complaints")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        List<Complaint> complaints = complaintService.findAllByOrderByIdDesc();
        model.addAttribute("complaints", complaints);
        return "admin/complaints";
    }

    @PostMapping("/complaints/{id}/approve")
    public String approve(@PathVariable("id") Integer id) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(1);
            complaintService.save(complaint);

            if (complaint.getAttendance() != null && complaint.getExpectedPeriods() != null) {
                Attendance attendance = complaint.getAttendance();
                attendance.setPeriods(complaint.getExpectedPeriods());
                attendance.setStatus("APPROVED");
                attendanceService.save(attendance);
            }
        }
        return "redirect:/admin/complaints";
    }

    @PostMapping("/complaints/{id}/reject")
    public String reject(@PathVariable("id") Integer id) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(2);
            complaintService.save(complaint);
        }
        return "redirect:/admin/complaints";
    }
}
