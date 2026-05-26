package com.example.hr.enums;

public enum GroupFeature {
    DASHBOARD("Group dashboard"),
    MEMBERS("Member directory"),
    NOTES("Group notes");

    private final String displayName;

    GroupFeature(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
