package com.example.hr.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hr.models.Payment;
import com.example.hr.models.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.PaymentRepository;
import com.example.hr.repository.PayrollRepository;
import com.example.hr.repository.UserRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    /**
     * Tạo mới một thanh toán
     */
    public Payment createPayment(Payment payment) {
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    /**
     * Cập nhật thanh toán
     */
    public Payment updatePayment(Integer paymentId, Payment paymentDetails) {
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (paymentDetails.getUser() != null) {
                payment.setUser(paymentDetails.getUser());
            }
            if (paymentDetails.getPayroll() != null) {
                payment.setPayroll(paymentDetails.getPayroll());
            }
            if (paymentDetails.getPaymentType() != null) {
                payment.setPaymentType(paymentDetails.getPaymentType());
            }
            if (paymentDetails.getAmount() != null) {
                payment.setAmount(paymentDetails.getAmount());
            }
            if (paymentDetails.getPaymentMethod() != null) {
                payment.setPaymentMethod(paymentDetails.getPaymentMethod());
            }
            if (paymentDetails.getAccountNumber() != null) {
                payment.setAccountNumber(paymentDetails.getAccountNumber());
            }
            if (paymentDetails.getBankName() != null) {
                payment.setBankName(paymentDetails.getBankName());
            }
            if (paymentDetails.getTransactionId() != null) {
                payment.setTransactionId(paymentDetails.getTransactionId());
            }
            if (paymentDetails.getPaymentDate() != null) {
                payment.setPaymentDate(paymentDetails.getPaymentDate());
            }
            if (paymentDetails.getPaymentStatus() != null) {
                payment.setPaymentStatus(paymentDetails.getPaymentStatus());
            }
            if (paymentDetails.getNotes() != null) {
                payment.setNotes(paymentDetails.getNotes());
            }
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }
        return null;
    }

    /**
     * Lấy chi tiết thanh toán
     */
    public Optional<Payment> getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * Lấy tất cả thanh toán của một nhân viên
     */
    public List<Payment> getPaymentsByUserId(Integer userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy thanh toán theo trạng thái
     */
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    /**
     * Lấy thanh toán theo loại
     */
    public List<Payment> getPaymentsByType(String type) {
        return paymentRepository.findByPaymentType(type);
    }

    /**
     * Lấy thanh toán theo mã giao dịch
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    /**
     * Lấy thanh toán theo khoảng thời gian
     */
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    /**
     * Xóa thanh toán
     */
    public void deletePayment(Integer paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    /**
     * Lấy tất cả thanh toán
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Tìm kiếm thanh toán theo từ khóa (tên nhân viên hoặc mã giao dịch)
     */
    public List<Payment> searchPayments(String keyword) {
        return paymentRepository.findAllWithUser(keyword);
    }

    /**
     * Thay đổi trạng thái thanh toán
     */
    public Payment updatePaymentStatus(Integer paymentId, String newStatus) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isPresent()) {
            Payment p = payment.get();
            p.setPaymentStatus(newStatus);
            p.setUpdatedAt(LocalDateTime.now());
            if (newStatus.equals("COMPLETED")) {
                p.setPaymentDate(LocalDate.now());
            }
            return paymentRepository.save(p);
        }
        return null;
    }

    /**
     * Tính tổng tiền thanh toán theo loại
     */
    public Double getTotalPaymentByType(String paymentType) {
        return paymentRepository.getTotalAmountByType(paymentType);
    }

    /**
     * Tính tổng tiền thanh toán cho một nhân viên
     */
    public Double getTotalPaymentByUser(Integer userId) {
        return paymentRepository.getTotalAmountByUser(userId);
    }

    /**
     * Lấy thanh toán của một nhân viên theo trạng thái
     */
    public List<Payment> getPaymentsByUserIdAndStatus(Integer userId, String status) {
        return paymentRepository.findByUserIdAndPaymentStatus(userId, status);
    }

    /**
     * Lấy thanh toán liên kết với một payroll
     */
    public List<Payment> getPaymentsByPayrollId(Integer payrollId) {
        return paymentRepository.findByPayrollId(payrollId);
    }
}
