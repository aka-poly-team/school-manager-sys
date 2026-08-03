package aka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aka.model.SystemLog;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
}
