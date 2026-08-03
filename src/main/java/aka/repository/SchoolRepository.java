package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.School;

public interface SchoolRepository extends JpaRepository<School, Integer> {
}
