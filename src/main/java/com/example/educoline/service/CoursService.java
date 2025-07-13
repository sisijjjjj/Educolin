package com.example.educoline.service;

import com.example.educoline.ResourceNotFoundException;
import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;

@Service
public class CoursService {

    private static final Logger logger = LoggerFactory.getLogger(CoursService.class);

    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;
    private final ClasseRepository classeRepository;
    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private String uploadDir;

    @Autowired
    public CoursService(CoursRepository coursRepository,
                        EnseignantRepository enseignantRepository,
                        ClasseRepository classeRepository,
                        EtudiantRepository etudiantRepository,
                        NoteRepository noteRepository,
                        @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.coursRepository = coursRepository;
        this.enseignantRepository = enseignantRepository;
        this.classeRepository = classeRepository;
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.uploadDir = uploadDir;
    }

    @PostConstruct
    public void init() {
        try {
            this.uploadDir = this.uploadDir.endsWith("/") ? this.uploadDir : this.uploadDir + "/";
            Path uploadPath = Paths.get(this.uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Répertoire de téléchargement créé avec succès: {}", uploadPath.toAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Échec de l'initialisation du répertoire de téléchargement: {}", this.uploadDir, e);
            throw new IllegalStateException("Impossible d'initialiser le répertoire de téléchargement: " + this.uploadDir, e);
        }
    }
    public void deleteCoursWithNotes(Long coursId) {
        // Vérifier d'abord si le cours existe
        coursRepository.findById(coursId).orElseThrow(() ->
                new EntityNotFoundException("Cours non trouvé avec l'ID: " + coursId));

        // Supprimer d'abord les notes associées
        noteRepository.deleteByCoursId(coursId);

        // Puis supprimer le cours
        coursRepository.deleteById(coursId);
    }

    @Transactional
    public Cours createCours(String nom, String description, String niveau,
                             LocalTime heureDebut, LocalTime heureFin,
                             Long enseignantId, Long classeId) {
        return createCoursWithStudentsAndNotes(nom, description, niveau,
                heureDebut, heureFin,
                enseignantId, classeId);
    }

    @Transactional
    public Cours createCoursWithStudentsAndNotes(String nom, String description, String niveau,
                                                 LocalTime heureDebut, LocalTime heureFin,
                                                 Long enseignantId, Long classeId) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du cours ne peut pas être vide");
        }
        if (enseignantId == null || classeId == null) {
            throw new IllegalArgumentException("L'enseignant et la classe doivent être spécifiés");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId));

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new EntityNotFoundException("Classe non trouvée avec l'ID: " + classeId));

        Cours cours = new Cours();
        cours.setNom(nom);
        cours.setDescription(description);
        cours.setNiveau(niveau);
        cours.setHeureDebut(heureDebut);
        cours.setHeureFin(heureFin);
        cours.setEnseignant(enseignant);
        cours.setClasse(classe);
        cours.setEtudiants(new HashSet<>());

        Cours savedCours = coursRepository.save(cours);

        List<Etudiant> etudiants = etudiantRepository.findByClasseId(classeId);
        if (etudiants.isEmpty()) {
            logger.warn("Aucun étudiant trouvé pour la classe ID: {}", classeId);
        }

        Set<Note> notes = new HashSet<>();
        for (Etudiant etudiant : etudiants) {
            savedCours.getEtudiants().add(etudiant);
            etudiant.getCours().add(savedCours);

            Note note = new Note();
            note.setTp(0.0);
            note.setExam(0.0);
            note.setMoyenne(0.0);
            note.setAbsences(0);
            note.setCours(savedCours);
            note.setEtudiant(etudiant);
            note.setEnseignant(enseignant);
            notes.add(note);
        }

        try {
            etudiantRepository.saveAll(etudiants);
            noteRepository.saveAll(notes);
            return coursRepository.save(savedCours);
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement du cours avec les étudiants", e);
            throw new RuntimeException("Erreur lors de la création du cours avec les étudiants", e);
        }
    }

    public List<Cours> getAllCours() {
        return coursRepository.findAll();
    }

    public Cours getCoursById(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + id));
    }

    public Cours saveCours(Cours cours) {
        return coursRepository.save(cours);
    }



    public List<Cours> getCoursByEnseignantId(Long enseignantId) {
        return coursRepository.findByEnseignantId(enseignantId);
    }

    public List<Cours> getCoursByEtudiantId(Long etudiantId) {
        return coursRepository.findByEtudiantsId(etudiantId);
    }

    public void addEtudiantToCours(Long coursId, Long etudiantId) {
    }

    public Cours getCours(Long coursId) {
        return null;
    }

    public static class CoursDto {
        private Long id;
        private String nom;
        private String description;
        private String niveau;
        private LocalTime heureDebut;
        private LocalTime heureFin;
        private Long enseignantId;
        private Long classeId;

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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
    @Transactional
    public void deleteCours(Long id) {
        // First delete all related notes
        noteRepository.deleteByCoursId(id);

        // Then delete the course
        coursRepository.deleteById(id);
    }

}