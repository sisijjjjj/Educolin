package com.example.educoline.service;

import com.example.educoline.entity.Conge;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailRequest {

    private static JavaMailSender mailSender = null;

    public EmailRequest(JavaMailSender mailSender) {
        EmailRequest.mailSender = mailSender;
    }

    // Méthode générique pour envoyer un email
    public static void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@educoline.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public static void sendCongeCancellation(@Email(message = "L'email doit être valide") String email, String annulationDeVotreCongé, String s) {
    }

    public static void sendCongeRejection(@Email(message = "L'email doit être valide") String email, String s, String s1) {
    }

    public static void sendCongeApproval(@Email(message = "L'email doit être valide") String email, String approvationDeVotreCongé, String s) {
    }

    public static void sendNotification(@Email(message = "L'email doit être valide") String email, String nouvelÉtudiantDansVotreClasse, String message) {
    }

    // Méthodes spécifiques pour les congés
    public void sendCongeApprovalNotification(String email, String fullName,
                                              Conge.TypeConge leaveType,
                                              String startDate, String endDate) {
        String subject = "Votre demande de congé a été approuvée";
        String content = String.format(
                "Bonjour %s,\n\n" +
                        "Votre demande de congé a été approuvée.\n\n" +
                        "Détails :\n" +
                        "Type: %s\n" +
                        "Période: du %s au %s\n\n" +
                        "Cordialement,\nL'équipe ÉduColine",
                fullName,
                leaveType.toString(),
                startDate,
                endDate
        );
        sendEmail(email, subject, content);
    }

    public void sendCongeRejectionNotification(String email, String fullName,
                                               Conge.TypeConge leaveType,
                                               String startDate, String endDate,
                                               String reason) {
        String subject = "Votre demande de congé a été refusée";
        String content = String.format(
                "Bonjour %s,\n\n" +
                        "Votre demande de congé a été refusée.\n\n" +
                        "Détails :\n" +
                        "Type: %s\n" +
                        "Période: du %s au %s\n" +
                        "Raison: %s\n\n" +
                        "Cordialement,\nL'équipe ÉduColine",
                fullName,
                leaveType.toString(),
                startDate,
                endDate,
                reason != null ? reason : "Non spécifiée"
        );
        sendEmail(email, subject, content);
    }

    // Méthodes pour les réunions
    public void sendMeetingConfirmation(String email, String participantName,
                                        String meetingSubject, String meetingDate,
                                        String meetingLocation, String additionalMessage) {
        String subject = "Confirmation de réunion: " + meetingSubject;
        String content = String.format(
                "Bonjour %s,\n\n" +
                        "%s\n\n" +
                        "Détails de la réunion:\n" +
                        "Sujet: %s\n" +
                        "Date: %s\n" +
                        "Lieu: %s\n\n" +
                        "Cordialement,\nL'équipe ÉduColine",
                participantName,
                additionalMessage != null ? additionalMessage : "Vous êtes invité à une réunion",
                meetingSubject,
                meetingDate,
                meetingLocation
        );
        sendEmail(email, subject, content);
    }

    // Méthode pour les alertes d'absence
    public void sendAbsenceAlert(String email, String studentName,
                                 String courseName, int absenceCount) {
        String subject = "Alerte d'absence pour " + studentName;
        String content = String.format(
                "Bonjour,\n\n" +
                        "L'étudiant %s a %d absence(s) non justifiée(s) dans le cours %s.\n\n" +
                        "Veuillez prendre les mesures appropriées.\n\n" +
                        "Cordialement,\nLe système de gestion des absences",
                studentName,
                absenceCount,
                courseName
        );
        sendEmail(email, subject, content);
    }

    public void sendCongeRejection(@Email(message = "L'email doit être valide") String email, String s, Conge.TypeConge type, String string, String string1, String s1) {
    }

    public void sendCongeApproval(@Email(message = "L'email doit être valide") String email, String s, Conge.TypeConge type, String string, String string1) {

    }

    public void envoyerNotificationAnnulationConge(Conge conge, String motifAnnulation) {
    }

    public void envoyerNotificationApprobationConge(Conge updatedConge) {

    }

    public void envoyerEmail(@Email(message = "L'email doit être valide") String email, String sujet, String contenu) {

    }

    public void sendWelcomeEmail(@Email(message = "L'email doit être valide") String email, String bienvenueSurNotrePlateforme, @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères") String s) {
    }
}