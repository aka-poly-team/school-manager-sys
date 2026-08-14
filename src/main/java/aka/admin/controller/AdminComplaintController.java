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
import aka.model.Complaint;
import aka.repository.ComplaintRepository;
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
    ComplaintRepository complaintRepository;
    AttendanceService attendanceService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/complaints")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Complaint> pageResult = complaintRepository.findAll(pageable);

        model.addAttribute("complaints", pageResult.getContent());
        model.addAttribute("pageObj", pageResult);
        return "admin/complaint/list";
    }

    @PostMapping("/complaints/approve/{id}")
    public String approve(@PathVariable("id") Integer id,
                          @RequestHeader(value = "Referer", required = false) String referer,
                          RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(1);
            complaintService.save(complaint);

            Attendance attendance = complaint.getAttendance();
            if (attendance != null) {
                attendance.setStatus("APPROVED");
                attendanceService.save(attendance);
            }

            systemLogService.log(SecurityUtils.getUser(), "DUYỆT KHIẾU NẠI", 
                    "Admin vừa CHẤP NHẬN đơn khiếu nại #" + id + " của giáo viên " + (complaint.getAttendance() != null && complaint.getAttendance().getTeacher() != null ? complaint.getAttendance().getTeacher().getName() : ""));

            if (attendance != null && attendance.getTeacher() != null) {
                String msg = "Khiếu nại #" + id + " cho ca dạy ngày " + attendance.getDate() + " của bạn đã được CHẤP NHẬN. Ca dạy đã được cập nhật thành ĐÃ DUYỆT.";
                notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/complaints");
            }
            redirectAttributes.addFlashAttribute("success", "Đã CHẤP NHẬN đơn khiếu nại #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn khiếu nại #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/complaints");
    }

    @PostMapping("/complaints/reject/{id}")
    public String reject(@PathVariable("id") Integer id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus(2);
            complaintService.save(complaint);

            Attendance attendance = complaint.getAttendance();

            systemLogService.log(SecurityUtils.getUser(), "TỪ CHỐI KHIẾU NẠI", 
                    "Admin vừa TỪ CHỐI đơn khiếu nại #" + id + " của giáo viên " + (attendance != null && attendance.getTeacher() != null ? attendance.getTeacher().getName() : ""));

            if (attendance != null && attendance.getTeacher() != null) {
                String msg = "Khiếu nại #" + id + " cho ca dạy ngày " + attendance.getDate() + " của bạn đã bị TỪ CHỐI.";
                notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/complaints");
            }
            redirectAttributes.addFlashAttribute("success", "Đã TỪ CHỐI đơn khiếu nại #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn khiếu nại #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/complaints");
    }
}
