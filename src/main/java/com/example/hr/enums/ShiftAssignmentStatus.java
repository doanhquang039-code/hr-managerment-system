package com.example.hr.enums;

public enum ShiftAssignmentStatus {
    SCHEDULED("Đã lên lịch"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Hoàn thành"),
    ABSENT("Vắng mặt"),
    SWAPPED("Đã đổi ca"),
    CANCELED("Đã hủy");

    private final String displayName;

    ShiftAssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
