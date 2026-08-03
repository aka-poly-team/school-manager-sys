package aka.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.Attendance;
import aka.model.Complaint;
import aka.model.Teacher;
import aka.service.AttendanceService;
import aka.service.ComplaintService;
import aka.service.UserService;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherComplaintController {

    UserService userService;
    ComplaintService complaintService;
    AttendanceService attendanceService;

    @GetMapping("/complaints")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Complaint> complaints = teacherId != null 
                ? complaintService.findByAttendanceTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("complaints", complaints);
        return "teacher/complaints";
    }

    @GetMapping("/complaints/new")
    public String form(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Attendance> attendances = teacherId != null 
                ? attendanceService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("attendances", attendances);
        return "teacher/complaint-form";
    }

    @PostMapping("/complaints/new")
    public String submit(@RequestParam("attendanceId") Long attendanceId,
                         @RequestParam("content") String content,
                         @RequestParam("expectedPeriods") Integer expectedPeriods) {
        Teacher teacher = SecurityUtils.getTeacher(userService);

        if (teacher != null) {
            Attendance attendance = attendanceService.findById(attendanceId).orElse(null);

            Complaint complaint = Complaint.builder()
                    .attendance(attendance)
                    .content(content)
                    .expectedPeriods(expectedPeriods)
                    .status(0)
                    .build();

            complaintService.save(complaint);
        }

        return "redirect:/teacher/complaints";
    }
}
