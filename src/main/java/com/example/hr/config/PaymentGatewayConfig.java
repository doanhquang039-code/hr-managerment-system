package com.example.hr.config;

public class PaymentGatewayConfig {
    
    /**
     * Momo Payment Gateway Configuration
     */
    public static final class MomoConfig {
        public static final String PARTNER_CODE = "MOMO";
        public static final String ACCESS_KEY = "F8590EC3070D82F4D4E39E4E";
        public static final String SECRET_KEY = "0f30c78b4a5f94c97f5a5f5b5f5f5f5f";
        public static final String ENDPOINT = "https://test-payment.momo.vn/v1/";
        public static final String REDIRECT_URL = "http://localhost:8080/admin/payment/callback/momo";
        public static final String IPN_URL = "http://localhost:8080/admin/payment/ipn/momo";
        public static final String REQUEST_TYPE = "captureWallet";
        
        public static final class Errors {
            public static final String SUCCESS = "0";
            public static final String AUTH_FAILED = "1";
            public static final String INVALID_REQUEST = "1005";
            public static final String NETWORK_ERROR = "104";
        }
    }
    
    /**
     * VNPay Payment Gateway Configuration
     */
    public static final class VNPayConfig {
        public static final String TMN_CODE = "TMNCODE";
        public static final String HASH_SECRET = "HASHSECRET";
        public static final String API_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        public static final String API_TRANS_URL = "https://sandbox.vnpayment.vn/merchant_webapi/transaction";
        public static final String RETURN_URL = "http://localhost:8080/admin/payment/callback/vnpay";
        public static final String NOTIFY_URL = "http://localhost:8080/admin/payment/ipn/vnpay";
        public static final String COMMAND = "pay";
        public static final String VERSION = "2.1.0";
        public static final String CURRENCY_CODE = "VND";
        public static final String LOCALE = "vn";
        
        public static final class Errors {
            public static final String SUCCESS = "00";
            public static final String INVALID_COMMAND = "01";
            public static final String INVALID_VERSION = "02";
            public static final String INVALID_AMOUNT = "03";
            public static final String INVALID_ORDER_ID = "04";
            public static final String TRANSACTION_NOT_FOUND = "91";
        }
    }
    
    /**
     * Bank Transfer Configuration
     */
    public static final class BankConfig {
        public static final String[] SUPPORTED_BANKS = {
            "Vietcombank", "Techcombank", "MB Bank", "ACB", "Sacombank",
            "VietinBank", "BIDV", "Agribank", "DongA Bank", "TPBank",
            "TVB", "HSBC", "Citibank", "Standard Chartered"
        };
    }
}
