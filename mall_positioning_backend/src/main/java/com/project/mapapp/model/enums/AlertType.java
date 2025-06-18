package com.project.mapapp.model.enums;

public enum AlertType {
    GEO_FENCE("GEO_FENCE", "围栏报警"),
    DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线"),
    BATTERY_LOW("BATTERY_LOW", "低电量"),
    SOS("SOS", "紧急求助");

    private final String code;
    private final String text;

    AlertType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    // Getters
    public String getCode() { return code; }
    public String getText() { return text; }
}