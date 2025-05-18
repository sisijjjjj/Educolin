package com.example.educoline.controller;

import com.example.educoline.entity.*;
import com.example.educoline.service.*;
import com.example.educoline.service.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200") // Assuming Angular runs on 4200
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

    @Autowired
    public AdminController(AdminService adminService,
                           EtudiantService etudiantService,
                           EnseignantService enseignantService,
                           NoteService noteService,
                           AbsenceService absenceService,
                           CoursService coursService,
                           EmploiService emploiService,
                           CongeService congeService,
                           ClasseService classeService,
                           ReunionService reunionService) {
        this.adminService = adminService;
        this.etudiantService = etudiantService;
        this.enseignantService = enseignantService;
        this.noteService = noteService;
        this.absenceService = absenceService;
        this.coursService = coursService;
        this.emploiService = emploiService;
        this.congeService = congeService;
        this.classeService = classeService;
        this.reunionService = reunionService;
    }

    // ==================== ADMIN CRUD ====================
    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @PostMapping("/add")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.createAdmin(admin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Admin admin = adminService.getAdminById(id);
        return admin != null ? ResponseEntity.ok(admin) : ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.updateAdmin(id, admin));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ETUDIANT CRUD ====================
    @GetMapping("/etudiants")
    public ResponseEntity<List<Etudiant>> getAllEtudiants() {
        return ResponseEntity.ok(etudiantService.getAllEtudiants());
    }

    @PostMapping("/etudiants")
    public ResponseEntity<Etudiant> createEtudiant(@RequestBody Etudiant etudiant) {
        return ResponseEntity.ok(etudiantService.saveEtudiant(etudiant));
    }

    @GetMapping("/etudiants/{id}")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable Long id) {
        Optional<Etudiant> etudiant = etudiantService.getEtudiantById(id);
        return etudiant.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/etudiants/{id}")
    public ResponseEntity<Etudiant> updateEtudiant(@PathVariable Long id, @RequestBody Etudiant etudiant) {
        Optional<Etudiant> existingEtudiant = etudiantService.getEtudiantById(id);
        if (existingEtudiant.isPresent()) {
            etudiant.setId(id);
            return ResponseEntity.ok(etudiantService.saveEtudiant(etudiant));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable Long id) {
        etudiantService.deleteEtudiant(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ETUDIANT RELATED ====================
    @PostMapping("/etudiants/{id}/notes")
    public ResponseEntity<Note> addNoteToEtudiant(@PathVariable Long id, @RequestBody Note note) {
        try {
            return ResponseEntity.ok(noteService.addNoteForEtudiant(id, note));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/enseignants")
    public Enseignant createEnseignant(@RequestBody Enseignant enseignant) {
        enseignant.setId(null);  // Assure-toi que l'id est null pour la création
        return enseignantService.createEnseignant(enseignant); // Utiliser le service, pas le repository directement
    }




    @GetMapping("/etudiants/{id}/notes")
    public ResponseEntity<List<Note>> getNotesForEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNotesByEtudiantId(id));
    }

    @GetMapping("/etudiants/{id}/absences")
    public ResponseEntity<List<Absence>> getAbsencesForEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(absenceService.getAbsencesByEtudiantId(id));
    }

    @GetMapping("/etudiants/{id}/cours")
    public ResponseEntity<List<Cours>> getCoursForEtudiant(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(coursService.getCoursByEtudiantId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/etudiants/{id}/emploi")
    public ResponseEntity<Emploi> getEmploiForEtudiant(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(emploiService.getEmploiByEtudiantId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // ==================== ENSEIGNANT CRUD ====================
    @GetMapping("/enseignants")
    public ResponseEntity<List<Enseignant>> getAllEnseignants() {
        return ResponseEntity.ok(enseignantService.getAllEnseignants());
    }




    @GetMapping("/enseignants/{id}")
    public ResponseEntity<Enseignant> getEnseignantById(@PathVariable Long id) {
        Optional<Enseignant> enseignant = enseignantService.getEnseignantById(id);
        return enseignant.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/enseignants/{id}")
    public ResponseEntity<Enseignant> updateEnseignant(@PathVariable Long id, @RequestBody Enseignant enseignant) {
        Optional<Enseignant> existingEnseignant = enseignantService.getEnseignantById(id);
        if (existingEnseignant.isPresent()) {
            enseignant.setId(id);
            return ResponseEntity.ok(enseignantService.saveEnseignant(enseignant));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/enseignants/{id}")
    public ResponseEntity<Void> deleteEnseignant(@PathVariable Long id) {
        enseignantService.deleteEnseignant(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENSEIGNANT RELATED ====================
    @GetMapping("/enseignants/{id}/conges")
    public ResponseEntity<List<CongeRequest>> getCongesForEnseignant(@PathVariable Long id) {
        return ResponseEntity.ok(congeService.getCongesByEnseignantId(id));
    }

    @GetMapping("/enseignants/{id}/absences")
    public ResponseEntity<List<Absence>> getAbsencesForEnseignant(@PathVariable Long id) {
        return ResponseEntity.ok(absenceService.getAbsencesByEnseignantId(id));
    }

    @GetMapping("/enseignants/{id}/emploi")
    public ResponseEntity<Emploi> getEmploiForEnseignant(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(emploiService.getEmploiByEnseignantId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/enseignants/{id}/reunions")
    public ResponseEntity<?> ajouterReunionPourEnseignant(
            @PathVariable Long id,
            @RequestBody Reunion reunion,
            @RequestParam(required = false, defaultValue = "true") boolean sendEmail) {

        try {
            Enseignant enseignant = enseignantService.getEnseignantById(id)
                    .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));

            reunion.setEnseignant(enseignant);
            Reunion savedReunion = reunionService.addReunionForEnseignant(id, reunion);

            if (sendEmail && enseignant.getEmail() != null) {
                EmailRequest.sendMeetingNotification(
                        enseignant.getEmail(),
                        enseignant.getPrenom() + " " + enseignant.getNom(),
                        savedReunion.getSujet(),
                        savedReunion.getDateHeure().toString(),
                        savedReunion.getLieu()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedReunion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création: " + e.getMessage());
        }
    }
    @PostMapping("/reunions/{reunionId}/send-email")
    public ResponseEntity<?> sendEmailForReunion(
            @PathVariable Long reunionId,
            @RequestParam(required = false) String customMessage) {

        try {
            Reunion reunion = reunionService.getReunionById(reunionId)
                    .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

            if (reunion.getEnseignant() == null) {
                return ResponseEntity.badRequest().body("Aucun enseignant associé à cette réunion");
            }

            Enseignant enseignant = reunion.getEnseignant();
            String message = customMessage != null ? customMessage :
                    "Une nouvelle réunion a été planifiée pour vous";

            EmailRequest.sendMeetingNotification(
                    enseignant.getEmail(),
                    enseignant.getPrenom() + " " + enseignant.getNom(),
                    reunion.getSujet(),
                    reunion.getDateHeure().toString(),
                    reunion.getLieu(),
                    message
            );

            return ResponseEntity.ok("Email envoyé avec succès à " + enseignant.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur d'envoi: " + e.getMessage());
        }
    }

    @GetMapping("/reunions")
    public ResponseEntity<List<Reunion>> getAllReunions() {
        try {
            List<Reunion> reunions = reunionService.getAllReunions();
            return ResponseEntity.ok(reunions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    // Récupérer toutes les demandes de congé
    @GetMapping("/conges")
    public ResponseEntity<List<CongeRequest>> getAllConges() {
        return ResponseEntity.ok(congeService.getAllConges());
    }

    // Récupérer les demandes par statut
    @GetMapping("/conges/statut/{statut}")
    public ResponseEntity<List<CongeRequest>> getCongesByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(congeService.getCongesByStatut(statut));
    }

    // Approuver une demande de congé
    @PutMapping("/conges/{congeId}/approve")
    public ResponseEntity<CongeRequest> approveConge(@PathVariable Long congeId) {
        try {
            CongeRequest conge = congeService.getCongeById(congeId);
            conge.setStatut("APPROUVE");
            CongeRequest updatedConge = congeService.saveConge(conge);

            // Envoyer une notification à l'enseignant si nécessaire
            if (updatedConge.getEnseignant().getEmail() != null) {
                EmailRequest.sendCongeApprovalNotification(
                        updatedConge.getEnseignant().getEmail(),
                        updatedConge.getEnseignant().getPrenom() + " " + updatedConge.getEnseignant().getNom(),
                        updatedConge.getType(),
                        updatedConge.getDateDebut().toString(),
                        updatedConge.getDateFin().toString()
                );
            }

            return ResponseEntity.ok(updatedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Rejeter une demande de congé
    @PutMapping("/conges/{congeId}/reject")
    public ResponseEntity<CongeRequest> rejectConge(@PathVariable Long congeId, @RequestBody(required = false) String raison) {
        try {
            CongeRequest conge = congeService.getCongeById(congeId);
            conge.setStatut("REJETE");
            if (raison != null) {
                conge.setMotif(conge.getMotif() + " (Rejeté: " + raison + ")");
            }
            CongeRequest updatedConge = congeService.saveConge(conge);

            // Envoyer une notification à l'enseignant si nécessaire
            if (updatedConge.getEnseignant().getEmail() != null) {
                EmailRequest.sendCongeRejectionNotification(
                        updatedConge.getEnseignant().getEmail(),
                        updatedConge.getEnseignant().getPrenom() + " " + updatedConge.getEnseignant().getNom(),
                        updatedConge.getType(),
                        updatedConge.getDateDebut().toString(),
                        updatedConge.getDateFin().toString(),
                        raison != null ? raison : "Raison non spécifiée"
                );
            }

            return ResponseEntity.ok(updatedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
