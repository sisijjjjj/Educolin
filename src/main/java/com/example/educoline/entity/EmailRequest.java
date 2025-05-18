package com.example.educoline.entity;

public class EmailRequest {
    private String teacherEmail;
    private String message;
    private String subject;

    // Constructeurs
    public EmailRequest() {
    }

    public EmailRequest(String teacherEmail, String message, String subject) {
        this.teacherEmail = teacherEmail;
        this.message = message;
        this.subject = subject;
    }

    // Getters et Setters
    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}