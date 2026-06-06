package com.example.hr.service;

import com.example.hr.dto.EmployeeBenefitDTO;
import com.example.hr.enums.BenefitStatus;
import com.example.hr.enums.BenefitType;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.EmployeeBenefit;
import com.example.hr.models.User;
import com.example.hr.repository.EmployeeBenefitRepository;
import com.example.hr.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quản lý phúc lợi nhân viên.
 * Bao gồm: assign/revoke, auto-expire, tính chi phí.
 */
@Service
@Transactional
public class BenefitService {

    private static final Logger log = LoggerFactory.getLogger(BenefitService.class);

    private final EmployeeBenefitRepository benefitRepository;
    private final UserRepository userRepository;

    public BenefitService(EmployeeBenefitRepository benefitRepository,
                           UserRepository userRepository) {
        this.benefitRepository = benefitRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy tất cả phúc lợi.
     */
    @Transactional(readOnly = true)
    public List<EmployeeBenefit> getAllBenefits() {
        return benefitRepository.findAll();
    }

    /**
     * Lấy phúc lợi theo ID.
     */
    @Transactional(readOnly = true)
    public EmployeeBenefit getBenefitById(Integer id) {
        return benefitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phúc lợi", id));
    }

    /**
     * Lấy phúc lợi theo nhân viên.
     */
    @Transactional(readOnly = true)
    public List<EmployeeBenefit> getBenefitsByUser(Integer userId) {
        return benefitRepository.findByUserId(userId);
    }

    /**
     * Lấy phúc lợi đang hoạt động theo nhân viên.
     */
    @Transactional(readOnly = true)
    public List<EmployeeBenefit> getActiveBenefitsByUser(Integer userId) {
        return benefitRepository.findByUserIdAndStatus(userId, BenefitStatus.ACTIVE);
    }

    /**
     * Gán phúc lợi cho nhân viên.
     */
    public EmployeeBenefit assignBenefit(EmployeeBenefitDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", dto.getUserId()));

        // Validate dates
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessValidationException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        EmployeeBenefit benefit = new EmployeeBenefit();
        benefit.setUser(user);
        benefit.setBenefitType(dto.getBenefitType());
        benefit.setBenefitName(dto.getBenefitName());
        benefit.setDescription(dto.getDescription());
        benefit.setMonetaryValue(dto.getMonetaryValue() != null ? dto.getMonetaryValue() : BigDecimal.ZERO);
        benefit.setStartDate(dto.getStartDate());
        benefit.setEndDate(dto.getEndDate());
        benefit.setProvider(dto.getProvider());
        benefit.setPolicyNumber(dto.getPolicyNumber());
        benefit.setStatus(BenefitStatus.ACTIVE);
        benefit.setCreatedAt(LocalDateTime.now());

        log.info("Benefit assigned: user={}, type={}, name={}",
                user.getUsername(), dto.getBenefitType(), dto.getBenefitName());

        return benefitRepository.save(benefit);
    }

    /**
     * Cập nhật phúc lợi.
     */
    public EmployeeBenefit updateBenefit(Integer id, EmployeeBenefitDTO dto) {
        EmployeeBenefit benefit = getBenefitById(id);

        if (dto.getBenefitName() != null) benefit.setBenefitName(dto.getBenefitName());
        if (dto.getDescription() != null) benefit.setDescription(dto.getDescription());
        if (dto.getMonetaryValue() != null) benefit.setMonetaryValue(dto.getMonetaryValue());
        if (dto.getEndDate() != null) benefit.setEndDate(dto.getEndDate());
        if (dto.getProvider() != null) benefit.setProvider(dto.getProvider());
        if (dto.getPolicyNumber() != null) benefit.setPolicyNumber(dto.getPolicyNumber());

        return benefitRepository.save(benefit);
    }

    /**
     * Hủy phúc lợi.
     */
    public EmployeeBenefit cancelBenefit(Integer id) {
        EmployeeBenefit benefit = getBenefitById(id);
        benefit.setStatus(BenefitStatus.CANCELLED);
        log.info("Benefit cancelled: id={}, user={}", id, benefit.getUser().getUsername());
        return benefitRepository.save(benefit);
    }

    /**
     * Xóa phúc lợi.
     */
    public void deleteBenefit(Integer id) {
        if (!benefitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Phúc lợi", id);
        }
        benefitRepository.deleteById(id);
    }

    /**
     * Auto-expire tất cả phúc lợi đã hết hạn nhưng còn status ACTIVE.
     */
    public int autoExpireBenefits() {
        List<EmployeeBenefit> expired = benefitRepository.findExpiredButActive(LocalDate.now());
        int count = 0;
        for (EmployeeBenefit benefit : expired) {
            if (benefit.autoExpireIfNeeded()) {
                benefitRepository.save(benefit);
                count++;
                log.info("Auto-expired benefit: id={}, user={}, name={}",
                        benefit.getId(), benefit.getUser().getUsername(), benefit.getBenefitName());
            }
        }
        return count;
    }

    /**
     * Lấy phúc lợi sắp hết hạn (30 ngày).
     */
    @Transactional(readOnly = true)
    public List<EmployeeBenefit> getExpiringSoonBenefits() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        return benefitRepository.findExpiringSoon(start, end);
    }

    /**
     * Tổng chi phí phúc lợi đang hoạt động.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalActiveBenefitCost() {
        return benefitRepository.sumTotalActiveBenefitCost();
    }

    /**
     * Chi phí phúc lợi của nhân viên.
     */
    @Transactional(readOnly = true)
    public BigDecimal getUserBenefitCost(Integer userId) {
        return benefitRepository.sumActiveValueByUser(userId);
    }

    /**
     * Thống kê phúc lợi theo loại.
     */
    @Transactional(readOnly = true)
    public Map<BenefitType, Long> countActiveBenefitsByType() {
        return benefitRepository.countActiveBenefitsByType().stream()
                .collect(Collectors.toMap(
                        row -> (BenefitType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Chi phí phúc lợi theo phòng ban.
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCostByDepartment() {
        return benefitRepository.sumCostByDepartment().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }
}
