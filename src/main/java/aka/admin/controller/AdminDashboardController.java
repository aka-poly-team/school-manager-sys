package aka.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.Attendance;
import aka.repository.AttendanceRepository;
import aka.service.AttendanceService;
import aka.service.ChangeRequestService;
import aka.service.ComplaintService;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.TeacherService;
import aka.util.DateUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardController {

    TeacherService teacherService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;
    ScheduleService scheduleService;
    AttendanceService attendanceService;
    AttendanceRepository attendanceRepository;
    ComplaintService complaintService;
    ChangeRequestService changeRequestService;

    @GetMapping({"", "/", "/dashboard"})
    public String index(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        model.addAttribute("totalTeachers", teacherService.count());
        model.addAttribute("totalSchools", schoolService.count());
        model.addAttribute("totalClasses", schoolClassService.count());
        model.addAttribute("totalSchedules", scheduleService.count());
        model.addAttribute("pendingAttendancesCount", attendanceService.countByStatus("PENDING"));
        model.addAttribute("pendingComplaintsCount", complaintService.countByStatus(0));
        model.addAttribute("pendingRequestsCount", changeRequestService.countByStatus("pending"));

        Pageable pageable = PageRequest.of(page, 5, Sort.by("id").descending());
        Page<Attendance> pageResult = (keyword != null && !keyword.isBlank())
                ? attendanceRepository.searchAttendances(keyword.trim(), pageable)
                : attendanceRepository.findAll(pageable);

        model.addAttribute("recentAttendances", pageResult.getContent());
        model.addAttribute("pageObj", pageResult);
        model.addAttribute("keyword", keyword);

        model.addAttribute("recentComplaints", complaintService.findAllByOrderByIdDesc());
        model.addAttribute("recentRequests", changeRequestService.findAllByOrderByIdDesc());
        model.addAttribute("todayFormatted", DateUtils.today());

        return "admin/dashboard/index";
    }
}
