package com.example.hr.enums;

public enum PaymentMethod {
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    CASH("Tiền mặt"),
    CHECK("Séc");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
