package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.DocumentTemplate;

public interface DocumentTemplateService {
    List<DocumentTemplate> findAll();
    Optional<DocumentTemplate> findById(Integer id);
    DocumentTemplate save(DocumentTemplate documentTemplate);
    void deleteById(Integer id);
}
