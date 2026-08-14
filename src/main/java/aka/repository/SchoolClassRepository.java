package aka.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.SchoolClass;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Integer> {
    Page<SchoolClass> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
