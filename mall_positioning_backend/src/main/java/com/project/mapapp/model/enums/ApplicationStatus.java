package com.project.mapapp.model.enums;


public enum ApplicationStatus {
    PENDING_CONFIRMATION("PENDING_CONFIRMATION", "待确认"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String description;

    ApplicationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
