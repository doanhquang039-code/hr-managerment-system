package com.example.hr.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hr.models.Payment;
import com.example.hr.models.User;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Tìm thanh toán theo id nhân viên
    List<Payment> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Tìm thanh toán theo trạng thái
    List<Payment> findByPaymentStatus(String paymentStatus);

    // Tìm thanh toán theo loại thanh toán
    List<Payment> findByPaymentType(String paymentType);

    // Tìm thanh toán theo mã giao dịch
    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.user LEFT JOIN FETCH p.payroll WHERE p.id = :id")
    Optional<Payment> findByIdWithRelations(@Param("id") Integer id);

    // Tìm thanh toán theo payroll id
    List<Payment> findByPayrollId(Integer payrollId);

    // Tìm thanh toán theo người dùng
    List<Payment> findByUser(User user);

    // Tìm thanh toán theo khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Tìm thanh toán theo người dùng và trạng thái
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = :status ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdAndPaymentStatus(@Param("userId") Integer userId, @Param("status") String status);

    // Tìm tất cả thanh toán với thông tin người dùng
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.user WHERE (:keyword IS NULL OR p.user.fullName LIKE %:keyword% OR p.transactionId LIKE %:keyword%)")
    List<Payment> findAllWithUser(@Param("keyword") String keyword);

    // Tổng số tiền thanh toán theo loại
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentType = :paymentType AND p.paymentStatus = 'COMPLETED'")
    Double getTotalAmountByType(@Param("paymentType") String paymentType);

    // Tổng số tiền thanh toán theo người dùng
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = 'COMPLETED'")
    Double getTotalAmountByUser(@Param("userId") Integer userId);
}
