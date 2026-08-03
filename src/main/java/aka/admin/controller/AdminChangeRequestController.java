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
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminChangeRequestController {

    UserService userService;
    ChangeRequestService changeRequestService;

    @GetMapping("/change-requests")
    public String list(Model model) {
        SecurityUtils.populate(model, userService);
        List<ChangeRequest> changeRequests = changeRequestService.findAllByOrderByIdDesc();
        model.addAttribute("changeRequests", changeRequests);
        return "admin/change-requests";
    }

    @PostMapping("/change-requests/{id}/approve")
    public String approve(@PathVariable("id") Integer id) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("approved");
            changeRequestService.save(cr);
        }
        return "redirect:/admin/change-requests";
    }

    @PostMapping("/change-requests/{id}/reject")
    public String reject(@PathVariable("id") Integer id) {
        ChangeRequest cr = changeRequestService.findById(id).orElse(null);
        if (cr != null) {
            cr.setStatus("rejected");
            changeRequestService.save(cr);
        }
        return "redirect:/admin/change-requests";
    }
}
