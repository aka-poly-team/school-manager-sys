package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.SchoolClass;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Integer> {
}
