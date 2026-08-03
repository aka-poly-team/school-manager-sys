package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.DocumentTemplate;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Integer> {
}
