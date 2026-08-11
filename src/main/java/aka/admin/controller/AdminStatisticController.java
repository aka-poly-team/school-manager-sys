package aka.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import aka.model.Attendance;
import aka.service.AttendanceService;
import aka.service.SchoolService;
import aka.service.TeacherService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatisticController {

    TeacherService teacherService;
    SchoolService schoolService;
    AttendanceService attendanceService;

    @GetMapping("/statistics")
    public String index(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "teacherId", required = false) Integer teacherId,
            @RequestParam(value = "schoolId", required = false) Integer schoolId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tab", defaultValue = "diagram") String activeTab,
            Model model) {

        // 1. Lọc danh sách điểm danh theo bộ lọc query
        List<Attendance> filteredAttendances = attendanceService.filterAttendancesByQuery(month, year, teacherId, schoolId, status);

        // 2. Tính toán Thống kê số tiết theo Giáo viên
        List<Object[]> rawPeriodStats = attendanceService.queryTeacherPeriodStats(month, year, teacherId, schoolId, status);

        List<Map<String, Object>> teacherStats = new ArrayList<>();
        long filteredApprovedPeriods = 0;
        long filteredPendingPeriods = 0;
        long maxTeacherPeriods = 1;

        for (Object[] row : rawPeriodStats) {
            Integer tId = (Integer) row[0];
            String name = (String) row[1];
            String email = (String) row[2];
            String phone = (String) row[3];
            long totalAtts = ((Number) row[4]).longValue();
            long approved = ((Number) row[5]).longValue();
            long pending = ((Number) row[6]).longValue();

            filteredApprovedPeriods += approved;
            filteredPendingPeriods += pending;

            long totalPeriods = approved + pending;
            if (totalPeriods > maxTeacherPeriods) {
                maxTeacherPeriods = totalPeriods;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("teacherId", tId);
            item.put("teacherName", name);
            item.put("email", email);
            item.put("phone", phone);
            item.put("totalAttendances", totalAtts);
            item.put("approvedPeriods", approved);
            item.put("pendingPeriods", pending);
            item.put("totalPeriods", totalPeriods);

            teacherStats.add(item);
        }

        // 3. Phân tích số lượng theo trạng thái điểm danh cho Biểu đồ Tròn/Sơ đồ
        long approvedCount = 0;
        long pendingCount = 0;
        long rejectedCount = 0;

        for (Attendance att : filteredAttendances) {
            if ("APPROVED".equalsIgnoreCase(att.getStatus())) {
                approvedCount++;
            } else if ("REJECTED".equalsIgnoreCase(att.getStatus())) {
                rejectedCount++;
            } else {
                pendingCount++;
            }
        }

        model.addAttribute("attendances", filteredAttendances);
        model.addAttribute("teacherStats", teacherStats);
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("schools", schoolService.findAll());

        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedTeacherId", teacherId);
        model.addAttribute("selectedSchoolId", schoolId);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("activeTab", activeTab);

        model.addAttribute("filteredCount", filteredAttendances.size());
        model.addAttribute("filteredApprovedPeriods", filteredApprovedPeriods);
        model.addAttribute("filteredPendingPeriods", filteredPendingPeriods);

        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("maxTeacherPeriods", maxTeacherPeriods);

        return "admin/statistic/index";
    }
}
