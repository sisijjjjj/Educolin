package com.example.educoline.controller;

import com.example.educoline.entity.*;
import com.example.educoline.service.EtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api/etudiants", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:4200/")
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final JavaMailSender mailSender;

    @Autowired
    public EtudiantController(EtudiantService etudiantService, JavaMailSender mailSender) {
        this.etudiantService = etudiantService;
        this.mailSender = mailSender;
    }

    @PostMapping
    public ResponseEntity<Etudiant> createEtudiant(@RequestBody Etudiant etudiant) {
        try {
            Etudiant savedEtudiant = etudiantService.createEtudiant(etudiant);
            return new ResponseEntity<>(savedEtudiant, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Etudiant>> getAllEtudiants() {
        List<Etudiant> etudiants = etudiantService.getAllEtudiants();
        return ResponseEntity.ok(etudiants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        if (etudiant.isPresent()) {
            return ResponseEntity.ok(etudiant.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{etudiantId}/send-email")
    public ResponseEntity<String> sendEmailToTeacherGet(
            @PathVariable Long etudiantId,
            @RequestParam String teacherEmail,
            @RequestParam(required = false) String subject,
            @RequestParam String message) {
        return sendEmail(etudiantId, teacherEmail, subject, message);
    }

    @PostMapping(value = "/{etudiantId}/send-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendEmailToTeacherPost(
            @PathVariable Long etudiantId,
            @RequestBody EmailRequest emailRequest) {
        return sendEmail(etudiantId, emailRequest.getTeacherEmail(), emailRequest.getSubject(), emailRequest.getMessage());
    }

    private ResponseEntity<String> sendEmail(Long etudiantId, String teacherEmail, String subject, String message) {
        Optional<Etudiant> opt = etudiantService.getEtudiantById(etudiantId);
        if (!opt.isPresent()) {
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

    @GetMapping("/{id}/email")
    public ResponseEntity<String> getEtudiantEmail(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        if (etudiant.isPresent()) {
            return ResponseEntity.ok(etudiant.get().getEmail());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/cours")
    public ResponseEntity<Set<Cours>> getCoursByEtudiant(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        if (etudiant.isPresent()) {
            return ResponseEntity.ok(etudiant.get().getCours());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<ArrayList<Note>> getNotesByEtudiant(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        if (etudiant.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(etudiant.get().getNotes()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/absences")
    public HttpEntity<List<Absence>> getAbsencesByEtudiant(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        if (etudiant.isPresent()) {
            return ResponseEntity.ok(etudiant.get().getAbsences());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all/cours")
    public ResponseEntity<List<Cours>> getAllCoursForAllEtudiants() {
        List<Cours> allCours = etudiantService.getAllCoursForAllEtudiants();
        return ResponseEntity.ok(allCours);
    }
}