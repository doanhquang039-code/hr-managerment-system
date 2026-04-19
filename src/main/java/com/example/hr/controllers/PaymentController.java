package com.example.hr.controllers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.hr.models.Payment;
import com.example.hr.models.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.PayrollRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.PaymentGatewayService;
import com.example.hr.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/payments", "/admin/payment"})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

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

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("payrolls", payrollRepository.findAll());
        model.addAttribute("payment", new Payment());
        return "admin/payment-create";
    }

    @PostMapping("/save")
    public String save(@RequestParam Integer userId,
                       @RequestParam(required = false) Integer payrollId,
                       @RequestParam String paymentType,
                       @RequestParam BigDecimal amount,
                       @RequestParam String paymentMethod,
                       @RequestParam(required = false) String accountNumber,
                       @RequestParam(required = false) String bankName,
                       @RequestParam(required = false) String notes,
                       HttpServletRequest request) {

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
            payment.setPayroll(payrollRepository.findById(payrollId).orElse(null));
        }

        Payment saved = paymentService.createPayment(payment);

        String method = paymentMethod != null ? paymentMethod.trim().toUpperCase() : "";

        if ("MOMO".equals(method)) {
            paymentService.markPaymentProcessing(saved.getId());
            Payment forGateway = paymentService.getPaymentDetail(saved.getId()).orElse(saved);
            String payUrl = paymentGatewayService.createMomoPayUrl(forGateway);
            if (payUrl != null && !payUrl.isBlank()) {
                return "redirect:" + payUrl;
            }
            return "redirect:/admin/payments?error=momo_unavailable";
        }

        if ("VNPAY".equals(method)) {
            paymentService.markPaymentProcessing(saved.getId());
            String clientIp = clientIp(request);
            Payment forGateway = paymentService.getPaymentDetail(saved.getId()).orElse(saved);
            String payUrl = paymentGatewayService.createVNPayPayUrl(forGateway, clientIp);
            if (payUrl != null && !payUrl.isBlank()) {
                return "redirect:" + payUrl;
            }
            return "redirect:/admin/payments?error=vnpay_unavailable";
        }

        return "redirect:/admin/payments?success=1";
    }

    private static String clientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id) {
        return "redirect:/admin/payments/detail/" + id;
    }

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
            return "redirect:/admin/payments?success=1";
        } catch (Exception e) {
            return "redirect:/admin/payments?error=1";
        }
    }

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

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        try {
            paymentService.deletePayment(id);
            return "redirect:/admin/payments?success=1";
        } catch (Exception e) {
            return "redirect:/admin/payments?error=1";
        }
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Optional<Payment> payment = paymentService.getPaymentDetail(id);
        if (payment.isEmpty()) {
            return "redirect:/admin/payments?error=1";
        }
        model.addAttribute("payment", payment.get());
        return "admin/payment-detail";
    }

    @GetMapping("/user/{userId}")
    public String getUserPayments(@PathVariable Integer userId, Model model) {
        model.addAttribute("payments", paymentService.getPaymentsByUserId(userId));
        model.addAttribute("user", userRepository.findById(userId).orElse(null));
        return "admin/user-payment-list";
    }

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

    @GetMapping("/callback/momo")
    public String momoCallback(@RequestParam Map<String, String> params, Model model) {
        try {
            if (!paymentGatewayService.verifyMomoCallback(params)) {
                model.addAttribute("error", "Chữ ký MoMo không hợp lệ");
                return "admin/payment-callback-error";
            }
            String orderId = params.get("orderId");
            Integer paymentId = PaymentGatewayService.parsePaymentIdFromMomoOrderId(orderId);
            if (paymentId == null) {
                model.addAttribute("error", "orderId không hợp lệ");
                return "admin/payment-callback-error";
            }
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (payment.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy giao dịch");
                return "admin/payment-callback-error";
            }
            String transId = params.get("transId");
            if ("0".equals(params.get("resultCode"))) {
                paymentService.markPaymentCompletedFromGateway(paymentId, transId);
                model.addAttribute("success", "Thanh toán MoMo thành công");
            } else {
                paymentService.markPaymentFailedFromGateway(paymentId, transId);
                model.addAttribute("error", "Thanh toán MoMo thất bại: " + params.get("message"));
            }
            model.addAttribute("payment", paymentService.getPaymentById(paymentId).orElseThrow());
            return "admin/payment-callback-success";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi xử lý callback MoMo: " + e.getMessage());
            return "admin/payment-callback-error";
        }
    }

    @PostMapping("/ipn/momo")
    @ResponseBody
    public Map<String, Object> momoIPN(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> params = PaymentGatewayService.flattenMomoBody(body);
            if (!paymentGatewayService.verifyMomoCallback(params)) {
                response.put("resultCode", 1);
                response.put("message", "Invalid signature");
                return response;
            }
            Integer paymentId = PaymentGatewayService.parsePaymentIdFromMomoOrderId(params.get("orderId"));
            if (paymentId != null && "0".equals(String.valueOf(params.get("resultCode")))) {
                paymentService.markPaymentCompletedFromGateway(paymentId, params.get("transId"));
            }
            response.put("resultCode", 0);
            response.put("message", "Processed");
        } catch (Exception e) {
            response.put("resultCode", 1);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/callback/vnpay")
    public String vnpayCallback(@RequestParam Map<String, String> params, Model model) {
        try {
            if (!paymentGatewayService.verifyVNPayCallback(params)) {
                model.addAttribute("error", "Chữ ký VNPay không hợp lệ");
                return "admin/payment-callback-error";
            }
            String txnRef = params.get("vnp_TxnRef");
            Integer paymentId = PaymentGatewayService.parsePaymentIdFromVnpTxnRef(txnRef);
            if (paymentId == null) {
                model.addAttribute("error", "Mã giao dịch VNPay không hợp lệ");
                return "admin/payment-callback-error";
            }
            Optional<Payment> payment = paymentService.getPaymentById(paymentId);
            if (payment.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy giao dịch");
                return "admin/payment-callback-error";
            }
            String transNo = params.get("vnp_TransactionNo");
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                paymentService.markPaymentCompletedFromGateway(paymentId, transNo);
                model.addAttribute("success", "Thanh toán VNPay thành công");
            } else {
                paymentService.markPaymentFailedFromGateway(paymentId, transNo);
                model.addAttribute("error", "Thanh toán VNPay thất bại. Mã: " + params.get("vnp_ResponseCode"));
            }
            model.addAttribute("payment", paymentService.getPaymentById(paymentId).orElseThrow());
            return "admin/payment-callback-success";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi xử lý callback VNPay: " + e.getMessage());
            return "admin/payment-callback-error";
        }
    }

    @GetMapping("/ipn/vnpay")
    @ResponseBody
    public Map<String, Object> vnpayIPN(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!paymentGatewayService.verifyVNPayCallback(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return response;
            }
            Integer paymentId = PaymentGatewayService.parsePaymentIdFromVnpTxnRef(params.get("vnp_TxnRef"));
            if (paymentId != null && "00".equals(params.get("vnp_ResponseCode"))) {
                paymentService.markPaymentCompletedFromGateway(paymentId, params.get("vnp_TransactionNo"));
            }
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", e.getMessage());
        }
        return response;
    }
}
