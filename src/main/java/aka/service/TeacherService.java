package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.Teacher;

public interface TeacherService {
    List<Teacher> findAll();
    Optional<Teacher> findById(Integer id);
    Teacher save(Teacher teacher);
    void deleteById(Integer id);
    long count();
}
