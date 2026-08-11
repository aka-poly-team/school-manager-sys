package aka.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aka.model.Complaint;
import aka.repository.ComplaintRepository;
import aka.service.ComplaintService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    ComplaintRepository complaintRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findAll() {
        return complaintRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Complaint> findById(Integer id) {
        return complaintRepository.findById(id);
    }

    @Override
    public Complaint save(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Override
    public void deleteById(Integer id) {
        complaintRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findByAttendanceTeacherIdOrderByIdDesc(Integer teacherId) {
        return complaintRepository.findByAttendanceTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAttendanceTeacherIdAndStatus(Integer teacherId, Integer status) {
        return complaintRepository.countByAttendanceTeacherIdAndStatus(teacherId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findAllByOrderByIdDesc() {
        return complaintRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return complaintRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAttendanceId(Long attendanceId) {
        return complaintRepository.existsByAttendanceId(attendanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return complaintRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return complaintRepository.count();
    }
}
