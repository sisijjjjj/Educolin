package com.example.educoline.controller;

import com.example.educoline.entity.EmailRequest;
import com.example.educoline.entity.Etudiant;
import com.example.educoline.service.EtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/etudiants", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:4200")
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final JavaMailSender mailSender;

    @Autowired
    public EtudiantController(EtudiantService etudiantService, JavaMailSender mailSender) {
        this.etudiantService = etudiantService;
        this.mailSender = mailSender;
    }

    // Récupérer tous les étudiants
    @GetMapping
    public ResponseEntity<List<Etudiant>> getAllEtudiants() {
        List<Etudiant> etudiants = etudiantService.getAllEtudiants();
        return ResponseEntity.ok(etudiants);
    }

    // Récupérer un étudiant par ID
    @GetMapping("/{id}")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable Long id) {
        return etudiantService.getEtudiantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ajouter un nouvel étudiant
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Etudiant> addEtudiant(@RequestBody Etudiant etudiant) {
        Etudiant created = etudiantService.addEtudiant(etudiant);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Mettre à jour un étudiant
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Etudiant> updateEtudiant(
            @PathVariable Long id,
            @RequestBody Etudiant etudiant) {
        Etudiant updated = etudiantService.updateEtudiant(id, etudiant);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer un étudiant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable Long id) {
        boolean deleted = etudiantService.deleteEtudiant(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Récupérer les étudiants d'une classe
    @GetMapping("/classe/{idClasse}")
    public ResponseEntity<List<Etudiant>> getEtudiantsByClasse(@PathVariable Long idClasse) {
        List<Etudiant> etudiants = etudiantService.getEtudiantsByClasse(idClasse);
        return ResponseEntity.ok(etudiants);
    }



    // Envoyer un email à un enseignant (GET — à éviter en production)
    @GetMapping("/{etudiantId}/send-email")
    public ResponseEntity<String> sendEmailToTeacherGet(
            @PathVariable Long etudiantId,
            @RequestParam String teacherEmail,
            @RequestParam(required = false) String subject,
            @RequestParam String message) {
        return sendEmail(etudiantId, teacherEmail, subject, message);
    }

    // Méthode privée pour l’envoi d’email
    private ResponseEntity<String> sendEmail(Long etudiantId, String teacherEmail, String subject, String message) {
        Optional<Etudiant> opt = etudiantService.getEtudiantById(etudiantId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Étudiant non trouvé pour id=" + etudiantId);
        }

        Etudiant etu = opt.get();

        try {
            SimpleMailMessage msg = new SimpleMailMessage();

            msg.setFrom("sirinemimouni97@gmail.com");
            msg.setTo(teacherEmail);

            String emailSubject = (subject != null && !subject.isBlank())
                    ? subject
                    : "Question de " + etu.getNom() + " " + etu.getPrenom();

            String texteComplet = "De la part de : " + etu.getEmail() + "\n\n" + message;

            msg.setSubject(emailSubject);
            msg.setText(texteComplet);

            mailSender.send(msg);

            return ResponseEntity.ok("Email envoyé avec succès");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
    // Récupérer l'email d'un étudiant par son ID
    // Récupérer l'email d'un étudiant par son ID
    @GetMapping("/{id}/email")
    public ResponseEntity<String> getEtudiantEmail(@PathVariable Long id) {
        Optional<Etudiant> opt = etudiantService.getEtudiantById(id);
        if (opt.isPresent()) {
            Etudiant etudiant = opt.get();
            return ResponseEntity.ok(etudiant.getEmail());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Étudiant avec l'ID " + id + " non trouvé.");
        }
    }
    @PostMapping(value = "/{etudiantId}/send-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendEmailToTeacherPost(
            @PathVariable Long etudiantId,
            @RequestBody EmailRequest emailRequest) {
        return sendEmail(etudiantId, emailRequest.getTeacherEmail(), emailRequest.getSubject(), emailRequest.getMessage());
    }


}
