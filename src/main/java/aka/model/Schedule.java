package aka.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    Integer dayOfWeek;

    @Column(nullable = false, length = 20)
    String session;

    @ManyToOne
    @JoinColumn(name = "teacherId", nullable = false)
    Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "schoolId", nullable = false)
    School school;

    @ManyToOne
    @JoinColumn(name = "classId", nullable = false)
    SchoolClass schoolClass;

    @Builder.Default
    @Column(nullable = false)
    Integer periods = 1;

    LocalTime startTime;
    LocalTime endTime;

    // Helper Getters cho HTML hiển thị siêu ngắn gọn
    public String getTeacherName() {
        return teacher != null ? teacher.getName() : "Giáo viên";
    }

    public String getSchoolAndClassName() {
        String sName = school != null ? school.getName() : "";
        String cName = schoolClass != null ? schoolClass.getName() : "";
        if (sName.isEmpty()) return cName;
        if (cName.isEmpty()) return sName;
        return sName + " - " + cName;
    }

    public String getTimeRange() {
        String sTime = startTime != null ? startTime.toString() : "08:00";
        String eTime = endTime != null ? endTime.toString() : "09:30";
        return sTime + " - " + eTime;
    }
}
