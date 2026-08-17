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

    // GỘP CHẤP NHẬN & TỪ CHỐI KHIẾU NẠI THÀNH 1 PHƯƠNG THỨC DUY NHẤT DÙNG IF-ELSE
    @PostMapping({"/complaints/approve/{id}", "/complaints/reject/{id}", "/complaints/process/{id}"})
    public String processComplaint(@PathVariable("id") Integer id,
                                   @RequestParam(value = "action", required = false) String action,
                                   jakarta.servlet.http.HttpServletRequest request,
                                   @RequestHeader(value = "Referer", required = false) String referer,
                                   RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.findById(id).orElse(null);
        if (complaint == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn khiếu nại #" + id);
            return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/complaints");
        }

        boolean isApprove = "approve".equalsIgnoreCase(action) || request.getRequestURI().contains("/approve");
        int newStatus = isApprove ? 1 : 2;
        String actionText = isApprove ? "CHẤP NHẬN" : "TỪ CHỐI";

        complaint.setStatus(newStatus);
        complaintService.save(complaint);

        Attendance attendance = complaint.getAttendance();
        if (isApprove && attendance != null) {
            attendance.setStatus("APPROVED");
            attendanceService.save(attendance);
        }

        String teacherName = (attendance != null && attendance.getTeacher() != null) ? attendance.getTeacher().getName() : "";
        systemLogService.log(SecurityUtils.getUser(), actionText + " KHIẾU NẠI", 
                "Admin vừa " + actionText + " đơn khiếu nại #" + id + " của giáo viên " + teacherName);

        if (attendance != null && attendance.getTeacher() != null) {
            String msg = isApprove 
                    ? "Khiếu nại #" + id + " cho ca dạy ngày " + attendance.getDate() + " của bạn đã được CHẤP NHẬN. Ca dạy đã được cập nhật thành ĐÃ DUYỆT."
                    : "Khiếu nại #" + id + " cho ca dạy ngày " + attendance.getDate() + " của bạn đã bị TỪ CHỐI.";
            notificationService.notifyTeacher(attendance.getTeacher(), msg, "/teacher/complaints");
        }

        redirectAttributes.addFlashAttribute("success", "Đã " + actionText + " đơn khiếu nại #" + id + " thành công!");
        return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/complaints");
    }
}
