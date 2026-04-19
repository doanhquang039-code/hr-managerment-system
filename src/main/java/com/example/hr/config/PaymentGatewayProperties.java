package com.example.hr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình cổng thanh toán (MoMo, VNPay). Điền khóa thật trong application.properties / biến môi trường.
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentGatewayProperties {

    /**
     * URL gốc ứng dụng (không có dấu / cuối), dùng ghép returnUrl / ipnUrl cho MoMo & VNPay.
     */
    private String baseUrl = "http://localhost:8080";

    private Momo momo = new Momo();
    private Vnpay vnpay = new Vnpay();

    public String paymentsCallbackBase() {
        return trimSlash(baseUrl) + "/admin/payments";
    }

    public String momoRedirectUrl() {
        return paymentsCallbackBase() + "/callback/momo";
    }

    public String momoIpnUrl() {
        return paymentsCallbackBase() + "/ipn/momo";
    }

    public String vnpayReturnUrl() {
        return paymentsCallbackBase() + "/callback/vnpay";
    }

    public String vnpayIpnUrl() {
        return paymentsCallbackBase() + "/ipn/vnpay";
    }

    private static String trimSlash(String s) {
        if (s == null || s.isBlank()) {
            return "http://localhost:8080";
        }
        return s.replaceAll("/+$", "");
    }

    @Data
    public static class Momo {
        /** Bật khi đã cấu hình đủ partnerCode / accessKey / secretKey */
        private boolean enabled = false;
        private String partnerCode = "";
        private String partnerName = "HRMS";
        private String storeId = "HRMS";
        private String accessKey = "";
        private String secretKey = "";
        /** Host MoMo, ví dụ https://test-payment.momo.vn */
        private String endpoint = "https://test-payment.momo.vn";
        /** Đường dẫn API tạo giao dịch (mặc định MoMo v2) */
        private String createPath = "/v2/gateway/api/create";
    }

    @Data
    public static class Vnpay {
        private boolean enabled = false;
        private String tmnCode = "";
        private String hashSecret = "";
        private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    }
}
