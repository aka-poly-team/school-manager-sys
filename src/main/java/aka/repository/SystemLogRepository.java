package aka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.SystemLog;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
    List<SystemLog> findAllByOrderByIdDesc();
}
