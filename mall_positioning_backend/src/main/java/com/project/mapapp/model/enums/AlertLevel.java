package com.project.mapapp.model.enums;

public enum AlertLevel {
    HIGH("HIGH", "高"),
    MEDIUM("MEDIUM", "中"),
    LOW("LOW", "低");

    private final String code;
    private final String text;

    AlertLevel(String code, String text) {
        this.code = code;
        this.text = text;
    }

    // Getters
    public String getCode() { return code; }
    public String getText() { return text; }
}
