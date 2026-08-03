package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.School;
import aka.repository.SchoolRepository;
import aka.service.SchoolService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SchoolServiceImpl implements SchoolService {

    SchoolRepository schoolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<School> findAll() {
        return schoolRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<School> findById(Integer id) {
        return schoolRepository.findById(id);
    }

    @Override
    public School save(School school) {
        return schoolRepository.save(school);
    }

    @Override
    public void deleteById(Integer id) {
        schoolRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return schoolRepository.count();
    }
}
