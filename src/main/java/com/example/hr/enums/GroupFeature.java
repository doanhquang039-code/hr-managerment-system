package com.example.hr.enums;

public enum GroupFeature {
    DASHBOARD("Group dashboard"),
    MEMBERS("Member directory"),
    NOTES("Group notes"),
    TASKS("Group tasks"),
    FILES("Documents"),
    MEETINGS("Meetings"),
    ANNOUNCEMENTS("Announcements"),
    RECOGNITION("Recognition");

    private final String displayName;

    GroupFeature(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
