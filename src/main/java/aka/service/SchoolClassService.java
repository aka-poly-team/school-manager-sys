package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.SchoolClass;

public interface SchoolClassService {
    List<SchoolClass> findAll();
    Optional<SchoolClass> findById(Integer id);
    SchoolClass save(SchoolClass schoolClass);
    void deleteById(Integer id);
    long count();
}
