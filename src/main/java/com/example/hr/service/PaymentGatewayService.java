package com.example.hr.service;

import org.springframework.stereotype.Service;
import com.example.hr.config.PaymentGatewayConfig;
import com.example.hr.models.Payment;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PaymentGatewayService {

    /**
     * Tạo URL thanh toán Momo
     */
    public String generateMomoPaymentUrl(Payment payment) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        
        params.put("partnerCode", PaymentGatewayConfig.MomoConfig.PARTNER_CODE);
        params.put("accessKey", PaymentGatewayConfig.MomoConfig.ACCESS_KEY);
        params.put("requestId", "REQUEST_" + payment.getId() + "_" + System.currentTimeMillis());
        params.put("amount", String.valueOf(payment.getAmount().longValue()));
        params.put("orderId", "ORDER_" + payment.getId());
        params.put("orderInfo", "Payment for " + payment.getUser().getFullName());
        params.put("returnUrl", PaymentGatewayConfig.MomoConfig.REDIRECT_URL);
        params.put("notifyUrl", PaymentGatewayConfig.MomoConfig.IPN_URL);
        params.put("requestType", PaymentGatewayConfig.MomoConfig.REQUEST_TYPE);
        params.put("extraData", "");
        params.put("signature", generateMomoSignature(params));
        
        return buildMomoUrlParams(params);
    }

    /**
     * Tạo signature cho Momo
     */
    private String generateMomoSignature(Map<String, String> params) {
        String data = "accessKey=" + params.get("accessKey") + 
                     "&amount=" + params.get("amount") + 
                     "&extraData=" + params.get("extraData") + 
                     "&orderId=" + params.get("orderId") + 
                     "&orderInfo=" + params.get("orderInfo") + 
                     "&partnerCode=" + params.get("partnerCode") + 
                     "&requestId=" + params.get("requestId") + 
                     "&requestType=" + params.get("requestType") + 
                     "&returnUrl=" + params.get("returnUrl");
        
        return hmacSHA256(PaymentGatewayConfig.MomoConfig.SECRET_KEY, data);
    }

    /**
     * Build Momo URL parameters
     */
    private String buildMomoUrlParams(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder(PaymentGatewayConfig.MomoConfig.ENDPOINT + "gateway/transactionProcessor");
        boolean first = true;
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                result.append("?").append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                first = false;
            } else {
                result.append("&").append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        
        return result.toString();
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    public String generateVNPayPaymentUrl(Payment payment, String ipAddress) throws UnsupportedEncodingException {
        Map<String, String> params = new TreeMap<>();
        
        params.put("vnp_Version", PaymentGatewayConfig.VNPayConfig.VERSION);
        params.put("vnp_Command", PaymentGatewayConfig.VNPayConfig.COMMAND);
        params.put("vnp_TmnCode", PaymentGatewayConfig.VNPayConfig.TMN_CODE);
        params.put("vnp_Amount", String.valueOf(payment.getAmount().longValue() * 100));
        params.put("vnp_CurrCode", PaymentGatewayConfig.VNPayConfig.CURRENCY_CODE);
        params.put("vnp_TxnRef", "TXN_" + payment.getId() + "_" + System.currentTimeMillis());
        params.put("vnp_OrderInfo", "Payment for " + payment.getUser().getFullName());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", PaymentGatewayConfig.VNPayConfig.LOCALE);
        params.put("vnp_ReturnUrl", PaymentGatewayConfig.VNPayConfig.RETURN_URL);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", getCurrentDateTime());
        params.put("vnp_ExpireDate", getExpireDateTime());
        
        String vnpRequestData = buildVNPayRequestData(params);
        String vnpSecureHash = generateVNPayHash(vnpRequestData);
        
        return PaymentGatewayConfig.VNPayConfig.API_URL + "?" + vnpRequestData + "&vnp_SecureHash=" + vnpSecureHash;
    }

    /**
     * Build VNPay request data
     */
    private String buildVNPayRequestData(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder data = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                data.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
                first = false;
            } else {
                data.append("&").append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        
        return data.toString();
    }

    /**
     * Generate VNPay hash
     */
    private String generateVNPayHash(String data) {
        String hashData = PaymentGatewayConfig.VNPayConfig.HASH_SECRET + data;
        return sha256(hashData).toLowerCase();
    }

    /**
     * HMAC SHA256
     */
    private String hmacSHA256(String key, String message) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = 
                new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(message.getBytes());
            
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * SHA256
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Get current datetime
     */
    private String getCurrentDateTime() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }

    /**
     * Get expire datetime (1 hour later)
     */
    private String getExpireDateTime() {
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusHours(1);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return expiry.format(formatter);
    }

    /**
     * Verify Momo callback
     */
    public boolean verifyMomoCallback(Map<String, String> params) {
        String signature = params.get("signature");
        if (signature == null) return false;
        
        String data = "accessKey=" + params.get("accessKey") + 
                     "&amount=" + params.get("amount") + 
                     "&extraData=" + params.get("extraData") + 
                     "&message=" + params.get("message") + 
                     "&orderId=" + params.get("orderId") + 
                     "&orderInfo=" + params.get("orderInfo") + 
                     "&orderType=" + params.get("orderType") + 
                     "&partnerCode=" + params.get("partnerCode") + 
                     "&payType=" + params.get("payType") + 
                     "&requestId=" + params.get("requestId") + 
                     "&requestType=" + params.get("requestType") + 
                     "&responseTime=" + params.get("responseTime") + 
                     "&resultCode=" + params.get("resultCode") + 
                     "&transId=" + params.get("transId");
        
        String calculatedSignature = hmacSHA256(PaymentGatewayConfig.MomoConfig.SECRET_KEY, data);
        
        return signature.equals(calculatedSignature);
    }

    /**
     * Verify VNPay callback
     */
    public boolean verifyVNPayCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) return false;
        
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");
        
        Map<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder data = new StringBuilder();
        
        boolean first = true;
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (first) {
                data.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            } else {
                data.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        
        String calculatedHash = sha256(PaymentGatewayConfig.VNPayConfig.HASH_SECRET + data.toString()).toLowerCase();
        
        return vnpSecureHash.equals(calculatedHash);
    }
}
