package com.example.hr.enums;

public enum UserStatus {
    ACTIVE("Đang làm việc"),
    INACTIVE("Đã nghỉ việc"),
    ON_LEAVE("Đang nghỉ phép"),
    APPLICANT("Đang ứng tuyển");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}