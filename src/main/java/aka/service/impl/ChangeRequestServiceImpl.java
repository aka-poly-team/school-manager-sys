package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.ChangeRequest;
import aka.repository.ChangeRequestRepository;
import aka.service.ChangeRequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ChangeRequestServiceImpl implements ChangeRequestService {

    ChangeRequestRepository changeRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChangeRequest> findAll() {
        return changeRequestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeRequest> findById(Integer id) {
        return changeRequestRepository.findById(id);
    }

    @Override
    public ChangeRequest save(ChangeRequest changeRequest) {
        return changeRequestRepository.save(changeRequest);
    }

    @Override
    public void deleteById(Integer id) {
        changeRequestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeRequest> findByTeacherIdOrderByIdDesc(Integer teacherId) {
        return changeRequestRepository.findByTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTeacherIdAndStatus(Integer teacherId, String status) {
        return changeRequestRepository.countByTeacherIdAndStatus(teacherId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeRequest> findAllByOrderByIdDesc() {
        return changeRequestRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return changeRequestRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return changeRequestRepository.count();
    }
}
