package com.project.mapapp.model.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EmailTask {
    private String to;
    private String subject;
    private String body;
    private EmailType emailType;

    public enum EmailType {
        VERIFICATION_CODE, DEVICE_INFO
    }

    public EmailTask(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public EmailTask(String to, String subject, String body, EmailType emailType) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.emailType = emailType;
    }


}
