package com.example.hr.leave.repository;

import com.example.hr.enums.LeaveType;
import com.example.hr.leave.entity.LeaveBalance;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    
    List<LeaveBalance> findByUserAndYear(User user, Integer year);
    
    Optional<LeaveBalance> findByUserAndLeaveTypeAndYear(User user, LeaveType leaveType, Integer year);
    
    List<LeaveBalance> findByYear(Integer year);
}
