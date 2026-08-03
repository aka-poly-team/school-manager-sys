package aka.service;

import java.util.List;
import java.util.Optional;
import aka.model.SystemLog;

public interface SystemLogService {
    List<SystemLog> findAll();
    Optional<SystemLog> findById(Integer id);
    SystemLog save(SystemLog systemLog);
    void deleteById(Integer id);
}
