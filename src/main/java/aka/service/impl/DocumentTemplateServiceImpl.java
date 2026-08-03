package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.DocumentTemplate;
import aka.repository.DocumentTemplateRepository;
import aka.service.DocumentTemplateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

    DocumentTemplateRepository documentTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplate> findAll() {
        return documentTemplateRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentTemplate> findById(Integer id) {
        return documentTemplateRepository.findById(id);
    }

    @Override
    public DocumentTemplate save(DocumentTemplate documentTemplate) {
        return documentTemplateRepository.save(documentTemplate);
    }

    @Override
    public void deleteById(Integer id) {
        documentTemplateRepository.deleteById(id);
    }
}
