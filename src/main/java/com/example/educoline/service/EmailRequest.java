package com.example.educoline.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailRequest {  // Supprimé 'static' partout

    private static JavaMailSender mailSender = null;

    // Injection par constructeur
    public EmailRequest(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public static void sendMeetingNotification(String toEmail,
                                               String teacherName,
                                               String meetingSubject,
                                               String meetingDate,
                                               String meetingLocation,
                                               String customMessage) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Notification de réunion: " + meetingSubject);

        String text = String.format(
                "Bonjour %s,\n\n%s\n\n" +
                        "Détails de la réunion:\n" +
                        "Sujet: %s\n" +
                        "Date: %s\n" +
                        "Lieu: %s\n\n" +
                        "Cordialement,\n" +
                        "L'équipe pédagogique",
                teacherName,
                customMessage != null ? customMessage : "Une nouvelle réunion a été planifiée pour vous",
                meetingSubject,
                meetingDate,
                meetingLocation
        );

        message.setText(text);
        mailSender.send(message);
    }

    public static void sendMeetingNotification(String email, String s, String sujet, String string, String lieu) {
    }

    public static void sendCongeApprovalNotification(String email, String s, String type, String string, String string1) {
    }

    public static void sendCongeRejectionNotification(String email, String s, String type, String string, String string1, String s1) {
    }
}