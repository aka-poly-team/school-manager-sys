package aka.model;

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
@Table(name = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String message;

    @Column(length = 255)
    String link;

    @Column(nullable = false)
    Boolean forAdmin;

    @ManyToOne
    @JoinColumn(name = "teacherId")
    Teacher teacher;

    @Builder.Default
    @Column(nullable = false)
    Boolean isRead = false;

    @Builder.Default
    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
