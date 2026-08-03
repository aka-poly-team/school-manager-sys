package aka.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.service.AttendanceService;
import aka.service.ChangeRequestService;
import aka.service.ComplaintService;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.TeacherService;
import aka.service.UserService;
import aka.util.DateUtils;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardController {

    UserService userService;
    TeacherService teacherService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;
    ScheduleService scheduleService;
    AttendanceService attendanceService;
    ComplaintService complaintService;
    ChangeRequestService changeRequestService;

    @GetMapping("/dashboard")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);

        model.addAttribute("totalTeachers", teacherService.count());
        model.addAttribute("totalSchools", schoolService.count());
        model.addAttribute("totalClasses", schoolClassService.count());
        model.addAttribute("totalSchedules", scheduleService.count());
        model.addAttribute("pendingAttendancesCount", attendanceService.countByStatus("PENDING"));
        model.addAttribute("pendingComplaintsCount", complaintService.countByStatus(0));
        model.addAttribute("pendingRequestsCount", changeRequestService.countByStatus("pending"));

        model.addAttribute("recentAttendances", attendanceService.findAllByOrderByIdDesc());
        model.addAttribute("recentComplaints", complaintService.findAllByOrderByIdDesc());
        model.addAttribute("recentRequests", changeRequestService.findAllByOrderByIdDesc());
        model.addAttribute("todayFormatted", DateUtils.today());

        return "admin/dashboard";
    }
}
