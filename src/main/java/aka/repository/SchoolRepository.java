package aka.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.School;

public interface SchoolRepository extends JpaRepository<School, Integer> {
    Page<School> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
