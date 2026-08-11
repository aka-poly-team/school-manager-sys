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
public class AdminComplaintController {

    ComplaintService complaintService;
    AttendanceService attendanceService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/complaints")
    public String list(Model model) {
        List<Complaint> complaints = complaintService.findAllByOrderByIdDesc();
        model.addAttribute("complaints", complaints);
        return "admin/complaint/list";
    }

    @PostMapping("/complaints/approve/{id}")
    public String approve(@PathVariable("id") Integer id) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(1);
            complaintService.save(complaint);

            systemLogService.log(SecurityUtils.getUser(), "DUYỆT KHIẾU NẠI", 
                    "Admin vừa CHẤP NHẬN đơn khiếu nại #" + id + " và cập nhật số tiết dạy");

            if (complaint.getAttendance() != null && complaint.getExpectedPeriods() != null) {
                Attendance attendance = complaint.getAttendance();
                attendance.setPeriods(complaint.getExpectedPeriods());
                attendance.setStatus("APPROVED");
                attendanceService.save(attendance);

                if (attendance.getTeacher() != null) {
                    String msg = "Đơn khiếu nại #" + complaint.getId() + " cho buổi dạy ngày " + attendance.getDate() + " đã được Admin PHÊ DUYỆT.";
                    notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/complaints");
                }
            }
        }
        return "redirect:/admin/complaints";
    }

    @PostMapping("/complaints/reject/{id}")
    public String reject(@PathVariable("id") Integer id) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(2);
            complaintService.save(complaint);

            systemLogService.log(SecurityUtils.getUser(), "TỪ CHỐI KHIẾU NẠI", 
                    "Admin vừa TỪ CHỐI đơn khiếu nại #" + id);

            if (complaint.getAttendance() != null && complaint.getAttendance().getTeacher() != null) {
                String msg = "Đơn khiếu nại #" + complaint.getId() + " cho buổi dạy ngày " + complaint.getAttendance().getDate() + " đã bị Admin TỪ CHỐI.";
                notificationService.notifyTeacher(complaint.getAttendance().getTeacher(), msg, "/teacher/complaints");
            }
        }
        return "redirect:/admin/complaints";
    }
}
