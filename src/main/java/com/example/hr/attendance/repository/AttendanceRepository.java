package com.example.hr.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.models.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByUser(User user);

    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);

    @Query(value = "SELECT a FROM Attendance a JOIN FETCH a.user u LEFT JOIN FETCH u.department " +
           "WHERE a.attendanceDate BETWEEN :start AND :end",
           countQuery = "SELECT COUNT(a) FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :start AND :end")
    org.springframework.data.domain.Page<Attendance> findByAttendanceDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT a FROM Attendance a JOIN FETCH a.user u LEFT JOIN FETCH u.department " +
           "WHERE a.attendanceDate BETWEEN :start AND :end AND a.status = :status",
           countQuery = "SELECT COUNT(a) FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :start AND :end AND a.status = :status")
    org.springframework.data.domain.Page<Attendance> findByAttendanceDateBetweenAndStatus(@Param("start") LocalDate start,
                                                                                          @Param("end") LocalDate end,
                                                                                          @Param("status") AttendanceStatus status,
                                                                                          org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT a FROM Attendance a JOIN FETCH a.user u LEFT JOIN FETCH u.department " +
           "WHERE (:keyword IS NULL OR u.fullName LIKE %:keyword%)",
           countQuery = "SELECT COUNT(a) FROM Attendance a JOIN a.user u " +
           "WHERE (:keyword IS NULL OR u.fullName LIKE %:keyword%)")
    org.springframework.data.domain.Page<Attendance> findAllWithUser(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT a FROM Attendance a JOIN FETCH a.user u LEFT JOIN FETCH u.department " +
           "WHERE (:keyword IS NULL OR u.fullName LIKE %:keyword%) AND a.status = :status",
           countQuery = "SELECT COUNT(a) FROM Attendance a JOIN a.user u " +
           "WHERE (:keyword IS NULL OR u.fullName LIKE %:keyword%) AND a.status = :status")
    org.springframework.data.domain.Page<Attendance> findAllWithUserAndStatus(@Param("keyword") String keyword,
                                                                              @Param("status") AttendanceStatus status,
                                                                              org.springframework.data.domain.Pageable pageable);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.user u " +
           "WHERE u = :user AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month " +
           "ORDER BY a.attendanceDate ASC")
    List<Attendance> findByUserAndYearAndMonth(@Param("user") User user,
                                               @Param("year") int year,
                                               @Param("month") int month);
    
    // Advanced Analytics methods
    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);
    long countByAttendanceDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, AttendanceStatus status);
    boolean existsByUserAndAttendanceDateAndStatus(User user, LocalDate date, AttendanceStatus status);
    boolean existsByUserAndAttendanceDate(User user, LocalDate date);
    List<Attendance> findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(User user, LocalDate startDate, LocalDate endDate);
    
    // Team Analytics methods
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user.department = :department AND a.attendanceDate = :date")
    long countByDepartmentAndDate(@Param("department") com.example.hr.models.Department department, @Param("date") LocalDate date);
}
