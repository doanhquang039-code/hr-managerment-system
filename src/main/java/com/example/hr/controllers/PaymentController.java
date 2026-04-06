package com.example.hr.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.hr.models.Payment;
import com.example.hr.models.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.PaymentRepository;
import com.example.hr.repository.PayrollRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.PaymentGatewayService;
import com.example.hr.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayrollRepository payrollRepository;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;

    /**
     * Danh sách tất cả thanh toán
     */
    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword, 
                       @RequestParam(name = "status", required = false) String status,
                       Model model) {
        List<Payment> payments;
        
        if (keyword != null && !keyword.isEmpty()) {
            payments = paymentService.searchPayments(keyword);
        } else if (status != null && !status.isEmpty()) {
            payments = paymentService.getPaymentsByStatus(status);
        } else {
            payments = paymentService.getAllPayments();
        }
        
        model.addAttribute("payments", payments);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/payment-list";
    }

    /**
     * Trang tạo thanh toán mới
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        List<User> users = userRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();
        
        model.addAttribute("users", users);
        model.addAttribute("payrolls", payrolls);
        model.addAttribute("payment", new Payment());
        
        return "admin/payment-create";
    }

    /**
     * Lưu thanh toán mới orseconds
     */
    @PostMapping("/save")
    public String save(@RequestParam Integer userId,
                       @RequestParam(required = false) Integer payrollId,
                       @RequestParam String paymentType,
                       @RequestParam BigDecimal amount,
                       @RequestParam String paymentMethod,
                       @RequestParam(required = false) String accountNumber,
                       @RequestParam(required = false) String bankName,
                       @RequestParam(required = false) String notes,
                       Model model) {
        
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
            
            Payment payment = new Payment();
            payment.setUser(user);
            payment.setPaymentType(paymentType);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setAccountNumber(accountNumber);
            payment.setBankName(bankName);
            payment.setNotes(notes);
            payment.setPaymentStatus("PENDING");
            
            if (payrollId != null && payrollId > 0) {
                Payroll payroll = payrollRepository.findById(payrollId).orElse(null);
                payment.setPayroll(payroll);
            }
            
            paymentService.createPayment(payment);
            return "redirect:/admin/payment?success=1";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tạo thanh toán: " + e.getMessage());
            return "redirect:/admin/payment?error=1";
        }
    }

    /**
     * Trang chỉnh sửa thanh toán
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        if (!payment.isPresent()) {
            return "redirect:/admin/payment?error=1";
        }
        
        List<User> users = userRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();
        
        model.addAttribute("payment", payment.get());
        model.addAttribute("users", users);
        model.addAttribute("payrolls", payrolls);
        
        return "admin/payment-edit";
    }

    /**
     * Cập nhật thanh toán
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam Integer userId,
                         @RequestParam(required = false) Integer payrollId,
                         @RequestParam String paymentType,
                         @RequestParam BigDecimal amount,
                         @RequestParam String paymentMethod,
                         @RequestParam(required = false) String accountNumber,
                         @RequestParam(required = false) String bankName,
                         @RequestParam String paymentStatus,
                         @RequestParam(required = false) String transactionId,
                         @RequestParam(required = false) String notes) {
        
        try {
            Payment payment = new Payment();
            payment.setUser(userRepository.findById(userId).orElse(null));
            payment.setPaymentType(paymentType);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setAccountNumber(accountNumber);
            payment.setBankName(bankName);
            payment.setPaymentStatus(paymentStatus);
            payment.setTransactionId(transactionId);
            payment.setNotes(notes);
            
            if (payrollId != null && payrollId > 0) {
                payment.setPayroll(payrollRepository.findById(payrollId).orElse(null));
            }
            
            paymentService.updatePayment(id, payment);
            return "redirect:/admin/payment?success=1";
            
        } catch (Exception e) {
            return "redirect:/admin/payment?error=1";
        }
    }

    /**
     * Thay đổi trạng thái thanh toán
     */
    @PostMapping("/updateStatus/{id}")
    @ResponseBody
    public String updateStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            paymentService.updatePaymentStatus(id, status);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * Xóa thanh toán
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        try {
            paymentService.deletePayment(id);
            return "redirect:/admin/payment?success=1";
        } catch (Exception e) {
            return "redirect:/admin/payment?error=1";
        }
    }

    /**
     * Chi tiết thanh toán
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        if (!payment.isPresent()) {
            return "redirect:/admin/payment?error=1";
        }
        
        model.addAttribute("payment", payment.get());
        return "admin/payment-detail";
    }

    /**
     * Thanh toán của một nhân viên
     */
    @GetMapping("/user/{userId}")
    public String getUserPayments(@PathVariable Integer userId, Model model) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        Optional<User> user = userRepository.findById(userId);
        
        model.addAttribute("payments", payments);
        model.addAttribute("user", user.orElse(null));
        
        return "admin/user-payment-list";
    }

    /**
     * Thống kê thanh toán theo loại
     */
    @GetMapping("/statistics")
    public String statistics(Model model) {
        List<Payment> allPayments = paymentService.getAllPayments();
        
        Double totalSalary = paymentService.getTotalPaymentByType("SALARY");
        Double totalBonus = paymentService.getTotalPaymentByType("BONUS");
        Double totalReward = paymentService.getTotalPaymentByType("REWARD");
        Double totalAdvance = paymentService.getTotalPaymentByType("ADVANCE");
        
        model.addAttribute("allPayments", allPayments);
        model.addAttribute("totalSalary", totalSalary != null ? totalSalary : 0);
        model.addAttribute("totalBonus", totalBonus != null ? totalBonus : 0);
        model.addAttribute("totalReward", totalReward != null ? totalReward : 0);
        model.addAttribute("totalAdvance", totalAdvance != null ? totalAdvance : 0);
        
        return "admin/payment-statistics";
    }
    
    /**
     * Momo Payment Callback - Xử lý kết quả từ Momo
     */
    @GetMapping("/callback/momo")
    public String momoCallback(@RequestParam Map<String, String> params, Model model) {
        try {
            // Verify callback từ Momo
            if (!paymentGatewayService.verifyMomoCallback(params)) {
                model.addAttribute("error", "Invalid Momo callback signature");
                return "admin/payment-callback-error";
            }
            
            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String transId = params.get("transId");
            
            // Extract payment ID from orderId (format: ORDER_<paymentId>)
            Integer paymentId = Integer.parseInt(orderId.split("_")[1]);
            
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (!payment.isPresent()) {
                model.addAttribute("error", "Payment not found");
                return "admin/payment-callback-error";
            }
            
            // Cập nhật trạng thái thanh toán
            if ("0".equals(resultCode)) {
                // Success
                Payment p = payment.get();
                p.setPaymentStatus("COMPLETED");
                p.setTransactionId(transId);
                p.setPaymentDate(LocalDate.now());
                paymentService.updatePayment(paymentId, p);
                
                model.addAttribute("success", "Thanh toán Momo thành công");
            } else {
                // Failed
                Payment p = payment.get();
                p.setPaymentStatus("FAILED");
                p.setTransactionId(transId);
                paymentService.updatePayment(paymentId, p);
                
                model.addAttribute("error", "Thanh toán Momo thất bại: " + params.get("message"));
            }
            
            model.addAttribute("payment", payment.get());
            return "admin/payment-callback-success";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error processing Momo callback: " + e.getMessage());
            return "admin/payment-callback-error";
        }
    }
    
    /**
     * Momo IPN (Instant Payment Notification) - Webhook từ Momo
     */
    @PostMapping("/ipn/momo")
    @ResponseBody
    public Map<String, Object> momoIPN(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify IPN signature
            if (!paymentGatewayService.verifyMomoCallback(params)) {
                response.put("resultCode", "1");
                response.put("message", "Invalid signature");
                return response;
            }
            
            String orderId = params.get("orderId");
            Integer paymentId = Integer.parseInt(orderId.split("_")[1]);
            
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (payment.isPresent() && "0".equals(params.get("resultCode"))) {
                Payment p = payment.get();
                p.setPaymentStatus("COMPLETED");
                p.setTransactionId(params.get("transId"));
                p.setPaymentDate(LocalDate.now());
                paymentService.updatePayment(paymentId, p);
            }
            
            response.put("resultCode", "0");
            response.put("message", "INP processed successfully");
        } catch (Exception e) {
            response.put("resultCode", "1");
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * VNPay Payment Callback
     */
    @GetMapping("/callback/vnpay")
    public String vnpayCallback(@RequestParam Map<String, String> params, Model model) {
        try {
            // Verify callback từ VNPay
            if (!paymentGatewayService.verifyVNPayCallback(params)) {
                model.addAttribute("error", "Invalid VNPay callback signature");
                return "admin/payment-callback-error";
            }
            
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transNo = params.get("vnp_TransactionNo");
            
            // Extract payment ID from txnRef (format: TXN_<paymentId>_<timestamp>)
            Integer paymentId = Integer.parseInt(txnRef.split("_")[1]);
            
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (!payment.isPresent()) {
                model.addAttribute("error", "Payment not found");
                return "admin/payment-callback-error";
            }
            
            // Cập nhật trạng thái thanh toán
            if ("00".equals(responseCode)) {
                // Success
                Payment p = payment.get();
                p.setPaymentStatus("COMPLETED");
                p.setTransactionId(transNo);
                p.setPaymentDate(LocalDate.now());
                paymentService.updatePayment(paymentId, p);
                
                model.addAttribute("success", "Thanh toán VNPay thành công");
            } else {
                // Failed
                Payment p = payment.get();
                p.setPaymentStatus("FAILED");
                p.setTransactionId(transNo);
                paymentService.updatePayment(paymentId, p);
                
                model.addAttribute("error", "Thanh toán VNPay thất bại. Mã: " + responseCode);
            }
            
            model.addAttribute("payment", payment.get());
            return "admin/payment-callback-success";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error processing VNPay callback: " + e.getMessage());
            return "admin/payment-callback-error";
        }
    }
    
    /**
     * VNPay IPN (Instant Payment Notification) - Webhook từ VNPay
     */
    @GetMapping("/ipn/vnpay")
    @ResponseBody
    public Map<String, Object> vnpayIPN(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify IPN signature
            if (!paymentGatewayService.verifyVNPayCallback(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return response;
            }
            
            String txnRef = params.get("vnp_TxnRef");
            Integer paymentId = Integer.parseInt(txnRef.split("_")[1]);
            
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (payment.isPresent() && "00".equals(params.get("vnp_ResponseCode"))) {
                Payment p = payment.get();
                p.setPaymentStatus("COMPLETED");
                p.setTransactionId(params.get("vnp_TransactionNo"));
                p.setPaymentDate(LocalDate.now());
                paymentService.updatePayment(paymentId, p);
            }
            
            response.put("RspCode", "00");
            response.put("Message", "Confirm received");
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Error: " + e.getMessage());
        }
        
        return response;
    }
}
