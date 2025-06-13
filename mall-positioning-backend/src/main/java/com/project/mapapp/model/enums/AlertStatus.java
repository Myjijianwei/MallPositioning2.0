package com.project.mapapp.model.enums;

/**
 * 警报状态枚举
 */
public enum AlertStatus {
    UNRESOLVED("UNRESOLVED", "未解决", "red"),
    RESOLVED("RESOLVED", "已解决", "green"),
    IGNORED("IGNORED", "已忽略", "orange"),
    DELETED("DELETED", "已删除", "gray");

    private final String code;
    private final String text;
    private final String color;

    AlertStatus(String code, String text, String color) {
        this.code = code;
        this.text = text;
        this.color = color;
    }

    // Getters
    public String getCode() { return code; }
    public String getText() { return text; }
    public String getColor() { return color; }
}

