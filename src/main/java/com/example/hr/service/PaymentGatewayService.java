package com.example.hr.service;

import com.example.hr.config.PaymentGatewayProperties;
import com.example.hr.models.Payment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class PaymentGatewayService {

    @Autowired
    private PaymentGatewayProperties props;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * MoMo Payment v2 — POST /v2/gateway/api/create, trả về payUrl để redirect người dùng.
     */
    public String createMomoPayUrl(Payment payment) {
        PaymentGatewayProperties.Momo m = props.getMomo();
        if (!m.isEnabled() || isBlank(m.getPartnerCode()) || isBlank(m.getAccessKey()) || isBlank(m.getSecretKey())) {
            return null;
        }
        try {
            String requestId = "HRMS" + System.currentTimeMillis();
            String orderId = "ORDER_" + payment.getId();
            long amount = payment.getAmount() != null ? payment.getAmount().longValue() : 0L;
            if (amount <= 0) {
                return null;
            }

            String orderInfo = "Thanh toan HRMS #" + payment.getId();
            String extraData = "";

            String raw = "accessKey=" + m.getAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + props.momoIpnUrl()
                    + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + m.getPartnerCode()
                    + "&redirectUrl=" + props.momoRedirectUrl()
                    + "&requestId=" + requestId
                    + "&requestType=captureWallet";

            String signature = hmacSha256Hex(m.getSecretKey(), raw);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", m.getPartnerCode());
            body.put("partnerName", m.getPartnerName());
            body.put("storeId", m.getStoreId());
            body.put("requestId", requestId);
            body.put("amount", amount);
            body.put("orderId", orderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", props.momoRedirectUrl());
            body.put("ipnUrl", props.momoIpnUrl());
            body.put("lang", "vi");
            body.put("requestType", "captureWallet");
            body.put("extraData", extraData);
            body.put("signature", signature);

            String url = trimSlash(m.getEndpoint()) + m.getCreatePath();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            if (root.has("resultCode") && root.get("resultCode").asInt() == 0 && root.has("payUrl")) {
                return root.get("payUrl").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * URL thanh toán VNPay (sandbox / production) — chữ ký HMAC-SHA512 theo tài liệu VNPay.
     */
    public String createVNPayPayUrl(Payment payment, String ipAddress) {
        PaymentGatewayProperties.Vnpay v = props.getVnpay();
        if (!v.isEnabled() || isBlank(v.getTmnCode()) || isBlank(v.getHashSecret())) {
            return null;
        }
        try {
            long amountVnd = payment.getAmount() != null ? payment.getAmount().longValue() * 100L : 0L;
            if (amountVnd <= 0) {
                return null;
            }
            String txnRef = "HRMS" + payment.getId() + "T" + System.currentTimeMillis();
            Map<String, String> params = new TreeMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", v.getTmnCode());
            params.put("vnp_Amount", String.valueOf(amountVnd));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", txnRef);
            params.put("vnp_OrderInfo", "Thanh toan HRMS " + payment.getId());
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", props.vnpayReturnUrl());
            params.put("vnp_IpAddr", ipAddress != null && !ipAddress.isBlank() ? ipAddress : "127.0.0.1");
            params.put("vnp_CreateDate", vnPayDateTime());
            params.put("vnp_ExpireDate", vnPayExpireDateTime());

            String hashData = buildVNPayHashData(params);
            String secureHash = hmacSha512Hex(v.getHashSecret(), hashData);
            return v.getPayUrl() + "?" + hashData + "&vnp_SecureHash=" + secureHash;
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildVNPayHashData(Map<String, String> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    /**
     * Xác thực callback MoMo (query hoặc JSON map string hóa).
     */
    public boolean verifyMomoCallback(Map<String, String> params) {
        PaymentGatewayProperties.Momo m = props.getMomo();
        if (isBlank(m.getSecretKey())) {
            return false;
        }
        String signature = params.get("signature");
        if (signature == null) {
            return false;
        }
        String data = "accessKey=" + nz(params.get("accessKey"))
                + "&amount=" + nz(params.get("amount"))
                + "&extraData=" + nz(params.get("extraData"))
                + "&message=" + nz(params.get("message"))
                + "&orderId=" + nz(params.get("orderId"))
                + "&orderInfo=" + nz(params.get("orderInfo"))
                + "&orderType=" + nz(params.get("orderType"))
                + "&partnerCode=" + nz(params.get("partnerCode"))
                + "&payType=" + nz(params.get("payType"))
                + "&requestId=" + nz(params.get("requestId"))
                + "&responseTime=" + nz(params.get("responseTime"))
                + "&resultCode=" + nz(params.get("resultCode"))
                + "&transId=" + nz(params.get("transId"));
        try {
            String calculated = hmacSha256Hex(m.getSecretKey(), data);
            return signature.equalsIgnoreCase(calculated);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Xác thực callback / IPN VNPay.
     */
    public boolean verifyVNPayCallback(Map<String, String> params) {
        PaymentGatewayProperties.Vnpay v = props.getVnpay();
        if (isBlank(v.getHashSecret())) {
            return false;
        }
        String received = params.get("vnp_SecureHash");
        if (received == null) {
            return false;
        }
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey();
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            if (e.getValue() != null) {
                sorted.put(key, e.getValue());
            }
        }
        try {
            String hashData = buildVNPayHashData(sorted);
            String calculated = hmacSha512Hex(v.getHashSecret(), hashData);
            return received.equalsIgnoreCase(calculated);
        } catch (Exception e) {
            return false;
        }
    }

    /** Trích payment id từ orderId dạng ORDER_{id} */
    public static Integer parsePaymentIdFromMomoOrderId(String orderId) {
        if (orderId == null || !orderId.startsWith("ORDER_")) {
            return null;
        }
        try {
            return Integer.parseInt(orderId.substring("ORDER_".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Trích payment id từ vnp_TxnRef dạng HRMS{id}T{timestamp} */
    public static Integer parsePaymentIdFromVnpTxnRef(String txnRef) {
        if (txnRef == null || !txnRef.startsWith("HRMS")) {
            return null;
        }
        int t = txnRef.indexOf('T');
        if (t <= 4) {
            return null;
        }
        try {
            return Integer.parseInt(txnRef.substring(4, t));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimSlash(String s) {
        return s.replaceAll("/+$", "");
    }

    private static String hmacSha256Hex(String secretKey, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hmacSha512Hex(String secretKey, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        mac.init(key);
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String vnPayDateTime() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private static String vnPayExpireDateTime() {
        return java.time.LocalDateTime.now().plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /** Map JSON body MoMo IPN → Map<String,String> */
    public static Map<String, String> flattenMomoBody(Map<String, Object> body) {
        if (body == null) {
            return Map.of();
        }
        return body.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }
}
