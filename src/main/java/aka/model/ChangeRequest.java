package aka.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "ChangeRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "teacherId")
    Teacher teacher;

    @Column(length = 50)
    String requestType;

    LocalDate date;

    @Column(length = 20)
    String session;

    @ManyToOne
    @JoinColumn(name = "scheduleId")
    Schedule schedule;

    @Column(length = 255)
    String reason;

    @Builder.Default
    @Column(length = 20)
    String status = "pending";

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    String adminNotes;

    @Column(length = 500)
    String documentUrl;
}
