package aka.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.SystemLog;
import aka.service.SystemLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminSystemLogController {

    SystemLogService systemLogService;

    @GetMapping("/system-logs")
    public String list(Model model) {
        List<SystemLog> logs = systemLogService.findAllByOrderByIdDesc();
        model.addAttribute("logs", logs);
        return "admin/system-log/list";
    }
}
