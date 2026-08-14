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

    @PostMapping("/change-requests/approve/{id}")
    public String approve(@PathVariable("id") Integer id,
                          @RequestHeader(value = "Referer", required = false) String referer,
                          RedirectAttributes redirectAttributes) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("approved");
            changeRequestService.save(cr);

            systemLogService.log(SecurityUtils.getUser(), "DUYỆT ĐƠN NGHỈ", 
                    "Admin vừa PHÊ DUYỆT đơn xin đổi/nghỉ dạy #" + id + " của giáo viên " + (cr.getTeacher() != null ? cr.getTeacher().getName() : ""));

            if (cr.getTeacher() != null) {
                String msg = "Đơn xin đổi/nghỉ dạy #" + id + " của bạn đã được Admin PHÊ DUYỆT.";
                notificationService.notifyTeacher(cr.getTeacher(), msg, "/teacher/change-requests");
            }
            redirectAttributes.addFlashAttribute("success", "Đã PHÊ DUYỆT đơn xin nghỉ #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn xin nghỉ #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/change-requests");
    }

    @PostMapping("/change-requests/reject/{id}")
    public String reject(@PathVariable("id") Integer id,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("rejected");
            changeRequestService.save(cr);

            systemLogService.log(SecurityUtils.getUser(), "TỪ CHỐI ĐƠN NGHỈ", 
                    "Admin vừa TỪ CHỐI đơn xin đổi/nghỉ dạy #" + id + " của giáo viên " + (cr.getTeacher() != null ? cr.getTeacher().getName() : ""));

            if (cr.getTeacher() != null) {
                String msg = "Đơn xin đổi/nghỉ dạy #" + id + " của bạn đã bị Admin TỪ CHỐI.";
                notificationService.notifyTeacher(cr.getTeacher(), msg, "/teacher/change-requests");
            }
            redirectAttributes.addFlashAttribute("success", "Đã TỪ CHỐI đơn xin nghỉ #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn xin nghỉ #" + id);
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/admin/change-requests");
    }
}
