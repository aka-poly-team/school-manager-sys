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
            item.put("email", email != null ? email : "");
            item.put("phone", phone != null ? phone : "");
            item.put("totalAttendances", totalAtts);
            item.put("approvedPeriods", approved);
            item.put("pendingPeriods", pending);
            item.put("totalPeriods", totalPeriods);

            teacherStats.add(item);
        }

        // Tính % hiển thị cho từng giáo viên để template không cần tính toán
        for (Map<String, Object> item : teacherStats) {
            long app = ((Number) item.get("approvedPeriods")).longValue();
            long pend = ((Number) item.get("pendingPeriods")).longValue();
            double appPct = (maxTeacherPeriods > 0) ? (app * 100.0 / maxTeacherPeriods) : 0;
            double pendPct = (maxTeacherPeriods > 0) ? (pend * 100.0 / maxTeacherPeriods) : 0;
            item.put("approvedPercent", Math.round(appPct));
            item.put("pendingPercent", Math.round(pendPct));
        }

        // 3. Phân tích số lượng theo trạng thái điểm danh
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

        // Tỷ lệ phê duyệt định dạng an toàn
        long totalAllPeriods = filteredApprovedPeriods + filteredPendingPeriods;
        String approvalRate = (totalAllPeriods > 0) 
                ? String.format("%.1f%%", (filteredApprovedPeriods * 100.0) / totalAllPeriods) 
                : "0%";

        // 4. REAL-TIME: Thống kê số tiết & số ca 12 tháng từ CSDL cho Chart.js
        int chartYear = (year != null) ? year : java.time.LocalDate.now().getYear();
        List<Attendance> yearAttendances = attendanceService.filterAttendancesByQuery(null, chartYear, teacherId, schoolId, null);

        long[] monthlyApproved = new long[12];
        long[] monthlyPending  = new long[12];
        long[] monthlyRejected = new long[12];
        long[] monthlySessions = new long[12];

        if (yearAttendances != null) {
            for (Attendance att : yearAttendances) {
                if (att != null && att.getDate() != null) {
                    int m = att.getDate().getMonthValue() - 1; // 0 to 11
                    if (m >= 0 && m < 12) {
                        monthlySessions[m]++;
                        int p = (att.getPeriods() != null) ? att.getPeriods() : 2;
                        if ("APPROVED".equalsIgnoreCase(att.getStatus())) {
                            monthlyApproved[m] += p;
                        } else if ("REJECTED".equalsIgnoreCase(att.getStatus())) {
                            monthlyRejected[m] += p;
                        } else {
                            monthlyPending[m] += p;
                        }
                    }
                }
            }
        }

        // Chuyển sang List để Thymeleaf inline JS render chính xác
        List<Long> monthlyApprovedList  = new ArrayList<>();
        List<Long> monthlyPendingList   = new ArrayList<>();
        List<Long> monthlyRejectedList  = new ArrayList<>();
        List<Long> monthlySessionsList  = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            monthlyApprovedList.add(monthlyApproved[i]);
            monthlyPendingList.add(monthlyPending[i]);
            monthlyRejectedList.add(monthlyRejected[i]);
            monthlySessionsList.add(monthlySessions[i]);
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
        model.addAttribute("approvalRate", approvalRate);

        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("maxTeacherPeriods", maxTeacherPeriods);

        // Real-time chart data
        model.addAttribute("chartYear", chartYear);
        model.addAttribute("monthlyApproved", monthlyApprovedList);
        model.addAttribute("monthlyPending", monthlyPendingList);
        model.addAttribute("monthlyRejected", monthlyRejectedList);
        model.addAttribute("monthlySessions", monthlySessionsList);

        return "admin/statistic/index";
    }
}
