package com.example.hr.enums;

public enum ExpenseStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    PAID("Đã thanh toán"),
    CANCELED("Đã hủy");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
