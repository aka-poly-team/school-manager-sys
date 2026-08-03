package aka.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import aka.model.Attendance;
import aka.model.Schedule;
import aka.model.School;
import aka.model.SchoolClass;
import aka.model.Teacher;
import aka.service.AttendanceService;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.UserService;
import aka.util.FileUploadUtils;
import aka.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherAttendanceController {

    UserService userService;
    AttendanceService attendanceService;
    SchoolService schoolService;
    SchoolClassService schoolClassService;
    ScheduleService scheduleService;

    @GetMapping("/attendance")
    public String index(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);
        Integer teacherId = teacher != null ? teacher.getId() : null;

        List<Attendance> attendances = teacherId != null 
                ? attendanceService.findByTeacherIdOrderByIdDesc(teacherId) 
                : Collections.emptyList();

        model.addAttribute("attendances", attendances);
        return "teacher/attendance";
    }

    @GetMapping("/attendance/new")
    public String form(Model model) {
        populateForm(model);
        return "teacher/attendance-form";
    }

    @PostMapping("/attendance/new")
    public String submit(@RequestParam("schoolId") Integer schoolId,
                         @RequestParam("classId") Integer classId,
                         @RequestParam("session") String session,
                         @RequestParam("periods") Integer periods,
                         @RequestParam(value = "notes", required = false) String notes,
                         @RequestParam("selfieFile") MultipartFile selfieFile,
                         Model model) {
        Teacher teacher = SecurityUtils.getTeacher(userService);

        if (teacher == null) {
            model.addAttribute("error", "Tài khoản của bạn chưa liên kết với hồ sơ Giáo viên!");
            populateForm(model);
            return "teacher/attendance-form";
        }

        if (selfieFile == null || selfieFile.isEmpty()) {
            model.addAttribute("error", "Vui lòng đính kèm ảnh xác minh điểm danh!");
            populateForm(model);
            return "teacher/attendance-form";
        }

        LocalDate today = LocalDate.now();

        List<Schedule> teacherSchedules = scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacher.getId());

        Schedule matchedSchedule = teacherSchedules.stream().filter(s ->
                s.getSchool() != null && s.getSchool().getId().equals(schoolId) &&
                s.getSchoolClass() != null && s.getSchoolClass().getId().equals(classId) &&
                s.getSession() != null && s.getSession().equalsIgnoreCase(session)
        ).findFirst().orElse(null);

        if (matchedSchedule == null) {
            model.addAttribute("error", "Bạn không có lịch giảng dạy được phân công cho Trường, Lớp và Ca dạy này!");
            populateForm(model);
            return "teacher/attendance-form";
        }

        boolean alreadyAttended = attendanceService.existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(
                teacher.getId(), schoolId, classId, session, today);

        if (alreadyAttended) {
            model.addAttribute("error", "Ca dạy này đã được điểm danh trong ngày hôm nay rồi!");
            populateForm(model);
            return "teacher/attendance-form";
        }

        String selfiePath;
        try {
            selfiePath = FileUploadUtils.save(selfieFile, "attendance", "selfie");
            if (selfiePath == null) {
                selfiePath = "/uploads/attendance/selfie_default.jpg";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi tải ảnh xác minh: " + e.getMessage());
            populateForm(model);
            return "teacher/attendance-form";
        }

        School school = schoolService.findById(schoolId).orElse(null);
        SchoolClass schoolClass = schoolClassService.findById(classId).orElse(null);

        Attendance attendance = Attendance.builder()
                .date(today)
                .schedule(matchedSchedule)
                .teacher(teacher)
                .school(school)
                .schoolClass(schoolClass)
                .session(session)
                .checkInTime(LocalTime.now())
                .periods(periods != null ? periods : 1)
                .selfieImage(selfiePath)
                .notes(notes)
                .status("PENDING")
                .build();

        attendanceService.save(attendance);
        return "redirect:/teacher/attendance";
    }

    private void populateForm(Model model) {
        SecurityUtils.populate(model, userService);
        Teacher teacher = SecurityUtils.getTeacher(userService);

        if (teacher != null) {
            List<Schedule> teacherSchedules = scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacher.getId());

            List<School> assignedSchools = teacherSchedules.stream()
                    .map(Schedule::getSchool)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<SchoolClass> assignedClasses = teacherSchedules.stream()
                    .map(Schedule::getSchoolClass)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            model.addAttribute("schools", assignedSchools);
            model.addAttribute("classes", assignedClasses);

            if (teacherSchedules.isEmpty()) {
                model.addAttribute("warning", "Bạn chưa được phân công lịch giảng dạy nào trong CSDL!");
            }
        } else {
            model.addAttribute("schools", Collections.emptyList());
            model.addAttribute("classes", Collections.emptyList());
        }
    }
}
