package com.example.educoline.controller;

import com.example.educoline.ResourceNotFoundException;
import com.example.educoline.entity.*;
import com.example.educoline.repository.ClasseRepository;
import com.example.educoline.repository.CongeRepository;
import com.example.educoline.repository.EnseignantRepository;
import com.example.educoline.service.*;
import com.example.educoline.service.EmailRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200/")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final EtudiantService etudiantService;
    private final EnseignantService enseignantService;
    private final NoteService noteService;
    private final AbsenceService absenceService;
    private final CoursService coursService;
    private final EmploiService emploiService;
    private final CongeService congeService;
    private final ClasseService classeService;
    private final ReunionService reunionService;
    private final EmailRequest emailRequest;
    private final EnseignantRepository enseignantRepository;
    private final CongeRepository congeRepository;

    public static class ClasseRequest {
        private String nom;
        private String niveau;
        private Set<Long> enseignantIds;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
        public Set<Long> getEnseignantIds() { return enseignantIds; }
        public void setEnseignantIds(Set<Long> enseignantIds) { this.enseignantIds = enseignantIds; }

        public String getName() {
            return "";
        }

        public String getLevel() {
            return "";
        }

        public Double getAverageGrade() {
            return 0.0;
        }

        public Integer getAbsenceCount() {
            return 0;
        }
    }

    public static class EtudiantRequest {
        private String nom;
        private String prenom;
        private String email;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateNaissance;
        private Long classeId;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDate getDateNaissance() { return dateNaissance; }
        public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
        public Long getClasseId() { return classeId; }
        public void setClasseId(Long classeId) { this.classeId = classeId; }
    }

    // ==================== GENERAL ENDPOINTS ====================

    @PostMapping("/enseignants")
    public ResponseEntity<?> createEnseignant(@Valid @RequestBody Enseignant enseignant) {
        try {
            // Vérification que le mot de passe est fourni
            if (enseignant.getPassword() == null || enseignant.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le mot de passe est obligatoire");
            }

            // Initialisation des valeurs par défaut
            if (enseignant.getStatus() == null) {
                enseignant.setStatus(Enseignant.StatusEnseignant.ACTIF);
            }
            if (enseignant.getNbAnneeExperience() == null) {
                enseignant.setNbAnneeExperience(0);
            }
            if (enseignant.getNbClasse() == null) {
                enseignant.setNbClasse(0);
            }
            if (enseignant.getMatieresEnseignees() == null) {
                enseignant.setMatieresEnseignees(new HashSet<>());
            }

            // Vérification de l'unicité de l'email
            if (enseignantService.existsByEmail(enseignant.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Un enseignant avec cet email existe déjà");
            }

            // Sauvegarde sans hashage du mot de passe (NON RECOMMANDÉ)
            Enseignant saved = enseignantService.save(enseignant);

            // Envoi d'email
            EmailRequest.sendEmail(
                    saved.getEmail(),
                    "Bienvenue sur notre plateforme",
                    "Bonjour " + saved.getPrenom() + ",\n\nVotre compte enseignant a été créé avec succès."
            );

            // Masquer le mot de passe dans la réponse
            saved.setPassword(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création: " + e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<Admin> admins = adminService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching admins: " + e.getMessage());
        }
    }

    @GetMapping("/emploi")
    public ResponseEntity<?> getAllEmplois() {
        try {
            List<Emploi> emplois = emploiService.getAllEmplois();
            return ResponseEntity.ok(emplois);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching schedules: " + e.getMessage());
        }
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getAllNotes() {
        try {
            List<Note> notes = noteService.getAllNotes();
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching notes: " + e.getMessage());
        }
    }
    @GetMapping("/absences")
    public ResponseEntity<?> getAllAbsences() {
        try {
            List<Absence> absences = absenceService.getAllAbsences();
            return ResponseEntity.ok(absences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching absences: " + e.getMessage());
        }
    }
    @GetMapping("/cours")
    public ResponseEntity<?> getAllCours() {
        try {
            List<Cours> cours = coursService.getAllCours();
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching courses: " + e.getMessage());
        }
    }

    @GetMapping("/classes")
    public ResponseEntity<?> getAllClasses() {
        try {
            List<Classe> classes = classeService.getAllClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error fetching classes: " + e.getMessage());
        }
    }

    // ==================== ADMIN CRUD ====================
    @PostMapping("/add")
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        try {
            Admin createdAdmin = adminService.createAdmin(admin);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAdmin);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating admin: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid admin ID");
            }
            Admin admin = adminService.getAdminById(id);
            return admin != null ? ResponseEntity.ok(admin) :
                    ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching admin: " + e.getMessage());
        }
    }

    @GetMapping("/conges/en-attente")
    public ResponseEntity<List<Conge>> getCongesEnAttente() {
        List<Conge> conges = congeRepository.findByStatut(Conge.StatutConge.EN_ATTENTE);
        return ResponseEntity.ok(conges);
    }
    @PostMapping("/conges/{congeId}/approve")
    @Transactional
    public ResponseEntity<Conge> approuverConge(@PathVariable Long congeId) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new EntityNotFoundException("Congé non trouvé"));

        conge.setStatut(Conge.StatutConge.APPROUVE);
        Conge updatedConge = congeRepository.save(conge);

        // Mettre à jour le statut de l'enseignant
        Enseignant enseignant = conge.getEnseignant();
        enseignant.setStatus(Enseignant.StatusEnseignant.EN_CONGE); // Correction ici
        enseignantRepository.save(enseignant);

        // Notification
        sendCongeNotification(updatedConge, true, null);

        return ResponseEntity.ok(updatedConge);
    }

    @PostMapping("/conges/{congeId}/reject")
    @Transactional
    public ResponseEntity<Conge> rejeterConge(
            @PathVariable Long congeId,
            @RequestBody(required = false) String motifRejet) {

        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new EntityNotFoundException("Congé non trouvé"));

        conge.setStatut(Conge.StatutConge.REJETE);
        conge.setMotifRejet(motifRejet);
        Conge updatedConge = congeRepository.save(conge);

        // Notification
        sendCongeNotification(updatedConge, false, motifRejet);

        return ResponseEntity.ok(updatedConge);
    }

    private void sendCongeNotification(Conge conge, boolean isApproved, String motifRejet) {
        Enseignant enseignant = conge.getEnseignant();
        String sujet = isApproved ? "Votre congé a été approuvé" : "Votre congé a été rejeté";

        String contenu = "Bonjour " + enseignant.getNom() + ",\n\n" +
                "Votre demande de congé du " + conge.getDateDebut() +
                " au " + conge.getDateFin() + " a été " +
                (isApproved ? "approuvée." : "rejetée.");

        if (!isApproved && motifRejet != null) {
            contenu += "\nMotif du rejet : " + motifRejet;
        }
        emailRequest.envoyerEmail(enseignant.getEmail(), sujet, contenu);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid admin ID");
            }
            Admin updatedAdmin = adminService.updateAdmin(id, admin);
            return ResponseEntity.ok(updatedAdmin);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating admin: " + e.getMessage());
        }
    }
    @PostMapping("/enseignants/{id}/reunions")
    public ResponseEntity<?> ajouterReunionPourEnseignant(
            @PathVariable Long id,
            @RequestBody Reunion reunion,
            @RequestParam(required = false, defaultValue = "true") boolean sendEmail) {

        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID enseignant invalide");
            }

            Enseignant enseignant = enseignantService.getEnseignantById(id)
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

            // Associer l'enseignant et définir l'email automatiquement
            reunion.setEnseignant(enseignant);
            reunion.setEmail(enseignant.getEmail());

            Reunion savedReunion = reunionService.save(reunion);

            // Envoi conditionnel de l'email
            if (sendEmail && enseignant.getEmail() != null) {
                String sujet = "Nouvelle réunion programmée";
                String contenu = String.format(
                        "Bonjour %s %s,\n\n" +
                                "Une nouvelle réunion a été programmée pour vous.\n" +
                                "Sujet: %s\n" +
                                "Date et heure: %s\n" +
                                "Lieu: %s\n\n" +
                                "Cordialement,\nL'administration",
                        enseignant.getPrenom(),
                        enseignant.getNom(),
                        savedReunion.getSujet(),
                        savedReunion.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        savedReunion.getLieu()
                );

                emailRequest.envoyerEmail(
                        enseignant.getEmail(),
                        sujet,
                        contenu
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedReunion);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création de la réunion: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid admin ID");
            }
            adminService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting admin: " + e.getMessage());
        }
    }
    @GetMapping("/etudiants")
    public ResponseEntity<?> getAllEtudiants() {
        try {
            List<Etudiant> etudiants = etudiantService.getAllEtudiants();

            // Initialize all needed associations
            for (Etudiant e : etudiants) {
                Hibernate.initialize(e.getCours());
                if (e.getCours() != null) {
                    e.getCours().forEach(c -> {
                        Hibernate.initialize(c.getMatiere());
                        Hibernate.initialize(c.getEnseignant());
                    });
                }
                Hibernate.initialize(e.getAbsences());
                Hibernate.initialize(e.getNotes());
                Hibernate.initialize(e.getClasse());
                Hibernate.initialize(e.getEnseignant());
            }

            return ResponseEntity.ok(etudiants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching students: " + e.getMessage());
        }
    }

    @GetMapping("/etudiants/{id}")
    public ResponseEntity<?> getEtudiantById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
            return etudiant.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching student: " + e.getMessage());
        }
    }
    @PutMapping(value = "/classes/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateClasse(
            @PathVariable Long id,
            @RequestBody Classe updatedClasse) {
        try {
            // 1. Validation
            if (updatedClasse.getName() == null || updatedClasse.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le name est obligatoire");
            }

            // 2. Vérifier l'existence de la classe
            Optional<Classe> existingClasse = classeService.findById(id);
            if (existingClasse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 3. Mise à jour des champs
            Classe classeToUpdate = existingClasse.get();
            classeToUpdate.setName(updatedClasse.getName().trim());
            classeToUpdate.setNiveau(updatedClasse.getNiveau() != null ?
                    updatedClasse.getNiveau().trim() : "Non spécifié");

            // Gestion des enseignants si nécessaire
            if (updatedClasse.getEnseignants() != null) {
                classeToUpdate.setEnseignants(updatedClasse.getEnseignants());
            }

            // Incrémentation de version
            Long currentVersion = classeToUpdate.getVersion();
            classeToUpdate.setVersion(currentVersion != null ? currentVersion + 1 : 1L);

            // 4. Sauvegarde
            Classe savedClasse = classeService.saveClasse(classeToUpdate);

            return ResponseEntity.ok("Classe mise à jour (ID: " + savedClasse.getId() + ")");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }
    @PutMapping("/etudiants/{id}")
    public ResponseEntity<?> updateEtudiant(@PathVariable Long id, @RequestBody Etudiant etudiant) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            Optional<Etudiant> existingEtudiant = etudiantService.getEtudiantById(id);
            if (existingEtudiant.isPresent()) {
                etudiant.setId(id);
                Etudiant updatedEtudiant = etudiantService.saveEtudiant(etudiant);
                return ResponseEntity.ok(updatedEtudiant);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating student: " + e.getMessage());
        }
    }

    private void notifyEnseignantIfNeeded(Etudiant savedEtudiant) {
        // Vérifier si l'étudiant est dans une classe
        if (savedEtudiant.getClasse() != null) {
            Classe classe = savedEtudiant.getClasse();

            // Récupérer les informations nécessaires
            int nombreAbsences = absenceService.getNombreAbsencesByEtudiantId(savedEtudiant.getId());
            double moyenne = calculerMoyenneEtudiant(savedEtudiant.getId());
            String statut = determinerStatutEtudiant(moyenne, nombreAbsences, classe.getSeuilAbsences(), classe.getSeuilMoyenne());

            // Construire le message de notification
            String message = String.format(
                    "Nouvel étudiant ajouté:\n" +
                            "Nom: %s %s\n" +
                            "Email: %s\n" +
                            "Classe: %s\n" +
                            "Matière assignée: %s\n" +
                            "Nombre d'absences: %d\n" +
                            "Moyenne: %.2f\n" +
                            "Statut: %s",
                    savedEtudiant.getPrenom(), savedEtudiant.getNom(),
                    savedEtudiant.getEmail(),
                    classe.getName(),
                    classe.getMatiere() != null ? classe.getMatiere().getNom() : "Non spécifiée",
                    nombreAbsences,
                    moyenne,
                    statut
            );

            // Envoyer la notification aux enseignants concernés
            List<Enseignant> enseignants = enseignantService.getEnseignantsByClasseId(classe.getId());
            for (Enseignant enseignant : enseignants) {
                EmailRequest.sendNotification(
                        enseignant.getEmail(),
                        "Nouvel étudiant dans votre classe",
                        message
                );
            }
        }
    }

    private double calculerMoyenneEtudiant(Long etudiantId) {
        List<Note> notes = noteService.getNotesByEtudiantId(etudiantId);
        if (notes.isEmpty()) {
            return 0.0;
        }

        double somme = 0.0;
        for (Note note : notes) {
            somme += note.getValeur();
        }

        return somme / notes.size();
    }

    private String determinerStatutEtudiant(double moyenne, int absences, int seuilAbsences, double seuilMoyenne) {
        boolean elimineParAbsences = absences > seuilAbsences;
        boolean elimineParMoyenne = moyenne < seuilMoyenne;

        if (elimineParAbsences && elimineParMoyenne) {
            return "Éliminé (absences et moyenne insuffisante)";
        } else if (elimineParAbsences) {
            return "Éliminé (trop d'absences)";
        } else if (elimineParMoyenne) {
            return "Éliminé (moyenne insuffisante)";
        } else {
            return "Non éliminé";
        }
    }

    @PostMapping("/etudiants/{etudiantId}/cours/{coursId}/notes")
    public ResponseEntity<?> addNoteToEtudiant(
            @PathVariable Long etudiantId,
            @PathVariable Long coursId,
            @RequestBody Note note) {
        try {
            if (etudiantId == null || etudiantId <= 0 || coursId == null || coursId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or course ID");
            }
            Note savedNote = noteService.addNoteToEtudiant(etudiantId, coursId, note);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding note: " + e.getMessage());
        }
    }

    @GetMapping("/etudiants/{etudiantId}/notes")
    public ResponseEntity<?> getNotesByEtudiant(@PathVariable Long etudiantId) {
        try {
            if (etudiantId == null || etudiantId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            List<Note> notes = noteService.getNotesByEtudiantId(etudiantId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notes: " + e.getMessage());
        }
    }


    @GetMapping("/etudiants/{etudiantId}/cours/{coursId}/notes")
    public ResponseEntity<?> getNotesByEtudiantAndCours(
            @PathVariable Long etudiantId,
            @PathVariable Long coursId) {
        try {
            if (etudiantId == null || etudiantId <= 0 || coursId == null || coursId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or course ID");
            }
            Optional<Note> notes = noteService.getNotesByEtudiantAndCours(etudiantId, coursId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notes: " + e.getMessage());
        }
    }

    @PutMapping("/etudiants/{etudiantId}/notes/{noteId}")
    public ResponseEntity<?> updateEtudiantNote(
            @PathVariable Long etudiantId,
            @PathVariable Long noteId,
            @RequestBody Note note) {
        try {
            if (etudiantId == null || etudiantId <= 0 || noteId == null || noteId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or note ID");
            }
            Note updatedNote = noteService.updateEtudiantNote(etudiantId, noteId, note);
            return ResponseEntity.ok(updatedNote);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating note: " + e.getMessage());
        }
    }

    @DeleteMapping("/etudiants/{etudiantId}/notes/{noteId}")
    public ResponseEntity<?> deleteEtudiantNote(
            @PathVariable Long etudiantId,
            @PathVariable Long noteId) {
        try {
            if (etudiantId == null || etudiantId <= 0 || noteId == null || noteId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or note ID");
            }
            noteService.deleteEtudiantNote(etudiantId, noteId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting note: " + e.getMessage());
        }
    }

    // ==================== ETUDIANT ABSENCES ====================
    @PostMapping("/etudiants/{etudiantId}/cours/{coursId}/absences")
    public ResponseEntity<?> addAbsenceToEtudiant(
            @PathVariable Long etudiantId,
            @PathVariable Long coursId,
            @RequestBody Absence absence) {
        try {
            if (etudiantId == null || etudiantId <= 0 || coursId == null || coursId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or course ID");
            }
            Absence savedAbsence = absenceService.addAbsenceToEtudiant(etudiantId, coursId, absence);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAbsence);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding absence: " + e.getMessage());
        }
    }

    @GetMapping("/etudiants/{etudiantId}/absences/justifiees")
    public ResponseEntity<?> getJustifiedAbsencesByEtudiant(@PathVariable Long etudiantId) {
        try {
            if (etudiantId == null || etudiantId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            List<Absence> absences = absenceService.getJustifiedAbsencesByEtudiantId(etudiantId);
            return ResponseEntity.ok(absences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching justified absences: " + e.getMessage());
        }
    }

    @GetMapping("/etudiants/{etudiantId}/absences/non-justifiees")
    public ResponseEntity<?> getUnjustifiedAbsencesByEtudiant(@PathVariable Long etudiantId) {
        try {
            if (etudiantId == null || etudiantId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            List<Absence> absences = absenceService.getUnjustifiedAbsencesByEtudiantId(etudiantId);
            return ResponseEntity.ok(absences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching unjustified absences: " + e.getMessage());
        }
    }

    @PutMapping("/etudiants/{etudiantId}/absences/{absenceId}/justifier")
    public ResponseEntity<?> justifyAbsence(
            @PathVariable Long etudiantId,
            @PathVariable Long absenceId) {
        try {
            if (etudiantId == null || etudiantId <= 0 || absenceId == null || absenceId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or absence ID");
            }
            Absence absence = absenceService.justifyAbsence(etudiantId, absenceId);
            return ResponseEntity.ok(absence);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error justifying absence: " + e.getMessage());
        }
    }

    @DeleteMapping("/etudiants/{etudiantId}/absences/{absenceId}")
    public ResponseEntity<?> deleteEtudiantAbsence(
            @PathVariable Long etudiantId,
            @PathVariable Long absenceId) {
        try {
            if (etudiantId == null || etudiantId <= 0 || absenceId == null || absenceId <= 0) {
                return ResponseEntity.badRequest().body("Invalid student or absence ID");
            }
            absenceService.deleteEtudiantAbsence(etudiantId, absenceId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting absence: " + e.getMessage());
        }
    }

    // ==================== ENSEIGNANT CRUD ====================
    @GetMapping("/enseignants")
    public ResponseEntity<?> getAllEnseignants() {
        try {
            List<Enseignant> enseignants = enseignantService.getAllEnseignants();
            return ResponseEntity.ok(enseignants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching teachers: " + e.getMessage());
        }
    }

    @GetMapping("/enseignants/{id}")
    public ResponseEntity<?> getEnseignantById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid teacher ID");
            }
            Optional<Enseignant> enseignant = enseignantService.getEnseignantById(id);
            return enseignant.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching teacher: " + e.getMessage());
        }
    }

    @PutMapping("/enseignants/{id}")
    public ResponseEntity<?> updateEnseignant(@PathVariable Long id, @RequestBody Enseignant enseignant) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID enseignant invalide");
            }

            Optional<Enseignant> existingOpt = enseignantService.getEnseignantById(id);
            if (!existingOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Enseignant existing = existingOpt.get();

            if (enseignant.getNom() != null) existing.setNom(enseignant.getNom());
            if (enseignant.getPrenom() != null) existing.setPrenom(enseignant.getPrenom());
            if (enseignant.getEmail() != null) existing.setEmail(enseignant.getEmail());
            if (enseignant.getNbClasse() != null) existing.setNbClasse(enseignant.getNbClasse());
            if (enseignant.getNbAnneeExperience() != null) existing.setNbAnneeExperience(enseignant.getNbAnneeExperience());

            Enseignant updated = enseignantService.saveEnseignant(existing);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @DeleteMapping("/enseignants/{id}")
    public ResponseEntity<?> deleteEnseignant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID enseignant invalide");
            }

            if (!enseignantService.enseignantExists(id)) {
                return ResponseEntity.notFound().build();
            }

            enseignantService.deleteEnseignant(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Impossible de supprimer : l'enseignant est lié à d'autres données");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ==================== CLASS ASSIGNMENT ====================


    @PostMapping("/etudiants/{etudiantId}/retirer-classe")
    public ResponseEntity<?> retirerEtudiantDeClasse(@PathVariable Long etudiantId) {
        try {
            if (etudiantId == null || etudiantId <= 0) {
                return ResponseEntity.badRequest().body("ID étudiant invalide");
            }

            Etudiant etudiant = etudiantService.getEtudiantById(etudiantId)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

            etudiant.setClasse(null);
            Etudiant updatedEtudiant = etudiantService.saveEtudiant(etudiant);

            return ResponseEntity.ok(Map.of(
                    "message", "Étudiant retiré de sa classe avec succès",
                    "etudiant", updatedEtudiant
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du retrait de la classe: " + e.getMessage());
        }
    }

    // ==================== OTHER FUNCTIONALITIES ====================
    @GetMapping("/etudiants/{id}/cours")
    public ResponseEntity<?> getCoursForEtudiant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            List<Cours> cours = coursService.getCoursByEtudiantId(id);
            return ResponseEntity.ok(cours);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching courses: " + e.getMessage());
        }
    }

    @GetMapping("/etudiants/{id}/emploi")
    public ResponseEntity<?> getEmploiForEtudiant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid student ID");
            }
            Emploi emploi = emploiService.getEmploiByEtudiantId(id);
            return ResponseEntity.ok(emploi);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching schedule: " + e.getMessage());
        }
    }

    @GetMapping("/enseignants/{id}/conges")
    public ResponseEntity<?> getCongesForEnseignant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid teacher ID");
            }
            List<Conge> conges = congeService.getCongesByEnseignantId(id);
            return ResponseEntity.ok(conges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching leaves: " + e.getMessage());
        }
    }

    @GetMapping("/enseignants/{id}/absences")
    public ResponseEntity<?> getAbsencesForEnseignant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid teacher ID");
            }
            List<Absence> absences = absenceService.getAbsencesByEnseignantId(id);
            return ResponseEntity.ok(absences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching absences: " + e.getMessage());
        }
    }

    @GetMapping("/enseignants/{id}/emploi")
    public ResponseEntity<?> getEmploiForEnseignant(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid teacher ID");
            }
            Emploi emploi = emploiService.getEmploiByEnseignantId(id);
            return ResponseEntity.ok(emploi);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching schedule: " + e.getMessage());
        }
    }

    // ==================== REUNIONS MANAGEMENT ====================


    @PostMapping("/reunions/{reunionId}/send-email")
    public ResponseEntity<?> sendEmailForReunion(
            @PathVariable Long reunionId,
            @RequestParam(required = false) String customMessage) {
        try {
            if (reunionId == null || reunionId <= 0) {
                return ResponseEntity.badRequest().body("Invalid meeting ID");
            }

            Reunion reunion = reunionService.getReunionById(reunionId)
                    .orElseThrow(() -> new RuntimeException("Meeting not found"));

            if (reunion.getEnseignant() == null) {
                return ResponseEntity.badRequest().body("No teacher associated with this meeting");
            }

            Enseignant enseignant = reunion.getEnseignant();
            String message = customMessage != null ? customMessage :
                    "Une réunion a été programmée pour vous. Veuillez trouver les détails ci-dessous.";

            String sujet = "Rappel: Réunion programmée";
            String contenu = String.format(
                    "Bonjour %s %s,\n\n" +
                            "%s\n" +
                            "Sujet: %s\n" +
                            "Date et heure: %s\n" +
                            "Lieu: %s\n\n" +
                            "Cordialement,\nL'administration",
                    enseignant.getPrenom(),
                    enseignant.getNom(),
                    message,
                    reunion.getSujet(),
                    reunion.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    reunion.getLieu()
            );

            emailRequest.envoyerEmail(
                    enseignant.getEmail(),
                    sujet,
                    contenu
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Email successfully sent",
                    "to", enseignant.getEmail(),
                    "subject", sujet
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error sending email: " + e.getMessage());
        }
    }

    @GetMapping("/reunions")
    public ResponseEntity<?> getAllReunions() {
        try {
            List<Reunion> reunions = reunionService.getAllReunions();
            return ResponseEntity.ok(reunions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error fetching meetings: " + e.getMessage());
        }
    }

    // ==================== CONGES MANAGEMENT ====================
    @GetMapping("/conges")
    public ResponseEntity<?> getAllConges() {
        try {
            List<Conge> conges = congeService.getAllConges();
            return ResponseEntity.ok(conges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching leaves: " + e.getMessage());
        }
    }

    @GetMapping("/conges/statut/{statut}")
    public ResponseEntity<?> getCongesByStatut(@PathVariable String statut) {
        try {
            List<Conge> conges = congeService.getCongesByStatut(Conge.StatutConge.valueOf(statut));
            return ResponseEntity.ok(conges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching leaves: " + e.getMessage());
        }
    }


    @PutMapping("/conges/{congeId}/approve")
    public ResponseEntity<?> approveConge(@PathVariable Long congeId) {
        try {
            // 1. Validation de l'ID
            if (congeId == null || congeId <= 0) {
                return ResponseEntity.badRequest().body("ID de congé invalide");
            }

            // 2. Récupération du congé
            Conge conge = congeRepository.findById(congeId)
                    .orElseThrow(() -> new RuntimeException("Congé non trouvé"));

            // 3. Vérification du statut
            if (conge.getStatut() != Conge.StatutConge.EN_ATTENTE) {
                return ResponseEntity.badRequest().body("Seuls les congés en attente peuvent être approuvés");
            }

            // 4. Mise à jour du congé
            conge.setStatut(Conge.StatutConge.APPROUVE);
            Conge updatedConge = congeRepository.save(conge);

            // 5. Mise à jour de l'enseignant
            Enseignant enseignant = updatedConge.getEnseignant();
            if (enseignant != null) {
                enseignant.setStatus(Enseignant.StatusEnseignant.EN_CONGE);
                enseignantRepository.save(enseignant);

                // 6. Envoi de notification
                if (enseignant.getEmail() != null) {
                    String emailContent = buildApprovalEmailContent(enseignant, updatedConge);
                    emailRequest.envoyerEmail(enseignant.getEmail(),
                            "Congé approuvé",
                            emailContent);
                }
            }

            return ResponseEntity.ok(updatedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    private String buildApprovalEmailContent(Enseignant enseignant, Conge conge) {
        return String.format(
                "Bonjour %s %s,%n%n" +
                        "Votre congé a été approuvé:%n" +
                        "- Type: %s%n" +
                        "- Du: %s%n" +
                        "- Au: %s%n%n" +
                        "Cordialement,%nL'administration",
                enseignant.getPrenom(),
                enseignant.getNom(),
                conge.getType(),
                conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }
    @PostMapping("/classes")
    @Transactional
    public ResponseEntity<?> createClasse(@Valid @RequestBody Classe classeRequest) {
        try {
            // 1. Validation et nettoyage du nom
            String nomClasse = Optional.ofNullable(classeRequest.getName())
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Le nom de la classe est obligatoire"));

            // 2. Vérification d'unicité (en ignorant la casse)
            if (classeService.existsByNameIgnoreCase(nomClasse)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Une classe avec ce nom existe déjà");
            }

            // 3. Création et initialisation de la classe
            Classe nouvelleClasse = new Classe();
            nouvelleClasse.setName(nomClasse);
            nouvelleClasse.setNiveau(Optional.ofNullable(classeRequest.getNiveau())
                    .map(String::trim)
                    .filter(niveau -> !niveau.isEmpty())
                    .orElse("Non spécifié"));

            // Valeurs par défaut
            nouvelleClasse.setMoyenneGenerale(0.0);
            nouvelleClasse.setNombreAbsences(0);

            // 4. Gestion des enseignants
            if (classeRequest.getEnseignants() != null && !classeRequest.getEnseignants().isEmpty()) {
                Set<Enseignant> enseignants = new HashSet<>();
                for (Enseignant enseignantRequest : classeRequest.getEnseignants()) {
                    Enseignant e = enseignantService.findById(enseignantRequest.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Enseignant non trouvé: " + enseignantRequest.getId()));
                    enseignants.add(e);
                }
                nouvelleClasse.setEnseignants(enseignants);
            }

            // 5. Sauvegarde avec gestion des relations bidirectionnelles
            Classe classeSauvegardee = classeService.saveWithEnseignants(nouvelleClasse);

            // 6. Construction de la réponse détaillée
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", classeSauvegardee.getId());
            response.put("name", classeSauvegardee.getName());
            response.put("niveau", classeSauvegardee.getNiveau());
            response.put("moyenneGenerale", classeSauvegardee.getMoyenneGenerale());
            response.put("nombreAbsences", classeSauvegardee.getNombreAbsences());

            // Formatage des enseignants avec leurs informations complètes
            response.put("enseignants", classeSauvegardee.getEnseignants().stream()
                    .map(e -> {
                        Map<String, Object> enseignantMap = new HashMap<>();
                        enseignantMap.put("id", e.getId());
                        enseignantMap.put("nom", e.getNom());
                        enseignantMap.put("prenom", e.getPrenom());
                        enseignantMap.put("email", e.getEmail());
                        enseignantMap.put("specialite", e.getSpecialite());
                        return enseignantMap;
                    })
                    .collect(Collectors.toList()));

            // 7. Retour avec Location header
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(classeSauvegardee.getId())
                    .toUri();

            return ResponseEntity.created(location).body(response);

        } catch (ResponseStatusException e) {
            throw e; // Exceptions déjà gérées
        } catch (Exception e) {
            log.error("Erreur lors de la création de la classe", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur serveur lors de la création de la classe");
        }
    }

    @GetMapping("/cours/{id}/notes")
    public ResponseEntity<?> getNotesByCours(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid course ID");
            }
            List<Note> notes = noteService.getNotesByCoursId(id);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notes: " + e.getMessage());
        }
    }


    @PostMapping("/classes/{classeId}/etudiants")
    public ResponseEntity<?> addEtudiantsToClasse(
            @PathVariable Long classeId,
            @RequestBody List<Long> etudiantIds) {
        try {
            if (classeId == null || classeId <= 0) {
                return ResponseEntity.badRequest().body("ID de classe invalide");
            }
            if (etudiantIds == null || etudiantIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Liste d'IDs étudiants vide");
            }

            Classe classe = classeService.getClasseById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

            for (Long etudiantId : etudiantIds) {
                Etudiant etudiant = etudiantService.getEtudiantById(etudiantId)
                        .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID: " + etudiantId));
                etudiant.setClasse(classe);
                etudiantService.saveEtudiant(etudiant);
            }

            return ResponseEntity.ok("Étudiants ajoutés à la classe avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<?> updateClasse(@PathVariable Long id, @RequestBody ClasseRequest request) {
        try {
            // Validation de l'ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID de classe invalide");
            }

            // Vérification de l'existence de la classe
            Optional<Classe> existingClasse = classeService.getClasseById(id);
            if (!existingClasse.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Classe classe = existingClasse.get();

            // Mise à jour du nom si présent dans la requête
            if (request.getNom() != null && !request.getNom().trim().isEmpty()) {
                classe.setName(request.getNom());  // Correction: setNom() au lieu de setName()
            }

            // Mise à jour du niveau si présent dans la requête
            if (request.getNiveau() != null && !request.getNiveau().trim().isEmpty()) {
                classe.setNiveau(request.getNiveau());  // Correction: setNiveau() au lieu de setLevel()
            }

            // Mise à jour des enseignants si présents dans la requête
            if (request.getEnseignantIds() != null) {
                Set<Enseignant> enseignants = new HashSet<>();
                for (Long enseignantId : request.getEnseignantIds()) {
                    Enseignant enseignant = enseignantService.getEnseignantById(enseignantId)
                            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + enseignantId));
                    enseignants.add(enseignant);
                }
                classe.setEnseignants(enseignants);
            }

            // Sauvegarde des modifications
            Classe updatedClasse = classeService.saveClasse(classe);
            return ResponseEntity.ok(updatedClasse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour des enseignants: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur serveur lors de la mise à jour de la classe: " + e.getMessage());
        }
    }

    @GetMapping("/classes/{id}")
    public ResponseEntity<?> getClasseDetails(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID de classe invalide");
            }

            Optional<Classe> classe = classeService.getClasseById(id);
            if (!classe.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(classe.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération de la classe: " + e.getMessage());
        }
    }

    @PostMapping("/classes/{classeId}/enseignants/{enseignantId}")
    public ResponseEntity<?> addEnseignantToClasse(
            @PathVariable Long classeId,
            @PathVariable Long enseignantId) {
        try {
            if (classeId == null || classeId <= 0 || enseignantId == null || enseignantId <= 0) {
                return ResponseEntity.badRequest().body("ID de classe ou enseignant invalide");
            }

            Classe classe = classeService.getClasseById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
            Enseignant enseignant = enseignantService.getEnseignantById(enseignantId)
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

            classe.addEnseignant(enseignant);
            classeService.saveClasse(classe);

            return ResponseEntity.ok().body("Enseignant ajouté à la classe avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout de l'enseignant: " + e.getMessage());
        }
    }

    @DeleteMapping("/classes/{classeId}/enseignants/{enseignantId}")
    public ResponseEntity<?> removeEnseignantFromClasse(
            @PathVariable Long classeId,
            @PathVariable Long enseignantId) {
        try {
            if (classeId == null || classeId <= 0 || enseignantId == null || enseignantId <= 0) {
                return ResponseEntity.badRequest().body("ID de classe ou enseignant invalide");
            }

            Classe classe = classeService.getClasseById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
            Enseignant enseignant = enseignantService.getEnseignantById(enseignantId)
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

            classe.removeEnseignant(enseignant);
            classeService.saveClasse(classe);

            return ResponseEntity.ok().body("Enseignant retiré de la classe avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du retrait de l'enseignant: " + e.getMessage());
        }
    }

    @GetMapping("/classes/{id}/etudiants")
    public ResponseEntity<?> getEtudiantsByClasse(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("ID de classe invalide");
            }

            List<Etudiant> etudiants = etudiantService.getEtudiantsByClasseId(id);
            return ResponseEntity.ok(etudiants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des étudiants: " + e.getMessage());
        }
    }


    // ==================== HELPER METHODS ====================


    private ResponseEntity<?> buildValidationErrorResponse(List<String> errors) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", "error",
                        "message", "Erreurs de validation",
                        "errors", errors
                )

        );
    }
    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<String> deleteEtudiant(@PathVariable Long id) {
        try {
            if (etudiantService.deleteEtudiant(id)) {
                return ResponseEntity.ok("Étudiant marqué comme supprimé");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur: " + e.getMessage());
        }
    }



    // ==================== ERROR HANDLING ====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + e.getMessage());
    }
    @GetMapping("/enseignants/{enseignantId}/reunions")
    public ResponseEntity<List<Reunion>> getReunionsByEnseignant(
            @PathVariable Long enseignantId) {
        List<Reunion> reunions = reunionService.findByEnseignantId(enseignantId);
        return ResponseEntity.ok(reunions);
    }
    @PostMapping("/etudiants")
    public ResponseEntity<?> createEtudiant(@Valid @RequestBody EtudiantRequest etudiantRequest) {
        try {
            // Validation des champs obligatoires
            if (etudiantRequest.getNom() == null || etudiantRequest.getNom().trim().isEmpty() ||
                    etudiantRequest.getPrenom() == null || etudiantRequest.getPrenom().trim().isEmpty() ||
                    etudiantRequest.getEmail() == null || etudiantRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nom, prénom et email sont obligatoires");
            }

            // Vérification de l'unicité de l'email
            if (etudiantService.existsByEmail(etudiantRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Un étudiant avec cet email existe déjà");
            }

            // Création de l'étudiant
            Etudiant etudiant = new Etudiant();
            etudiant.setNom(etudiantRequest.getNom());
            etudiant.setPrenom(etudiantRequest.getPrenom());
            etudiant.setEmail(etudiantRequest.getEmail());
            etudiant.setDateNaissance(etudiantRequest.getDateNaissance());

            // Assignation à la classe si l'ID est fourni
            if (etudiantRequest.getClasseId() != null && etudiantRequest.getClasseId() > 0) {
                Optional<Classe> classe = classeService.getClasseById(etudiantRequest.getClasseId());
                if (classe.isPresent()) {
                    etudiant.setClasse(classe.get());
                } else {
                    return ResponseEntity.badRequest().body("Classe non trouvée avec l'ID: " + etudiantRequest.getClasseId());
                }
            }

            // Sauvegarde de l'étudiant
            Etudiant savedEtudiant = etudiantService.saveEtudiant(etudiant);

            // Notification si assigné à une classe
            if (savedEtudiant.getClasse() != null) {
                notifyEnseignantIfNeeded(savedEtudiant);
            }

            // Construction de la réponse
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedEtudiant.getId())
                    .toUri();

            return ResponseEntity.created(location).body(savedEtudiant);

        } catch (ConstraintViolationException e) {
            return ResponseEntity.badRequest().body("Erreur de validation: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création de l'étudiant: " + e.getMessage());
        }
    }
    @PostMapping("/etudiants/{etudiantId}/classes/{classeId}")
    public ResponseEntity<?> assignerEtudiantAClasse(
            @PathVariable Long etudiantId,
            @PathVariable Long classeId) {

        try {
            // Validation des IDs
            if (etudiantId == null || etudiantId <= 0 || classeId == null || classeId <= 0) {
                return ResponseEntity.badRequest().body("ID étudiant ou classe invalide");
            }

            // Récupération de l'étudiant
            Etudiant etudiant = etudiantService.getEtudiantById(etudiantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

            // Récupération de la classe
            Classe classe = classeService.getClasseById(classeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée"));

            // Assignation
            etudiant.setClasse(classe);
            Etudiant updatedEtudiant = etudiantService.saveEtudiant(etudiant);

            // Notification
            notifyEnseignantIfNeeded(updatedEtudiant);

            return ResponseEntity.ok(updatedEtudiant);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'assignation: " + e.getMessage());
        }
    }
    @GetMapping("/enseignants/{enseignantId}/classes/noms")
    public ResponseEntity<?> getClassesNomsForEnseignant(@PathVariable Long enseignantId) {
        try {
            if (enseignantId == null || enseignantId <= 0) {
                return ResponseEntity.badRequest().body("ID enseignant invalide");
            }

            Optional<Enseignant> enseignant = enseignantService.getEnseignantById(enseignantId);
            if (enseignant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Set<Classe> classes = enseignant.get().getClasses();
            if (classes == null || classes.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "L'enseignant n'est assigné à aucune classe"));
            }

            List<Map<String, Object>> classesInfos = new ArrayList<>();
            for (Classe classe : classes) {
                classesInfos.add(Map.of(
                        "classeId", classe.getId(),
                        "classeNom", classe.getName(),
                        "classeNiveau", classe.getNiveau()
                ));
            }

            return ResponseEntity.ok(classesInfos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la récupération: " + e.getMessage());
        }
    }
    @GetMapping("/etudiants/{etudiantId}/classe/nom")
    public ResponseEntity<?> getClasseNomForEtudiant(@PathVariable Long etudiantId) {
        try {
            if (etudiantId == null || etudiantId <= 0) {
                return ResponseEntity.badRequest().body("ID étudiant invalide");
            }

            Optional<Etudiant> etudiant = etudiantService.getEtudiantById(etudiantId);
            if (etudiant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Classe classe = etudiant.get().getClasse();
            if (classe == null) {
                return ResponseEntity.ok(Map.of("message", "L'étudiant n'est pas assigné à une classe"));
            }

            return ResponseEntity.ok(Map.of(
                    "classeId", classe.getId(),
                    "classeNom", classe.getName(),
                    "classeNiveau", classe.getNiveau()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la récupération: " + e.getMessage());
        }
    }

}