package com.example.hr.enums;

public enum SkillLevel {
    BEGINNER("Mới bắt đầu"),
    ELEMENTARY("Cơ bản"),
    INTERMEDIATE("Trung cấp"),
    ADVANCED("Nâng cao"),
    EXPERT("Chuyên gia");

    private final String displayName;

    SkillLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
