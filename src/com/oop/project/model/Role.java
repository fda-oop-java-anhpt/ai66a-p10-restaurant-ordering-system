package com.oop.project.model;

public enum Role {
    MANAGER,
    STAFF;

    // Parse từ DB
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }

    // Helper methods 
    public boolean isManager() {
        return this == MANAGER;
    }

    public boolean isStaff() {
        return this == STAFF;
    }
}