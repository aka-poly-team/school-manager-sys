package aka.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aka.util.StringUtils;
import aka.model.Attendance;
import aka.model.Schedule;
import aka.model.School;
import aka.model.SchoolClass;
import aka.model.Teacher;
import aka.repository.AttendanceRepository;
import aka.service.AttendanceService;
import aka.service.CloudinaryService;
import aka.service.ScheduleService;
import aka.service.SchoolClassService;
import aka.service.SchoolService;
import aka.service.SystemLogService;
import aka.util.FileUploadUtils;
import aka.util.SecurityUtils;
import aka.util.ValidationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherAttendanceController {

    AttendanceService attendanceService;
    AttendanceRepository attendanceRepository;
    SchoolService schoolService;
    SchoolClassService schoolClassService;
    ScheduleService scheduleService;
    CloudinaryService cloudinaryService;
    SystemLogService systemLogService;

    @GetMapping("/attendance")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Teacher teacher = SecurityUtils.getTeacher();
        Integer teacherId = teacher != null ? teacher.getId() : null;

        if (teacherId != null) {
            Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
            Page<Attendance> pageResult = attendanceRepository.findByTeacherId(teacherId, pageable);
            model.addAttribute("attendances", pageResult.getContent());
            model.addAttribute("pageObj", pageResult);
        } else {
            model.addAttribute("attendances", Collections.emptyList());
        }

        return "teacher/attendance/list";
    }

    @GetMapping("/attendance/new")
    public String form(Model model) {
        populateForm(model);
        return "teacher/attendance/form";
    }

    @PostMapping("/attendance/new")
    public String submit(@RequestParam("schoolId") Integer schoolId,
                         @RequestParam("classId") Integer classId,
                         @RequestParam("session") String session,
                         @RequestParam(value = "periods", defaultValue = "1") Integer periods,
                         @RequestParam(value = "notes", required = false) String notes,
                         @RequestParam(value = "selfieFile", required = false) MultipartFile selfieFile,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Teacher teacher = SecurityUtils.getTeacher();

        if (teacher == null) {
            model.addAttribute("error", "Tài khoản của bạn chưa liên kết với hồ sơ Giáo viên!");
            populateForm(model);
            return "teacher/attendance/form";
        }

        if (selfieFile == null || selfieFile.isEmpty()) {
            model.addAttribute("error", "Vui lòng đính kèm ảnh xác minh điểm danh!");
            populateForm(model);
            return "teacher/attendance/form";
        }
        periods = periods != null ? periods : 1;

        LocalDate today = LocalDate.now();

        List<Schedule> teacherSchedules = scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacher.getId());

        Schedule matchedSchedule = teacherSchedules.stream().filter(s ->
                s.getSchool() != null && s.getSchool().getId().equals(schoolId) &&
                s.getSchoolClass() != null && s.getSchoolClass().getId().equals(classId) &&
                s.getSession() != null && s.getSession().equalsIgnoreCase(session)
        ).findFirst().orElse(null);

        // Fallback 1: Tìm bất kỳ lịch dạy tương ứng của Trường & Lớp này
        if (matchedSchedule == null) {
            List<Schedule> allSchedules = scheduleService.findAll();
            matchedSchedule = allSchedules.stream().filter(s ->
                    s.getSchool() != null && s.getSchool().getId().equals(schoolId) &&
                    s.getSchoolClass() != null && s.getSchoolClass().getId().equals(classId)
            ).findFirst().orElse(null);
        }

        boolean alreadyAttended = attendanceService.existsByTeacherIdAndSchoolIdAndSchoolClassIdAndSessionAndDate(
                teacher.getId(), schoolId, classId, session, today);

        if (alreadyAttended) {
            model.addAttribute("error", "Ca dạy này đã được điểm danh trong ngày hôm nay rồi!");
            populateForm(model);
            return "teacher/attendance/form";
        }

        String selfiePath;
        try {
            selfiePath = cloudinaryService.uploadImage(selfieFile, "attendance");
            if (selfiePath == null) {
                selfiePath = FileUploadUtils.save(selfieFile, "attendance", "selfie");
            }
            if (selfiePath == null) {
                selfiePath = "/uploads/attendance/selfie_default.jpg";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi tải ảnh xác minh: " + e.getMessage());
            populateForm(model);
            return "teacher/attendance/form";
        }

        School school = schoolService.findById(schoolId).orElse(null);
        SchoolClass schoolClass = schoolClassService.findById(classId).orElse(null);

        LocalTime now = LocalTime.now();
        LocalTime expectedStartTime = null;

        if (matchedSchedule != null && matchedSchedule.getStartTime() != null) {
            expectedStartTime = matchedSchedule.getStartTime();
        } else {
            if ("Ca 1".equalsIgnoreCase(session)) expectedStartTime = LocalTime.of(7, 30);
            else if ("Ca 2".equalsIgnoreCase(session)) expectedStartTime = LocalTime.of(9, 0);
            else if ("Ca 3".equalsIgnoreCase(session)) expectedStartTime = LocalTime.of(14, 0);
            else if ("Ca 4".equalsIgnoreCase(session)) expectedStartTime = LocalTime.of(15, 30);
        }

        boolean isLate = false;
        String status = "PENDING";
        if (expectedStartTime != null) {
            LocalTime lateThreshold = expectedStartTime.plusMinutes(15);
            if (now.isAfter(lateThreshold)) {
                isLate = true;
                status = "LATE";
                String lateDetail = "[VÀO MUỘN: Giờ vào " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " (Giờ chuẩn: " + expectedStartTime + ")]";
                notes = (notes != null && !notes.isBlank()) ? notes + " | " + lateDetail : lateDetail;
            }
        }

        Attendance attendance = Attendance.builder()
                .date(today)
                .schedule(matchedSchedule)
                .teacher(teacher)
                .school(school)
                .schoolClass(schoolClass)
                .session(session)
                .checkInTime(now)
                .periods(periods != null ? periods : 1)
                .selfieImage(selfiePath)
                .notes(StringUtils.toTitleCase(notes))
                .status(status)
                .build();

        attendanceService.save(attendance);

        String schoolName = (school != null) ? school.getName() : "Trường";
        String className = (schoolClass != null) ? schoolClass.getName() : "Lớp";
        systemLogService.log(SecurityUtils.getUser(), "ĐIỂM DANH CA DẠY", 
                "Giáo viên thực hiện điểm danh cho " + session + " tại " + schoolName + " (" + className + ")" + (isLate ? " [VÀO MUỘN]" : ""));

        if (isLate) {
            redirectAttributes.addFlashAttribute("warning", "Hệ thống ghi nhận bạn ĐIỂM DANH MUỘN cho " + session + "! Thông tin đã được tự động gắn nhãn 'Vào muộn'.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Điểm danh ca dạy " + session + " thành công!");
        }

        return "redirect:/teacher/attendance";
    }

    private void populateForm(Model model) {
        Teacher teacher = SecurityUtils.getTeacher();

        List<School> assignedSchools = Collections.emptyList();
        List<SchoolClass> assignedClasses = Collections.emptyList();

        if (teacher != null) {
            List<Schedule> teacherSchedules = scheduleService.findByTeacherIdOrderByDayOfWeekAsc(teacher.getId());

            assignedSchools = teacherSchedules.stream()
                    .map(Schedule::getSchool)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            assignedClasses = teacherSchedules.stream()
                    .map(Schedule::getSchoolClass)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        if (assignedSchools.isEmpty()) {
            assignedSchools = schoolService.findAll();
        }
        if (assignedClasses.isEmpty()) {
            assignedClasses = schoolClassService.findAll();
        }

        model.addAttribute("schools", assignedSchools);
        model.addAttribute("classes", assignedClasses);
    }
}
