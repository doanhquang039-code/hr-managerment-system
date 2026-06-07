package com.example.hr.recruitment.service;

import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.department.entity.Department;
import com.example.hr.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    /**
     * Create new job posting
     */
    @Transactional
    public JobPosting createJobPosting(JobPosting jobPosting) {
        jobPosting.setCreatedAt(LocalDateTime.now());
        jobPosting.setStatus("DRAFT");
        jobPosting.setViewsCount(0);
        jobPosting.setApplicationsCount(0);
        return jobPostingRepository.save(jobPosting);
    }

    /**
     * Update job posting
     */
    @Transactional
    public JobPosting updateJobPosting(Integer id, JobPosting jobData) {
        JobPosting job = getJobPostingById(id);
        
        job.setTitle(jobData.getTitle());
        job.setDescription(jobData.getDescription());
        job.setRequirements(jobData.getRequirements());
        job.setDepartment(jobData.getDepartment());
        job.setPosition(jobData.getPosition());
        job.setEmploymentType(jobData.getEmploymentType());
        job.setExperienceLevel(jobData.getExperienceLevel());
        job.setSalaryMin(jobData.getSalaryMin());
        job.setSalaryMax(jobData.getSalaryMax());
        job.setLocation(jobData.getLocation());
        job.setRemoteAllowed(jobData.getRemoteAllowed());
        job.setClosingDate(jobData.getClosingDate());
        job.setUpdatedAt(LocalDateTime.now());
        
        return jobPostingRepository.save(job);
    }

    /**
     * Publish job posting
     */
    @Transactional
    public JobPosting publishJobPosting(Integer id) {
        JobPosting job = getJobPostingById(id);
        job.setStatus("ACTIVE");
        job.setPostingDate(LocalDate.now());
        job.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(job);
    }

    /**
     * Close job posting
     */
    @Transactional
    public JobPosting closeJobPosting(Integer id) {
        JobPosting job = getJobPostingById(id);
        job.setStatus("CLOSED");
        job.setClosingDate(LocalDate.now());
        job.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(job);
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(Integer id) {
        JobPosting job = getJobPostingById(id);
        job.setViewsCount(job.getViewsCount() + 1);
        jobPostingRepository.save(job);
    }

    /**
     * Increment application count
     */
    @Transactional
    public void incrementApplicationCount(Integer id) {
        JobPosting job = getJobPostingById(id);
        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobPostingRepository.save(job);
    }

    /**
     * Get job posting by ID
     */
    public JobPosting getJobPostingById(Integer id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + id));
    }

    /**
     * Get all job postings
     */
    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    /**
     * Get active job postings
     */
    public List<JobPosting> getActiveJobPostings() {
        return jobPostingRepository.findActiveJobPostings(LocalDate.now());
    }

    /**
     * Get job postings by status
     */
    public List<JobPosting> getJobPostingsByStatus(String status) {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get job postings by department
     */
    public List<JobPosting> getJobPostingsByDepartment(Department department) {
        return jobPostingRepository.findByDepartmentOrderByCreatedAtDesc(department);
    }

    /**
     * Search job postings
     */
    public List<JobPosting> searchJobPostings(String keyword) {
        return jobPostingRepository.searchByKeyword(keyword);
    }

    public Page<JobPosting> searchJobPostings(String keyword, String status, String employmentType,
                                              String experienceLevel, Integer departmentId, Pageable pageable) {
        return jobPostingRepository.searchJobPostings(keyword, status, employmentType, experienceLevel, departmentId, pageable);
    }

    @Transactional
    public void deleteJobPosting(Integer id) {
        jobPostingRepository.delete(getJobPostingById(id));
    }

    /**
     * Get jobs closing soon
     */
    public List<JobPosting> getJobsClosingSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return jobPostingRepository.findJobsClosingSoon(startDate, endDate);
    }

    /**
     * Get job posting statistics
     */
    public JobPostingStatistics getJobPostingStatistics() {
        long totalJobs = jobPostingRepository.count();
        long activeJobs = jobPostingRepository.countByStatus("ACTIVE");
        long draftJobs = jobPostingRepository.countByStatus("DRAFT");
        long closedJobs = jobPostingRepository.countByStatus("CLOSED");
        
        Long totalApplications = jobPostingRepository.getTotalApplicationsCount();
        Double avgApplications = jobPostingRepository.getAverageApplicationsPerJob();
        
        return new JobPostingStatistics(
                totalJobs,
                activeJobs,
                draftJobs,
                closedJobs,
                totalApplications != null ? totalApplications : 0L,
                avgApplications != null ? avgApplications : 0.0
        );
    }

    public record JobPostingStatistics(
            long totalJobs,
            long activeJobs,
            long draftJobs,
            long closedJobs,
            long totalApplications,
            double avgApplicationsPerJob
    ) {}
}


