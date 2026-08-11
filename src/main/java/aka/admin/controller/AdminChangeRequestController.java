package aka.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.ChangeRequest;
import aka.service.ChangeRequestService;
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
public class AdminChangeRequestController {

    ChangeRequestService changeRequestService;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/change-requests")
    public String list(Model model) {
        List<ChangeRequest> changeRequests = changeRequestService.findAllByOrderByIdDesc();
        model.addAttribute("changeRequests", changeRequests);
        return "admin/change-request/list";
    }

    @PostMapping("/change-requests/approve/{id}")
    public String approve(@PathVariable("id") Integer id) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("approved");
            changeRequestService.save(cr);

            systemLogService.log(SecurityUtils.getUser(), "DUYỆT ĐƠN NGHỈ", 
                    "Admin vừa PHÊ DUYỆT đơn xin nghỉ / đổi ca #" + id + " của giáo viên " + (cr.getTeacher() != null ? cr.getTeacher().getName() : ""));

            if (cr.getTeacher() != null) {
                String msg = "Yêu cầu nghỉ phép / đổi ca ngày " + cr.getDate() + " của bạn đã được Admin PHÊ DUYỆT.";
                notificationService.notifyTeacher(cr.getTeacher(), msg, "/teacher/change-requests");
            }
        }
        return "redirect:/admin/change-requests";
    }

    @PostMapping("/change-requests/reject/{id}")
    public String reject(@PathVariable("id") Integer id) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("rejected");
            changeRequestService.save(cr);

            systemLogService.log(SecurityUtils.getUser(), "TỪ CHỐI ĐƠN NGHỈ", 
                    "Admin vừa TỪ CHỐI đơn xin nghỉ / đổi ca #" + id + " của giáo viên " + (cr.getTeacher() != null ? cr.getTeacher().getName() : ""));

            if (cr.getTeacher() != null) {
                String msg = "Yêu cầu nghỉ phép / đổi ca ngày " + cr.getDate() + " của bạn đã bị Admin TỪ CHỐI.";
                notificationService.notifyTeacher(cr.getTeacher(), msg, "/teacher/change-requests");
            }
        }
        return "redirect:/admin/change-requests";
    }
}
