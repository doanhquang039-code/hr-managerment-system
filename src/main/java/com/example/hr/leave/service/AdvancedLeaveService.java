package com.example.hr.leave.service;

import com.example.hr.leave.dto.LeaveBalanceDTO;
import com.example.hr.enums.LeaveType;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.BalanceOperation;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.models.PublicHoliday;
import com.example.hr.models.User;
import com.example.hr.leave.repository.LeaveBalanceRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.repository.PublicHolidayRepository;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedLeaveService {
    
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void initializeLeaveBalances(User user, Integer year) {
        // Initialize default leave balances for new employee
        Map<LeaveType, Double> defaultBalances = getDefaultLeaveBalances();
        
        for (Map.Entry<LeaveType, Double> entry : defaultBalances.entrySet()) {
            LeaveBalance balance = new LeaveBalance();
            balance.setUser(user);
            balance.setLeaveType(entry.getKey());
            balance.setYear(year);
            balance.setTotalDays(entry.getValue());
            balance.setUsedDays(0.0);
            balance.setCarriedForward(0.0);
            balance.calculateRemaining();
            
            leaveBalanceRepository.save(balance);
        }
    }
    
    private Map<LeaveType, Double> getDefaultLeaveBalances() {
        Map<LeaveType, Double> balances = new HashMap<>();
        balances.put(LeaveType.ANNUAL, 15.0);
        balances.put(LeaveType.SICK, 10.0);
        balances.put(LeaveType.UNPAID, 0.0);
        balances.put(LeaveType.MATERNITY, 90.0);
        balances.put(LeaveType.PATERNITY, 7.0);
        balances.put(LeaveType.COMPASSIONATE, 3.0);
        balances.put(LeaveType.STUDY, 5.0);
        balances.put(LeaveType.COMPENSATORY, 0.0);
        return balances;
    }
    
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getUserLeaveBalances(User user, Integer year) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByUserAndYear(user, year);
        
        return balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private LeaveBalanceDTO convertToDTO(LeaveBalance balance) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setId(balance.getId());
        dto.setLeaveType(balance.getLeaveType());
        dto.setLeaveTypeName(balance.getLeaveType().getDisplayName());
        dto.setYear(balance.getYear());
        dto.setTotalDays(balance.getTotalDays());
        dto.setUsedDays(balance.getUsedDays());
        dto.setRemainingDays(balance.getRemainingDays());
        dto.setCarriedForward(balance.getCarriedForward());
        dto.setPendingDays(calculatePendingDays(balance.getUser(), balance.getLeaveType(), balance.getYear()));
        return dto;
    }
    
    private Double calculatePendingDays(User user, LeaveType leaveType, Integer year) {
        List<LeaveRequest> pendingRequests = leaveRequestRepository
                .findByUserAndStatusAndStartDateBetween(
                        user, 
                        LeaveStatus.PENDING,
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31)
                );
        
        return pendingRequests.stream()
                .filter(req -> req.getLeaveType().equals(leaveType.name()))
                .mapToDouble(req -> calculateWorkingDays(req.getStartDate(), req.getEndDate()))
                .sum();
    }
    
    @Transactional
    public void updateLeaveBalance(User user, LeaveType leaveType, Integer year, Double days, BalanceOperation operation) {
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, year);
        
        LeaveBalance balance;
        if (balanceOpt.isPresent()) {
            balance = balanceOpt.get();
        } else {
            balance = new LeaveBalance();
            balance.setUser(user);
            balance.setLeaveType(leaveType);
            balance.setYear(year);
            balance.setTotalDays(getDefaultLeaveBalances().getOrDefault(leaveType, 0.0));
            balance.setUsedDays(0.0);
            balance.setCarriedForward(0.0);
        }
        
        if (operation == BalanceOperation.ADD_USED_DAYS) {
            balance.setUsedDays(balance.getUsedDays() + days);
        } else if (operation == BalanceOperation.SUBTRACT_USED_DAYS) {
            balance.setUsedDays(Math.max(0, balance.getUsedDays() - days));
        }
        
        balance.calculateRemaining();
        leaveBalanceRepository.save(balance);
    }
    
    public Double calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return 0.0;
        }
        
        List<LocalDate> holidays = publicHolidayRepository
                .findByDateBetweenAndIsActive(startDate, endDate, true)
                .stream()
                .map(PublicHoliday::getDate)
                .collect(Collectors.toList());
        
        long workingDays = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    return dayOfWeek != DayOfWeek.SATURDAY && 
                           dayOfWeek != DayOfWeek.SUNDAY &&
                           !holidays.contains(date);
                })
                .count();
        
        return (double) workingDays;
    }
    
    @Transactional
    public void carryForwardLeaves(Integer fromYear, Integer toYear) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByYear(fromYear);
        List<LeaveBalance> balancesToSave = new ArrayList<>();
        
        for (LeaveBalance oldBalance : balances) {
            if (oldBalance.getLeaveType() == LeaveType.ANNUAL && oldBalance.getRemainingDays() > 0) {
                // Carry forward up to 5 days of annual leave
                double carryForward = Math.min(oldBalance.getRemainingDays(), 5.0);
                
                Optional<LeaveBalance> newBalanceOpt = leaveBalanceRepository
                        .findByUserAndLeaveTypeAndYear(oldBalance.getUser(), LeaveType.ANNUAL, toYear);
                
                LeaveBalance newBalance;
                if (newBalanceOpt.isPresent()) {
                    newBalance = newBalanceOpt.get();
                } else {
                    newBalance = new LeaveBalance();
                    newBalance.setUser(oldBalance.getUser());
                    newBalance.setLeaveType(LeaveType.ANNUAL);
                    newBalance.setYear(toYear);
                    newBalance.setTotalDays(15.0);
                    newBalance.setUsedDays(0.0);
                }
                
                newBalance.setCarriedForward(carryForward);
                newBalance.calculateRemaining();
                balancesToSave.add(newBalance);
            }
        }
        if (!balancesToSave.isEmpty()) {
            leaveBalanceRepository.saveAll(balancesToSave);
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getLeaveCalendar(Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LeaveStatus.APPROVED, endDate, startDate);
        
        List<PublicHoliday> holidays = publicHolidayRepository
                .findByDateBetweenAndIsActive(startDate, endDate, true);
        
        Map<String, Object> calendar = new HashMap<>();
        calendar.put("leaves", approvedLeaves);
        calendar.put("holidays", holidays);
        calendar.put("startDate", startDate);
        calendar.put("endDate", endDate);
        
        return calendar;
    }
    
    @Transactional(readOnly = true)
    public boolean canApplyLeave(User user, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        Integer year = startDate.getYear();
        double requestedDays = calculateWorkingDays(startDate, endDate);
        
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, year);
        
        if (balanceOpt.isEmpty()) {
            return false;
        }
        
        LeaveBalance balance = balanceOpt.get();
        return balance.getRemainingDays() >= requestedDays;
    }
}
