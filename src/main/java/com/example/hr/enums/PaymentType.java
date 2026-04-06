package com.example.hr.enums;

public enum PaymentType {
    SALARY("Lương tháng"),
    BONUS("Thưởng tháng"),
    REWARD("Thưởng nhiệm vụ"),
    ADVANCE("Tạm ứng");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
