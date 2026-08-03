package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.SchoolClass;
import aka.repository.SchoolClassRepository;
import aka.service.SchoolClassService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SchoolClassServiceImpl implements SchoolClassService {

    SchoolClassRepository schoolClassRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClass> findAll() {
        return schoolClassRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolClass> findById(Integer id) {
        return schoolClassRepository.findById(id);
    }

    @Override
    public SchoolClass save(SchoolClass schoolClass) {
        return schoolClassRepository.save(schoolClass);
    }

    @Override
    public void deleteById(Integer id) {
        schoolClassRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return schoolClassRepository.count();
    }
}
