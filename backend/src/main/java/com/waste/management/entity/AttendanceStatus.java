package com.waste.management.entity;

/**
 * Enum for attendance status
 */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    EARLY_LEAVE("Early Leave"),
    ON_DUTY("On Duty");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
