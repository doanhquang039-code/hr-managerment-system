package com.example.hr.service;

import com.example.hr.enums.AnnouncementPriority;
import com.example.hr.models.CompanyAnnouncement;
import com.example.hr.models.Department;
import com.example.hr.models.User;
import com.example.hr.repository.CompanyAnnouncementRepository;
import com.example.hr.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyAnnouncementService {

    private final CompanyAnnouncementRepository announcementRepository;
    private final DepartmentRepository departmentRepository;

    public CompanyAnnouncementService(CompanyAnnouncementRepository announcementRepository,
                                        DepartmentRepository departmentRepository) {
        this.announcementRepository = announcementRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<CompanyAnnouncement> listForEmployee(User user) {
        Integer deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        return announcementRepository.findPublishedVisibleForUser(LocalDateTime.now(), deptId);
    }

    public Optional<CompanyAnnouncement> findByIdForEmployee(Integer id, User user) {
        return announcementRepository.findById(id).filter(a -> canEmployeeSee(a, user));
    }

    private boolean canEmployeeSee(CompanyAnnouncement a, User user) {
        if (!a.isActive() || a.getPublishedAt().isAfter(LocalDateTime.now())) {
            return false;
        }
        if (a.getTargetDepartment() == null) {
            return true;
        }
        return user.getDepartment() != null
                && user.getDepartment().getId().equals(a.getTargetDepartment().getId());
    }

    public List<CompanyAnnouncement> listAllForAdmin() {
        return announcementRepository.findAllByOrderByPublishedAtDesc();
    }

    public Optional<CompanyAnnouncement> findById(Integer id) {
        return announcementRepository.findById(id);
    }

    @Transactional
    public CompanyAnnouncement create(User author, String title, String content,
                                        Integer departmentId, AnnouncementPriority priority,
                                        LocalDateTime publishedAt, boolean active) {
        CompanyAnnouncement a = new CompanyAnnouncement();
        a.setAuthor(author);
        a.setTitle(title);
        a.setContent(content);
        a.setPriority(priority != null ? priority : AnnouncementPriority.NORMAL);
        a.setPublishedAt(publishedAt != null ? publishedAt : LocalDateTime.now());
        a.setCreatedAt(LocalDateTime.now());
        a.setActive(active);
        if (departmentId != null) {
            Department d = departmentRepository.findById(departmentId).orElse(null);
            a.setTargetDepartment(d);
        }
        return announcementRepository.save(a);
    }

    @Transactional
    public CompanyAnnouncement update(Integer id, String title, String content,
                                      Integer departmentId, AnnouncementPriority priority,
                                      LocalDateTime publishedAt, boolean active) {
        CompanyAnnouncement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));
        a.setTitle(title);
        a.setContent(content);
        a.setPriority(priority != null ? priority : AnnouncementPriority.NORMAL);
        a.setPublishedAt(publishedAt != null ? publishedAt : a.getPublishedAt());
        a.setActive(active);
        if (departmentId == null) {
            a.setTargetDepartment(null);
        } else {
            a.setTargetDepartment(departmentRepository.findById(departmentId).orElse(null));
        }
        return announcementRepository.save(a);
    }

    @Transactional
    public void delete(Integer id) {
        announcementRepository.deleteById(id);
    }
}
