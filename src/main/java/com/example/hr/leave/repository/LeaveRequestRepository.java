package com.example.hr.leave.repository;


import com.example.hr.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.models.User;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByStatus(LeaveStatus pending);

    List<LeaveRequest> findByUser(User user);
@Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user WHERE (:keyword IS NULL OR l.user.fullName LIKE %:keyword%)")
List<LeaveRequest> findAllWithUser(@Param("keyword") String keyword);

    @Query(value = "SELECT l FROM LeaveRequest l JOIN FETCH l.user u LEFT JOIN FETCH u.department d WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:deptId IS NULL OR d.id = :deptId) AND " +
           "(:leaveType IS NULL OR l.leaveType = :leaveType) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:fromDate IS NULL OR l.startDate >= :fromDate) AND " +
           "(:toDate IS NULL OR l.startDate <= :toDate)",
           countQuery = "SELECT COUNT(l) FROM LeaveRequest l JOIN l.user u LEFT JOIN u.department d WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:deptId IS NULL OR d.id = :deptId) AND " +
           "(:leaveType IS NULL OR l.leaveType = :leaveType) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:fromDate IS NULL OR l.startDate >= :fromDate) AND " +
           "(:toDate IS NULL OR l.startDate <= :toDate)")
    org.springframework.data.domain.Page<LeaveRequest> searchLeaves(
            @Param("keyword") String keyword, 
            @Param("deptId") Integer deptId, 
            @Param("leaveType") com.example.hr.enums.LeaveType leaveType, 
            @Param("status") com.example.hr.enums.LeaveStatus status, 
            @Param("fromDate") java.time.LocalDate fromDate, 
            @Param("toDate") java.time.LocalDate toDate, 
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(l) FROM LeaveRequest l JOIN l.user u LEFT JOIN u.department d WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:deptId IS NULL OR d.id = :deptId) AND " +
           "(:leaveType IS NULL OR l.leaveType = :leaveType) AND " +
           "l.status = :status AND " +
           "(:fromDate IS NULL OR l.startDate >= :fromDate) AND " +
           "(:toDate IS NULL OR l.startDate <= :toDate)")
    long countLeavesByFiltersAndStatus(
            @Param("keyword") String keyword, 
            @Param("deptId") Integer deptId, 
            @Param("leaveType") com.example.hr.enums.LeaveType leaveType, 
            @Param("fromDate") java.time.LocalDate fromDate, 
            @Param("toDate") java.time.LocalDate toDate, 
            @Param("status") com.example.hr.enums.LeaveStatus status);

   long countByStatus(LeaveStatus status);

   @EntityGraph(attributePaths = "user")
   List<LeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
   
   // Advanced Leave Management methods — dùng @Query để tránh ambiguous với countByStatus(LeaveStatus)
   @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = :statusStr")
   long countByStatusString(@Param("statusStr") LeaveStatus statusStr);

   long countByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
       java.time.LocalDate endDate, java.time.LocalDate startDate, LeaveStatus status);
   List<LeaveRequest> findByUserAndStatusAndStartDateBetween(
       User user, LeaveStatus status, java.time.LocalDate startDate, java.time.LocalDate endDate);
   List<LeaveRequest> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
       LeaveStatus status, java.time.LocalDate endDate, java.time.LocalDate startDate);
   long countByUserAndStatus(User user, LeaveStatus status);
   List<LeaveRequest> findTop5ByOrderByCreatedAtDesc();
   
   // Team Analytics methods
   @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.user.department = :department AND l.status = 'PENDING'")
   long countPendingByDepartment(@Param("department") com.example.hr.department.entity.Department department);
   
   @Query("SELECT l FROM LeaveRequest l WHERE l.user.department = :department AND l.startDate >= :startDate AND l.endDate <= :endDate")
   List<LeaveRequest> findByDepartmentAndDateRange(@Param("department") com.example.hr.department.entity.Department department, 
                                                     @Param("startDate") java.time.LocalDate startDate, 
                                                     @Param("endDate") java.time.LocalDate endDate);
}
