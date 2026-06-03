package com.ifsp.userservice.dto;

import java.util.UUID;

public class EmailDto {
    private String emailTo;
    private String subject;
    private String text;
    private UUID userId;

    public EmailDto() {}

    public EmailDto(String emailTo, String subject, String text, UUID userId) {
        this.emailTo = emailTo;
        this.subject = subject;
        this.text = text;
        this.userId = userId;
    }

    public String getEmailTo() { return emailTo; }
    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}