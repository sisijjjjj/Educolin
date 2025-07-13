package com.example.educoline.controller;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import com.example.educoline.service.CoursService;
import com.example.educoline.service.EtudiantService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@CrossOrigin(origins = "http://localhost:4200")
public class CoursController {

    private final CoursService coursService;
    private final EtudiantService etudiantService;
    private final NoteRepository noteRepository;
    private static final String UPLOAD_DIR = "uploads/";

    public CoursController(CoursService coursService,
                           EtudiantService etudiantService,
                           NoteRepository noteRepository) {
        this.coursService = coursService;
        this.etudiantService = etudiantService;
        this.noteRepository = noteRepository;

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du répertoire upload: " + e.getMessage());
        }
    }

    public static class CoursRequest {
        private String nom;
        private String description;
        private String niveau;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime heureDebut;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime heureFin;
        private Long enseignantId;
        private Long classeId;

        // Getters et Setters
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
        public LocalTime getHeureDebut() { return heureDebut; }
        public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
        public LocalTime getHeureFin() { return heureFin; }
        public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
        public Long getEnseignantId() { return enseignantId; }
        public void setEnseignantId(Long enseignantId) { this.enseignantId = enseignantId; }
        public Long getClasseId() { return classeId; }
        public void setClasseId(Long classeId) { this.classeId = classeId; }
    }

    @GetMapping
    public ResponseEntity<List<Cours>> getAllCours() {
        return ResponseEntity.ok(coursService.getAllCours());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cours> getCoursById(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getCoursById(id));
    }

    @PostMapping
    public ResponseEntity<?> createCours(@Valid @RequestBody CoursRequest coursRequest) {
        try {
            // Création du cours
            Cours cours = coursService.createCours(
                    coursRequest.getNom(),
                    coursRequest.getDescription(),
                    coursRequest.getNiveau(),
                    coursRequest.getHeureDebut(),
                    coursRequest.getHeureFin(),
                    coursRequest.getEnseignantId(),
                    coursRequest.getClasseId()
            );

            // Ajout des étudiants et création des notes
            etudiantService.addCoursToClassStudents(cours);

            return ResponseEntity.status(HttpStatus.CREATED).body(cours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création du cours: " + e.getMessage());
        }
    }

    @PostMapping("/enseignants/{enseignantId}/cours")
    public ResponseEntity<?> addCoursToEnseignant(
            @PathVariable Long enseignantId,
            @Valid @RequestBody CoursRequest coursRequest) {
        try {
            coursRequest.setEnseignantId(enseignantId);
            return createCours(coursRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création du cours: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cours> updateCours(
            @PathVariable Long id,
            @Valid @RequestBody Cours coursDetails) {
        Cours cours = coursService.getCoursById(id);
        cours.setNom(coursDetails.getNom());
        cours.setDescription(coursDetails.getDescription());
        cours.setHeureDebut(coursDetails.getHeureDebut());
        cours.setHeureFin(coursDetails.getHeureFin());
        cours.setNiveau(coursDetails.getNiveau());
        cours.setEmploi(coursDetails.getEmploi());
        return ResponseEntity.ok(coursService.saveCours(cours));
    }


    @GetMapping("/enseignants/{enseignantId}/cours")
    public ResponseEntity<List<Cours>> getCoursByEnseignant(
            @PathVariable Long enseignantId) {
        return ResponseEntity.ok(coursService.getCoursByEnseignantId(enseignantId));
    }

    @GetMapping("/etudiants/{etudiantId}/cours")
    public ResponseEntity<List<Cours>> getCoursByEtudiant(
            @PathVariable Long etudiantId) {
        return ResponseEntity.ok(coursService.getCoursByEtudiantId(etudiantId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCours(@PathVariable Long id) {
        coursService.deleteCoursWithNotes(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{coursId}/upload")
    public ResponseEntity<?> uploadEmploiTemps(
            @PathVariable Long coursId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Veuillez sélectionner un fichier");
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("Seuls les fichiers PDF sont acceptés");
            }

            String fileName = "emploi_" + coursId + "_" + System.currentTimeMillis() + ".pdf";
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path);

            Cours cours = coursService.getCoursById(coursId);
            cours.setEmploi(fileName);
            coursService.saveCours(cours);

            return ResponseEntity.ok(Map.of(
                    "message", "Fichier uploadé avec succès",
                    "fileName", fileName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/{coursId}/emploi")
    public ResponseEntity<?> downloadEmploiTemps(@PathVariable Long coursId) {
        try {
            Cours cours = coursService.getCoursById(coursId);

            if (cours.getEmploi() == null || cours.getEmploi().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(UPLOAD_DIR + cours.getEmploi());
            byte[] fileContent = Files.readAllBytes(path);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + cours.getEmploi() + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la lecture du fichier");
        }
    }

    @PostMapping("/{coursId}/etudiants/{etudiantId}")
    public ResponseEntity<?> addEtudiantToCours(
            @PathVariable Long coursId,
            @PathVariable Long etudiantId) {
        try {
            coursService.addEtudiantToCours(coursId, etudiantId);
            return ResponseEntity.ok().body("Étudiant ajouté au cours avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

}