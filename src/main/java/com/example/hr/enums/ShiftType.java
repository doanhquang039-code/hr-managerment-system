package com.example.hr.enums;

public enum ShiftType {
    REGULAR("Ca thường"),
    NIGHT("Ca đêm"),
    WEEKEND("Ca cuối tuần"),
    HOLIDAY("Ca lễ/Tết"),
    SPLIT("Ca chia đôi");

    private final String displayName;

    ShiftType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
