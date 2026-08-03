package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.SystemLog;
import aka.repository.SystemLogRepository;
import aka.service.SystemLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SystemLogServiceImpl implements SystemLogService {

    SystemLogRepository systemLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemLog> findAll() {
        return systemLogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SystemLog> findById(Integer id) {
        return systemLogRepository.findById(id);
    }

    @Override
    public SystemLog save(SystemLog systemLog) {
        return systemLogRepository.save(systemLog);
    }

    @Override
    public void deleteById(Integer id) {
        systemLogRepository.deleteById(id);
    }
}
