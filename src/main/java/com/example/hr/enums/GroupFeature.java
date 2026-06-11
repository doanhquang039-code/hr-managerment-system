package com.example.hr.enums;

import java.util.*;

public class GroupFeature {
    public static final GroupFeature DASHBOARD = new GroupFeature("DASHBOARD", "Group dashboard");
    public static final GroupFeature MEMBERS = new GroupFeature("MEMBERS", "Member directory");
    public static final GroupFeature NOTES = new GroupFeature("NOTES", "Group notes");
    public static final GroupFeature TASKS = new GroupFeature("TASKS", "Group tasks");
    public static final GroupFeature FILES = new GroupFeature("FILES", "Documents");
    public static final GroupFeature MEETINGS = new GroupFeature("MEETINGS", "Meetings");
    public static final GroupFeature ANNOUNCEMENTS = new GroupFeature("ANNOUNCEMENTS", "Announcements");
    public static final GroupFeature RECOGNITION = new GroupFeature("RECOGNITION", "Recognition");

    private static final Map<String, GroupFeature> registry = new LinkedHashMap<>();

    static {
        register(DASHBOARD);
        register(MEMBERS);
        register(NOTES);
        register(TASKS);
        register(FILES);
        register(MEETINGS);
        register(ANNOUNCEMENTS);
        register(RECOGNITION);
    }

    private final String name;
    private final String displayName;

    public GroupFeature(String name, String displayName) {
        this.name = name.toUpperCase();
        this.displayName = displayName;
    }

    public static void register(GroupFeature feature) {
        registry.put(feature.getName(), feature);
    }

    public static void register(String name, String displayName) {
        registry.put(name.toUpperCase(), new GroupFeature(name, displayName));
    }

    public String getName() {
        return name;
    }

    public String name() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GroupFeature[] values() {
        return registry.values().toArray(new GroupFeature[0]);
    }

    public static GroupFeature valueOf(String name) {
        if (name == null) return null;
        String cleanName = name.trim().toUpperCase();
        GroupFeature feat = registry.get(cleanName);
        if (feat == null) {
            feat = new GroupFeature(cleanName, cleanName);
            register(feat);
        }
        return feat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupFeature)) return false;
        GroupFeature that = (GroupFeature) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
