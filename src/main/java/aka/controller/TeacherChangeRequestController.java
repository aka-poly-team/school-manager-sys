package aka.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.ChangeRequest;
import aka.model.Teacher;
import aka.service.ChangeRequestService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherChangeRequestController {

    UserService userService;
    ChangeRequestService changeRequestService;

    @GetMapping("/change-requests")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<ChangeRequest> changeRequests = teacherId != null 
                ? changeRequestService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("changeRequests", changeRequests);
        return "teacher/change-requests";
    }

    @GetMapping("/change-requests/new")
    public String form(Model model) {
        SecurityUtils.populate(model, userService);
        return "teacher/change-request-form";
    }

    @PostMapping("/change-requests/new")
    public String submit(@RequestParam("requestType") String requestType,
                         @RequestParam("reason") String reason,
                         @RequestParam(value = "requestDate", required = false) String requestDate) {
        Teacher teacher = SecurityUtils.getTeacher(userService);

        if (teacher != null) {
            ChangeRequest cr = ChangeRequest.builder()
                    .teacher(teacher)
                    .requestType(requestType)
                    .reason(reason)
                    .status("pending")
                    .build();

            changeRequestService.save(cr);
        }

        return "redirect:/teacher/change-requests";
    }
}
