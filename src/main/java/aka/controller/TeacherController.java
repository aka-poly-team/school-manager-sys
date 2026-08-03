package aka.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import aka.model.Attendance;
import aka.model.Schedule;
import aka.model.Teacher;
import aka.service.AttendanceService;
import aka.service.ChangeRequestService;
import aka.service.ComplaintService;
import aka.service.ScheduleService;
import aka.service.UserService;
import aka.util.DateUtils;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherController {

    UserService userService;
    ScheduleService scheduleService;
    AttendanceService attendanceService;
    ComplaintService complaintService;
    ChangeRequestService changeRequestService;

    @GetMapping("/dashboard")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Schedule> upcomingClasses = teacherId != null 
                ? scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacherId) 
                : Collections.emptyList();

        List<Attendance> allTeacherAttendances = teacherId != null
                ? attendanceService.findByTeacherIdOrderByIdDesc(teacherId)
                : Collections.emptyList();

        List<Attendance> recentAttendances = teacherId != null 
                ? attendanceService.findTop5ByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        List<Attendance> todayAttendances = teacherId != null
                ? attendanceService.findByTeacherIdAndDate(teacherId, LocalDate.now())
                : Collections.emptyList();

        Map<Integer, Attendance> scheduleAttendanceMap = new HashMap<>();

        for (Schedule sch : upcomingClasses) {
            if (sch.getSchool() == null || sch.getSchoolClass() == null || sch.getSession() == null) continue;

            for (Attendance att : todayAttendances) {
                boolean matchByScheduleId = (att.getSchedule() != null && att.getSchedule().getId().equals(sch.getId()));
                boolean matchBySchoolClassSession = (
                        att.getSchool() != null && att.getSchool().getId().equals(sch.getSchool().getId()) &&
                        att.getSchoolClass() != null && att.getSchoolClass().getId().equals(sch.getSchoolClass().getId()) &&
                        att.getSession() != null && att.getSession().equalsIgnoreCase(sch.getSession())
                );

                if (matchByScheduleId || matchBySchoolClassSession) {
                    scheduleAttendanceMap.put(sch.getId(), att);
                    break;
                }
            }
        }

        // Tính tổng số tiết giảng dạy tích lũy từ các ca chấm công
        long totalPeriodsTaught = allTeacherAttendances.stream()
                .mapToLong(a -> a.getPeriods() != null ? a.getPeriods() : 0)
                .sum();

        model.addAttribute("thisWeekClassesCount", (long) upcomingClasses.size());
        model.addAttribute("totalAttendancesCount", (long) allTeacherAttendances.size());
        model.addAttribute("totalPeriodsTaught", totalPeriodsTaught);
        model.addAttribute("approvedAbsencesCount", teacherId != null ? changeRequestService.countByTeacherIdAndStatus(teacherId, "approved") : 0);
        model.addAttribute("upcomingClasses", upcomingClasses);
        model.addAttribute("recentAttendances", recentAttendances);
        model.addAttribute("scheduleAttendanceMap", scheduleAttendanceMap);
        model.addAttribute("todayFormatted", DateUtils.today());

        return "teacher/dashboard";
    }
}
