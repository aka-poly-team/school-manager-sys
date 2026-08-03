package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
}
