package com.example.hr.enums;

public enum PaymentTransactionStatus {
    PENDING("Chờ xử lý"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Đã thanh toán"),
    FAILED("Thất bại"),
    CANCELLED("Đã hủy");

    private final String displayName;

    PaymentTransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
