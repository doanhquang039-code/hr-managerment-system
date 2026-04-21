package com.example.hr.enums;

public enum KpiStatus {
    ACTIVE("Đang thực hiện"),
    COMPLETED("Hoàn thành"),
    FAILED("Không đạt"),
    CANCELED("Đã hủy"),
    DRAFT("Nháp");

    private final String displayName;

    KpiStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
