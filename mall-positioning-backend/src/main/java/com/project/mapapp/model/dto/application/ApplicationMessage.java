package com.project.mapapp.model.dto.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApplicationMessage implements Serializable {

    private Long applicationId;
    private String guardianId;
    private String wardDeviceId;
    private String status;// PENDING, APPROVED, REJECTED
    private String timestamp;
}
