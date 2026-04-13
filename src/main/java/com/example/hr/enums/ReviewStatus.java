package com.example.hr.enums;

public enum ReviewStatus {
    DRAFT("Bản nháp"),
    SUBMITTED("Đã gửi"),
    APPROVED("Đã duyệt");

    private final String label;

    ReviewStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
