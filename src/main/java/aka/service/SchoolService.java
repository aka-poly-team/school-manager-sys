package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.School;

public interface SchoolService {
    List<School> findAll();
    Optional<School> findById(Integer id);
    School save(School school);
    void deleteById(Integer id);
    long count();
}
