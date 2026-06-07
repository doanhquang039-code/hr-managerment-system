package com.example.hr.service;

import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SelfServicePortalService {
    
    private final EmployeeProfileRepository profileRepository;
    private final ExpenseClaimRepository expenseClaimRepository;
    private final BenefitPlanRepository benefitPlanRepository;
    private final BenefitEnrollmentRepository benefitEnrollmentRepository;
    
    // ===== Employee Profile =====
    
    /**
     * Get or create employee profile
     */
    public EmployeeProfile getOrCreateProfile(User user) {
        Optional<EmployeeProfile> profileOpt = profileRepository.findByUser(user);
        if (profileOpt.isPresent()) {
            return profileOpt.get();
        }
        
        EmployeeProfile profile = new EmployeeProfile();
        profile.setUser(user);
        return profileRepository.save(profile);
    }
    
    /**
     * Update employee profile
     */
    public EmployeeProfile updateProfile(User user, EmployeeProfile updatedProfile) {
        EmployeeProfile profile = getOrCreateProfile(user);
        
        // Update fields
        if (updatedProfile.getEmergencyContactName() != null) 
            profile.setEmergencyContactName(updatedProfile.getEmergencyContactName());
        if (updatedProfile.getEmergencyContactPhone() != null) 
            profile.setEmergencyContactPhone(updatedProfile.getEmergencyContactPhone());
        if (updatedProfile.getEmergencyContactRelation() != null) 
            profile.setEmergencyContactRelation(updatedProfile.getEmergencyContactRelation());
        
        if (updatedProfile.getBankName() != null) 
            profile.setBankName(updatedProfile.getBankName());
        if (updatedProfile.getBankAccountNumber() != null) 
            profile.setBankAccountNumber(updatedProfile.getBankAccountNumber());
        if (updatedProfile.getBankAccountName() != null) 
            profile.setBankAccountName(updatedProfile.getBankAccountName());
        if (updatedProfile.getIdentityNumber() != null)
            profile.setIdentityNumber(updatedProfile.getIdentityNumber());
        
        if (updatedProfile.getCurrentAddress() != null) 
            profile.setCurrentAddress(updatedProfile.getCurrentAddress());
        if (updatedProfile.getPermanentAddress() != null) 
            profile.setPermanentAddress(updatedProfile.getPermanentAddress());
        
        if (updatedProfile.getBio() != null) 
            profile.setBio(updatedProfile.getBio());
        if (updatedProfile.getSkills() != null) 
            profile.setSkills(updatedProfile.getSkills());
        
        return profileRepository.save(profile);
    }
    
    // ===== Expense Claims =====
    
    /**
     * Create expense claim
     */
    public ExpenseClaim createExpenseClaim(User user, String category, String title, 
                                          String description, BigDecimal amount, 
                                          LocalDate expenseDate, String receiptUrl) {
        ExpenseClaim claim = new ExpenseClaim();
        claim.setUser(user);
        claim.setClaimNumber(generateClaimNumber());
        claim.setCategory(category);
        claim.setTitle(title);
        claim.setDescription(description);
        claim.setAmount(amount);
        claim.setExpenseDate(expenseDate);
        claim.setReceiptUrl(receiptUrl);
        claim.setStatus("PENDING");
        
        return expenseClaimRepository.save(claim);
    }
    
    /**
     * Get user's expense claims
     */
    @Transactional(readOnly = true)
    public List<ExpenseClaim> getUserExpenseClaims(User user) {
        return expenseClaimRepository.findByUser(user);
    }
    
    /**
     * Get pending expense claims
     */
    @Transactional(readOnly = true)
    public List<ExpenseClaim> getPendingClaims() {
        return expenseClaimRepository.findByStatus("PENDING");
    }
    
    /**
     * Approve expense claim
     */
    public ExpenseClaim approveClaim(Integer claimId, User approver) {
        Optional<ExpenseClaim> claimOpt = expenseClaimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            throw new RuntimeException("Expense claim not found");
        }
        
        ExpenseClaim claim = claimOpt.get();
        claim.setStatus("APPROVED");
        claim.setApprovedBy(approver);
        claim.setApprovedAt(java.time.LocalDateTime.now());
        
        return expenseClaimRepository.save(claim);
    }
    
    /**
     * Reject expense claim
     */
    public ExpenseClaim rejectClaim(Integer claimId, User approver, String reason) {
        Optional<ExpenseClaim> claimOpt = expenseClaimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            throw new RuntimeException("Expense claim not found");
        }
        
        ExpenseClaim claim = claimOpt.get();
        claim.setStatus("REJECTED");
        claim.setApprovedBy(approver);
        claim.setApprovedAt(java.time.LocalDateTime.now());
        claim.setRejectionReason(reason);
        
        return expenseClaimRepository.save(claim);
    }
    
    /**
     * Mark claim as paid
     */
    public ExpenseClaim markAsPaid(Integer claimId) {
        Optional<ExpenseClaim> claimOpt = expenseClaimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            throw new RuntimeException("Expense claim not found");
        }
        
        ExpenseClaim claim = claimOpt.get();
        claim.setStatus("PAID");
        claim.setPaidAt(java.time.LocalDateTime.now());
        
        return expenseClaimRepository.save(claim);
    }
    
    /**
     * Get total pending amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        BigDecimal total = expenseClaimRepository.getTotalPendingAmount();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    // ===== Benefits =====
    
    /**
     * Get all active benefit plans
     */
    @Transactional(readOnly = true)
    public List<BenefitPlan> getActiveBenefitPlans() {
        return benefitPlanRepository.findByIsActiveTrue();
    }
    
    /**
     * Get benefit plans by category
     */
    @Transactional(readOnly = true)
    public List<BenefitPlan> getBenefitsByCategory(String category) {
        return benefitPlanRepository.findByCategory(category);
    }
    
    /**
     * Enroll in benefit plan
     */
    public BenefitEnrollment enrollInBenefit(User user, BenefitPlan plan, LocalDate effectiveDate) {
        // Check if already enrolled
        if (benefitEnrollmentRepository.existsByUserAndBenefitPlanAndStatus(user, plan, "ACTIVE")) {
            throw new RuntimeException("Already enrolled in this benefit plan");
        }
        
        BenefitEnrollment enrollment = new BenefitEnrollment();
        enrollment.setUser(user);
        enrollment.setBenefitPlan(plan);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setEffectiveDate(effectiveDate);
        enrollment.setStatus("ACTIVE");
        
        return benefitEnrollmentRepository.save(enrollment);
    }
    
    /**
     * Get user's benefit enrollments
     */
    @Transactional(readOnly = true)
    public List<BenefitEnrollment> getUserBenefits(User user) {
        return benefitEnrollmentRepository.findByUser(user);
    }
    
    /**
     * Cancel benefit enrollment
     */
    public BenefitEnrollment cancelEnrollment(Integer enrollmentId) {
        Optional<BenefitEnrollment> enrollmentOpt = benefitEnrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new RuntimeException("Enrollment not found");
        }
        
        BenefitEnrollment enrollment = enrollmentOpt.get();
        enrollment.setStatus("CANCELLED");
        enrollment.setEndDate(LocalDate.now());
        
        return benefitEnrollmentRepository.save(enrollment);
    }
    
    // ===== Helper Methods =====
    
    private String generateClaimNumber() {
        return "EXP-" + System.currentTimeMillis();
    }
}


