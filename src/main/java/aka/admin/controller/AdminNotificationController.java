package aka.admin.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Notification;
import aka.service.NotificationService;
import aka.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminNotificationController {

    NotificationService notificationService;

    @GetMapping("/notifications")
    public String index(Model model) {
        List<Notification> notifications = notificationService.findByForAdminTrueOrderByIdDesc();
        model.addAttribute("notifications", notifications);
        return "admin/notification/list";
    }

    @PostMapping("/notifications/read/{id}")
    public String markRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead() {
        notificationService.markAllAsReadForAdmin();
        return "redirect:/admin/notifications";
    }

    @GetMapping("/notifications/click/{id}")
    public String clickNotification(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        Optional<Notification> notifOpt = notificationService.findById(id);
        if (notifOpt.isPresent()) {
            String link = notifOpt.get().getLink();
            if (StringUtils.isNotBlank(link)) {
                return "redirect:" + link;
            }
        }
        return "redirect:/admin/notifications";
    }
}
