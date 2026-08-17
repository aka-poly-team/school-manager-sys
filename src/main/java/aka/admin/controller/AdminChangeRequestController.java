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

import aka.model.ChangeRequest;
import aka.repository.ChangeRequestRepository;
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
    ChangeRequestRepository changeRequestRepository;
    NotificationService notificationService;
    SystemLogService systemLogService;

    @GetMapping("/change-requests")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<ChangeRequest> pageResult = changeRequestRepository.findAll(pageable);

        model.addAttribute("changeRequests", pageResult.getContent());
        model.addAttribute("pageObj", pageResult);
        return "admin/change-request/list";
    }

    // GỘP PHÊ DUYỆT & TỪ CHỐI ĐƠN NGHỈ THÀNH 1 PHƯƠNG THỨC DUY NHẤT DÙNG IF-ELSE
    @PostMapping({"/change-requests/approve/{id}", "/change-requests/reject/{id}", "/change-requests/process/{id}"})
    public String processChangeRequest(@PathVariable("id") Integer id,
                                       @RequestParam(value = "action", required = false) String action,
                                       jakarta.servlet.http.HttpServletRequest request,
                                       @RequestHeader(value = "Referer", required = false) String referer,
                                       RedirectAttributes redirectAttributes) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn xin nghỉ #" + id);
            return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/change-requests");
        }

        boolean isApprove = "approve".equalsIgnoreCase(action) || request.getRequestURI().contains("/approve");
        String newStatus = isApprove ? "approved" : "rejected";
        String actionText = isApprove ? "PHÊ DUYỆT" : "TỪ CHỐI";

        cr.setStatus(newStatus);
        changeRequestService.save(cr);

        String teacherName = cr.getTeacher() != null ? cr.getTeacher().getName() : "";
        systemLogService.log(SecurityUtils.getUser(), actionText + " ĐƠN NGHỈ", 
                "Admin vừa " + actionText + " đơn xin đổi/nghỉ dạy #" + id + " của giáo viên " + teacherName);

        if (cr.getTeacher() != null) {
            String msg = "Đơn xin đổi/nghỉ dạy #" + id + " của bạn đã " + (isApprove ? "được Admin PHÊ DUYỆT." : "bị Admin TỪ CHỐI.");
            notificationService.notifyTeacher(cr.getTeacher(), msg, "/teacher/change-requests");
        }

        redirectAttributes.addFlashAttribute("success", "Đã " + actionText + " đơn xin nghỉ #" + id + " thành công!");
        return "redirect:" + aka.util.StringUtils.defaultIfBlank(referer, "/admin/change-requests");
    }
}
