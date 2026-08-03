package aka.model;

import java.time.LocalDate;
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
@Table(name = "Attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    LocalDate date;

    @ManyToOne
    @JoinColumn(name = "scheduleId")
    Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "teacherId", nullable = false)
    Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "schoolId", nullable = false)
    School school;

    @ManyToOne
    @JoinColumn(name = "classId", nullable = false)
    SchoolClass schoolClass;

    @Column(nullable = false, length = 20)
    String session;

    @Column(nullable = false)
    LocalTime checkInTime;

    @Builder.Default
    @Column(nullable = false)
    Integer periods = 1;

    @Column(nullable = false, length = 500)
    String selfieImage;

    @Column(length = 255)
    String notes;

    @Builder.Default
    @Column(nullable = false, length = 20)
    String status = "PENDING";

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
}
