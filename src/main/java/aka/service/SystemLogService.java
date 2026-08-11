package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.SystemLog;
import aka.model.User;

public interface SystemLogService {
    List<SystemLog> findAll();
    Optional<SystemLog> findById(Integer id);
    SystemLog save(SystemLog systemLog);
    void deleteById(Integer id);
    
    List<SystemLog> findAllByOrderByIdDesc();
    void log(User user, String action, String details);
}
