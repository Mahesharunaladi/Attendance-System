package com.waste.management.entity;

/**
 * Enum for worker roles in waste management system
 */
public enum WorkerRole {
    CLEANER("Cleaner"),
    DRIVER("Driver"),
    HELPER("Helper"),
    SUPERVISOR("Supervisor"),
    MANAGER("Manager");

    private final String displayName;

    WorkerRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
