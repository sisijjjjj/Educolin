package com.example.educoline.controller;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Transactional
@RestController
@RequestMapping("/api/enseignants")
@CrossOrigin(origins = "http://localhost:4200/")
public class EnseignantController {

    private static final Logger logger = LoggerFactory.getLogger(EnseignantController.class);

    private final EnseignantRepository enseignantRepository;
    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final CongeRepository congeRepository;
    private final AbsenceRepository absenceRepository;
    private final ClasseRepository classeRepository;
    private final JavaMailSender mailSender;
    private final AdminRepository adminRepository;

    @Autowired
    public EnseignantController(EnseignantRepository enseignantRepository,
                                CoursRepository coursRepository,
                                EtudiantRepository etudiantRepository,
                                NoteRepository noteRepository,
                                CongeRepository congeRepository,
                                AbsenceRepository absenceRepository,
                                ClasseRepository classeRepository,
                                JavaMailSender mailSender,
                                AdminRepository adminRepository) {
        this.enseignantRepository = enseignantRepository;
        this.coursRepository = coursRepository;
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.congeRepository = congeRepository;
        this.absenceRepository = absenceRepository;
        this.classeRepository = classeRepository;
        this.mailSender = mailSender;
        this.adminRepository = adminRepository;
    }

    // 1. Gestion des enseignants
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Enseignant>> getAllEnseignants() {
        try {
            List<Enseignant> enseignants = enseignantRepository.findAll();
            return ResponseEntity.ok(enseignants);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des enseignants", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Enseignant> getEnseignantById(@PathVariable Long id) {
        try {
            return enseignantRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'enseignant avec l'ID: " + id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. Gestion des cours
    @GetMapping(value = "/{enseignantId}/cours", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Cours>> getCoursByEnseignant(@PathVariable Long enseignantId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }
            List<Cours> cours = coursRepository.findByEnseignantId(enseignantId);
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des cours pour l'enseignant ID: " + enseignantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/{enseignantId}/cours",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCoursForEnseignant(
            @PathVariable Long enseignantId,
            @Valid @RequestBody CoursRequest coursRequest) {
        try {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

            Cours cours = new Cours();
            cours.setNom(coursRequest.getNom());
            cours.setDescription(coursRequest.getDescription());
            cours.setNiveau(coursRequest.getNiveau());
            cours.setHeureDebut(coursRequest.getHeureDebut());
            cours.setHeureFin(coursRequest.getHeureFin());
            cours.setEnseignant(enseignant);
            cours.setEtudiants(new HashSet<>());

            if (coursRequest.getClasseId() != null) {
                Classe classe = classeRepository.findById(coursRequest.getClasseId())
                        .orElseThrow(() -> new EntityNotFoundException("Classe non trouvée"));
                cours.setClasse(classe);
            }

            Cours savedCours = coursRepository.save(cours);

            if (coursRequest.getClasseId() != null) {
                associerCoursAuxEtudiants(savedCours);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCours);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la création du cours", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création du cours: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{enseignantId}/cours/{coursId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCoursForEnseignant(
            @PathVariable Long enseignantId,
            @PathVariable Long coursId,
            @Valid @RequestBody CoursRequest coursRequest) {
        try {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

            Cours existingCours = coursRepository.findById(coursId)
                    .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

            if (!existingCours.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ce cours n'appartient pas à cet enseignant");
            }

            existingCours.setNom(coursRequest.getNom());
            existingCours.setDescription(coursRequest.getDescription());
            existingCours.setNiveau(coursRequest.getNiveau());
            existingCours.setHeureDebut(coursRequest.getHeureDebut());
            existingCours.setHeureFin(coursRequest.getHeureFin());

            if (coursRequest.getClasseId() != null) {
                Classe classe = classeRepository.findById(coursRequest.getClasseId())
                        .orElseThrow(() -> new EntityNotFoundException("Classe non trouvée"));
                existingCours.setClasse(classe);
                associerCoursAuxEtudiants(existingCours);
            } else {
                existingCours.setClasse(null);
                existingCours.setEtudiants(new HashSet<>());
            }

            Cours updatedCours = coursRepository.save(existingCours);
            return ResponseEntity.ok(updatedCours);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du cours", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la mise à jour du cours: " + e.getMessage());
        }
    }

    @DeleteMapping("/{enseignantId}/cours/{coursId}")
    @Transactional
    public ResponseEntity<?> deleteCours(
            @PathVariable Long enseignantId,
            @PathVariable Long coursId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }

            Optional<Cours> coursOpt = coursRepository.findById(coursId);
            if (coursOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Cours cours = coursOpt.get();
            if (!cours.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ce cours n'appartient pas à cet enseignant");
            }

            noteRepository.deleteByCours_Id(coursId);
            absenceRepository.deleteByCours_Id(coursId);

            Set<Etudiant> etudiants = cours.getEtudiants();
            if (etudiants != null) {
                for (Etudiant etudiant : etudiants) {
                    etudiant.getCours().remove(cours);
                }
                etudiantRepository.saveAll(etudiants);
                cours.getEtudiants().clear();
                coursRepository.save(cours);
            }

            coursRepository.deleteById(coursId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du cours ID: " + coursId, e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la suppression du cours: " + e.getMessage());
        }
    }

    // 3. Gestion des étudiants
    @GetMapping("/{enseignantId}/cours/{coursId}/etudiants")
    public ResponseEntity<?> getEtudiantsByCours(
            @PathVariable Long enseignantId,
            @PathVariable Long coursId) {
        try {
            Cours cours = coursRepository.findById(coursId)
                    .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

            if (!cours.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Cet enseignant n'enseigne pas ce cours");
            }

            if (cours.getClasse() == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Etudiant> etudiants = etudiantRepository.findByClasseId(cours.getClasse().getId());
            return ResponseEntity.ok(etudiants);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des étudiants", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur lors de la récupération des étudiants");
        }
    }

    @GetMapping("/{enseignantId}/etudiants")
    public ResponseEntity<?> getEtudiantsByEnseignant(@PathVariable Long enseignantId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }

            List<Etudiant> etudiants = etudiantRepository.findByCours_Enseignant_Id(enseignantId);

            List<Map<String, Object>> result = etudiants.stream().map(etudiant -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", etudiant.getId());
                dto.put("nom", etudiant.getNom());
                dto.put("prenom", etudiant.getPrenom());
                dto.put("email", etudiant.getEmail());
                dto.put("eliminated", etudiant.isEliminated());

                if (etudiant.getClasse() != null) {
                    Map<String, Object> classeDto = new HashMap<>();
                    classeDto.put("id", etudiant.getClasse().getId());
                    classeDto.put("nom", etudiant.getClasse().getName());
                    dto.put("classe", classeDto);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des étudiants pour enseignant ID: " + enseignantId, e);
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    // 4. Gestion des notes
    @GetMapping(value = "/{enseignantId}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Note>> getNotesByEnseignant(@PathVariable Long enseignantId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }
            List<Note> notes = noteRepository.findByEnseignantId(enseignantId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notes pour l'enseignant ID: " + enseignantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/{enseignantId}/notes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNote(@PathVariable Long enseignantId,
                                     @Valid @RequestBody NoteRequest noteRequest) {
        try {
            if (noteRequest == null) {
                return ResponseEntity.badRequest().body("La requête de note ne peut pas être nulle");
            }

            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

            Etudiant etudiant = etudiantRepository.findById(noteRequest.getEtudiantId())
                    .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

            Cours cours = coursRepository.findById(noteRequest.getCoursId())
                    .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

            if (!cours.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Cet enseignant n'enseigne pas ce cours");
            }

            if (etudiant.getClasse() == null || cours.getClasse() == null ||
                    !etudiant.getClasse().getId().equals(cours.getClasse().getId())) {
                return ResponseEntity.badRequest()
                        .body("L'étudiant n'appartient pas à la classe du cours");
            }

            Optional<Note> existingNote = noteRepository.findByEtudiantIdAndCoursId(
                    noteRequest.getEtudiantId(), noteRequest.getCoursId());

            Note note;
            if (existingNote.isPresent()) {
                note = existingNote.get();
            } else {
                note = new Note();
                note.setEtudiant(etudiant);
                note.setCours(cours);
                note.setEnseignant(enseignant);
            }

            note.setTp(noteRequest.getTp());
            note.setExam(noteRequest.getExam());
            note.setAbsences(noteRequest.getAbsences());
            note.setMoyenne(calculateMoyenne(noteRequest.getTp(), noteRequest.getExam()));
            note.setDateCreation(LocalDate.now());

            Note savedNote = noteRepository.save(note);

            // Notification à l'admin
            notifyAdminAboutNewNote(savedNote);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout de la note", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'ajout de la note: " + e.getMessage());
        }
    }

    @PutMapping(
            value = "/{enseignantId}/notes/{noteId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateNote(
            @PathVariable Long enseignantId,
            @PathVariable Long noteId,
            @Valid @RequestBody NoteUpdateRequest noteUpdateRequest) {

        try {
            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new EntityNotFoundException("Note non trouvée avec l'ID: " + noteId));

            if (!note.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Cette note n'appartient pas à l'enseignant avec l'ID: " + enseignantId);
            }

            if (noteUpdateRequest.getTp() != null && (noteUpdateRequest.getTp() < 0 || noteUpdateRequest.getTp() > 20)) {
                return ResponseEntity.badRequest().body("La note TP doit être entre 0 et 20");
            }

            if (noteUpdateRequest.getExam() != null && (noteUpdateRequest.getExam() < 0 || noteUpdateRequest.getExam() > 20)) {
                return ResponseEntity.badRequest().body("La note d'examen doit être entre 0 et 20");
            }

            boolean updated = false;

            if (noteUpdateRequest.getTp() != null) {
                note.setTp(noteUpdateRequest.getTp());
                updated = true;
            }

            if (noteUpdateRequest.getExam() != null) {
                note.setExam(noteUpdateRequest.getExam());
                updated = true;
            }

            if (noteUpdateRequest.getAbsences() != null) {
                note.setAbsences(noteUpdateRequest.getAbsences());
                updated = true;
            }

            if (!updated) {
                return ResponseEntity.badRequest().body("Aucune donnée valide fournie pour la mise à jour");
            }

            note.setMoyenne(calculateMoyenne(
                    noteUpdateRequest.getTp() != null ? noteUpdateRequest.getTp() : note.getTp(),
                    noteUpdateRequest.getExam() != null ? noteUpdateRequest.getExam() : note.getExam()
            ));

            Note updatedNote = noteRepository.save(note);

            // Notification à l'admin pour la mise à jour
            notifyAdminAboutUpdatedNote(updatedNote);

            return ResponseEntity.ok(updatedNote);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de la note ID: " + noteId, e);
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur lors de la mise à jour de la note");
        }
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId) {
        try {
            if (!noteRepository.existsById(noteId)) {
                return ResponseEntity.notFound().build();
            }

            noteRepository.deleteById(noteId);

            // Notification à l'admin pour la suppression
            notifyAdminAboutDeletedNote(noteId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la note ID: " + noteId, e);
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de la note: " + e.getMessage());
        }
    }

    // 5. Gestion des congés
    @GetMapping(value = "/{enseignantId}/conges", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conge>> getCongesByEnseignant(@PathVariable Long enseignantId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }
            List<Conge> conges = congeRepository.findByEnseignantId(enseignantId);
            return ResponseEntity.ok(conges);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des congés pour l'enseignant ID: " + enseignantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/{enseignantId}/conges",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> demanderConge(
            @PathVariable Long enseignantId,
            @Valid @RequestBody CongeRequest congeRequest) {

        if (congeRequest.getDateDebut().isAfter(congeRequest.getDateFin())) {
            return ResponseEntity.badRequest()
                    .body("La date de fin doit être après la date de début");
        }

        try {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

            boolean hasConflict = congeRepository.existsByEnseignantAndStatutAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                    enseignant,
                    Conge.StatutConge.APPROUVE,
                    congeRequest.getDateFin(),
                    congeRequest.getDateDebut()
            );

            if (hasConflict) {
                return ResponseEntity.badRequest()
                        .body("Vous avez déjà un congé approuvé pendant cette période");
            }

            Conge conge = new Conge();
            conge.setType(Conge.TypeConge.valueOf(congeRequest.getType()));
            conge.setMotif(congeRequest.getMotif());
            conge.setDateDebut(congeRequest.getDateDebut());
            conge.setDateFin(congeRequest.getDateFin());
            conge.setStatut(Conge.StatutConge.EN_ATTENTE);
            conge.setEnseignant(enseignant);

            Conge savedConge = congeRepository.save(conge);
            notifyAdminAndTeacher(savedConge);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedConge);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la demande de congé", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la demande de congé: " + e.getMessage());
        }
    }

    // 6. Gestion des absences
    @GetMapping(value = "/{enseignantId}/absences", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAbsencesByEnseignant(@PathVariable Long enseignantId) {
        try {
            if (!enseignantRepository.existsById(enseignantId)) {
                return ResponseEntity.notFound().build();
            }

            List<Absence> absences = absenceRepository.findByEnseignantId(enseignantId);

            List<Map<String, Object>> result = absences.stream().map(absence -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", absence.getId());
                dto.put("date", absence.getDate());
                dto.put("justifiee", absence.isJustifiee());
                dto.put("motif", absence.getMotif());

                if (absence.getEtudiant() != null) {
                    Map<String, Object> etudiantDto = new HashMap<>();
                    etudiantDto.put("id", absence.getEtudiant().getId());
                    etudiantDto.put("nom", absence.getEtudiant().getNom());
                    etudiantDto.put("prenom", absence.getEtudiant().getPrenom());
                    dto.put("etudiant", etudiantDto);
                }

                if (absence.getCours() != null) {
                    Map<String, Object> coursDto = new HashMap<>();
                    coursDto.put("id", absence.getCours().getId());
                    coursDto.put("nom", absence.getCours().getNom());
                    dto.put("cours", coursDto);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des absences", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{enseignantId}/absences",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAbsence(@PathVariable Long enseignantId,
                                           @Valid @RequestBody AbsenceRequest absenceRequest) {
        try {
            if (absenceRequest.getDate() == null) {
                return ResponseEntity.badRequest().body("La date est requise");
            }

            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

            Etudiant etudiant = etudiantRepository.findById(absenceRequest.getEtudiantId())
                    .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

            Cours cours = coursRepository.findById(absenceRequest.getCoursId())
                    .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

            if (!cours.getEnseignant().getId().equals(enseignantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Cet enseignant n'enseigne pas ce cours");
            }

            if (etudiant.getClasse() == null || cours.getClasse() == null ||
                    !etudiant.getClasse().getId().equals(cours.getClasse().getId())) {
                return ResponseEntity.badRequest()
                        .body("L'étudiant n'appartient pas à la classe du cours");
            }

            Absence absence = new Absence();
            absence.setDate(absenceRequest.getDate());
            absence.setJustifiee(absenceRequest.isJustifiee());
            absence.setMotif(absenceRequest.getMotif());
            absence.setEnseignant(enseignant);
            absence.setEtudiant(etudiant);
            absence.setCours(cours);

            Absence savedAbsence = absenceRepository.save(absence);
            updateAbsenceCount(etudiant.getId(), cours.getId());
            checkAndMarkEliminated(etudiant.getId(), cours.getId());

            // Notification à l'admin
            notifyAdminAboutNewAbsence(savedAbsence);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAbsence);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'absence", e);
            return ResponseEntity.badRequest().body("Erreur lors de la création de l'absence: " + e.getMessage());
        }
    }

    @PutMapping(value = "/absences/{absenceId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAbsence(@PathVariable Long absenceId,
                                           @Valid @RequestBody AbsenceRequest absenceRequest) {
        try {
            Absence absence = absenceRepository.findById(absenceId)
                    .orElseThrow(() -> new EntityNotFoundException("Absence non trouvée"));

            absence.setDate(absenceRequest.getDate());
            absence.setJustifiee(absenceRequest.isJustifiee());
            absence.setMotif(absenceRequest.getMotif());

            Absence updatedAbsence = absenceRepository.save(absence);

            if (updatedAbsence.getEtudiant() != null && updatedAbsence.getCours() != null) {
                updateAbsenceCount(updatedAbsence.getEtudiant().getId(), updatedAbsence.getCours().getId());
                checkAndMarkEliminated(updatedAbsence.getEtudiant().getId(), updatedAbsence.getCours().getId());
            }

            // Notification à l'admin pour la mise à jour
            notifyAdminAboutUpdatedAbsence(updatedAbsence);

            return ResponseEntity.ok(updatedAbsence);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'absence ID: " + absenceId, e);
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour de l'absence: " + e.getMessage());
        }
    }

    @DeleteMapping("/absences/{absenceId}")
    public ResponseEntity<?> deleteAbsence(@PathVariable Long absenceId) {
        try {
            Absence absence = absenceRepository.findById(absenceId)
                    .orElseThrow(() -> new EntityNotFoundException("Absence non trouvée"));

            Long etudiantId = absence.getEtudiant() != null ? absence.getEtudiant().getId() : null;
            Long coursId = absence.getCours() != null ? absence.getCours().getId() : null;

            absenceRepository.deleteById(absenceId);

            if (etudiantId != null && coursId != null) {
                updateAbsenceCount(etudiantId, coursId);
                checkAndMarkEliminated(etudiantId, coursId);
            }

            // Notification à l'admin pour la suppression
            notifyAdminAboutDeletedAbsence(absenceId);

            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'absence ID: " + absenceId, e);
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de l'absence: " + e.getMessage());
        }
    }

    // Méthodes utilitaires
    private double calculateMoyenne(double tp, double exam) {
        return (tp * 0.3) + (exam * 0.7);
    }

    private void updateAbsenceCount(Long etudiantId, Long coursId) {
        Optional<Note> noteOpt = noteRepository.findByEtudiantIdAndCoursId(etudiantId, coursId);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            int totalAbsences = absenceRepository.countByEtudiantIdAndCoursId(etudiantId, coursId);
            note.setAbsences(totalAbsences);
            noteRepository.save(note);
        }
    }

    private void checkAndMarkEliminated(Long etudiantId, Long coursId) {
        int unjustifiedAbsences = absenceRepository.countByEtudiantIdAndCoursIdAndJustifieeFalse(etudiantId, coursId);

        if (unjustifiedAbsences >= 3) {
            Optional<Etudiant> etudiantOpt = etudiantRepository.findById(etudiantId);
            if (etudiantOpt.isPresent()) {
                Etudiant etudiant = etudiantOpt.get();
                etudiant.setEliminated(true);
                etudiantRepository.save(etudiant);
            }
        }
    }

    @Transactional
    protected void associerCoursAuxEtudiants(Cours cours) {
        if (cours.getClasse() == null) {
            return;
        }

        List<Etudiant> etudiants = etudiantRepository.findByClasseId(cours.getClasse().getId());

        if (etudiants == null || etudiants.isEmpty()) {
            return;
        }

        if (cours.getEtudiants() == null) {
            cours.setEtudiants(new HashSet<>());
        }

        for (Etudiant etudiant : etudiants) {
            if (etudiant.getCours() == null) {
                etudiant.setCours(new HashSet<>());
            }

            if (!etudiant.getCours().contains(cours)) {
                etudiant.getCours().add(cours);
                cours.getEtudiants().add(etudiant);
                etudiantRepository.save(etudiant);

                boolean noteExists = noteRepository.existsByEtudiantIdAndCoursId(
                        etudiant.getId(), cours.getId());

                if (!noteExists) {
                    Note note = new Note();
                    note.setTp(0.0);
                    note.setExam(0.0);
                    note.setMoyenne(0.0);
                    note.setAbsences(0);
                    note.setCours(cours);
                    note.setEtudiant(etudiant);
                    note.setEnseignant(cours.getEnseignant());
                    noteRepository.save(note);
                }
            }
        }

        coursRepository.save(cours);
    }

    // Méthodes de notification
    private void notifyAdminAboutNewNote(Note note) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Nouvelle note enregistrée";
            String content = String.format(
                    "Une nouvelle note a été enregistrée par l'enseignant %s %s:\n\n" +
                            "Étudiant: %s %s\n" +
                            "Cours: %s\n" +
                            "Note TP: %.2f\n" +
                            "Note Examen: %.2f\n" +
                            "Moyenne: %.2f\n" +
                            "Date: %s",
                    note.getEnseignant().getPrenom(),
                    note.getEnseignant().getNom(),
                    note.getEtudiant().getPrenom(),
                    note.getEtudiant().getNom(),
                    note.getCours().getNom(),
                    note.getTp(),
                    note.getExam(),
                    note.getMoyenne(),
                    note.getDateCreation().format(String.valueOf(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAboutUpdatedNote(Note note) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Note mise à jour";
            String content = String.format(
                    "Une note a été mise à jour par l'enseignant %s %s:\n\n" +
                            "Étudiant: %s %s\n" +
                            "Cours: %s\n" +
                            "Nouvelles valeurs:\n" +
                            " - TP: %.2f\n" +
                            " - Examen: %.2f\n" +
                            " - Moyenne: %.2f\n" +
                            "Date de modification: %s",
                    note.getEnseignant().getPrenom(),
                    note.getEnseignant().getNom(),
                    note.getEtudiant().getPrenom(),
                    note.getEtudiant().getNom(),
                    note.getCours().getNom(),
                    note.getTp(),
                    note.getExam(),
                    note.getMoyenne(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAboutDeletedNote(Long noteId) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Note supprimée";
            String content = String.format(
                    "Une note a été supprimée du système.\n" +
                            "ID de la note: %d\n" +
                            "Date de suppression: %s",
                    noteId,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAboutNewAbsence(Absence absence) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Nouvelle absence enregistrée";
            String content = String.format(
                    "Une nouvelle absence a été enregistrée par l'enseignant %s %s:\n\n" +
                            "Étudiant: %s %s\n" +
                            "Cours: %s\n" +
                            "Date: %s\n" +
                            "Statut: %s\n" +
                            "Motif: %s",
                    absence.getEnseignant().getPrenom(),
                    absence.getEnseignant().getNom(),
                    absence.getEtudiant().getPrenom(),
                    absence.getEtudiant().getNom(),
                    absence.getCours().getNom(),
                    absence.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    absence.isJustifiee() ? "Justifiée" : "Non justifiée",
                    absence.getMotif() != null ? absence.getMotif() : "Non spécifié"
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAboutUpdatedAbsence(Absence absence) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Absence mise à jour";
            String content = String.format(
                    "Une absence a été mise à jour par l'enseignant %s %s:\n\n" +
                            "Étudiant: %s %s\n" +
                            "Cours: %s\n" +
                            "Date: %s\n" +
                            "Nouveau statut: %s\n" +
                            "Nouveau motif: %s",
                    absence.getEnseignant().getPrenom(),
                    absence.getEnseignant().getNom(),
                    absence.getEtudiant().getPrenom(),
                    absence.getEtudiant().getNom(),
                    absence.getCours().getNom(),
                    absence.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    absence.isJustifiee() ? "Justifiée" : "Non justifiée",
                    absence.getMotif() != null ? absence.getMotif() : "Non spécifié"
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAboutDeletedAbsence(Long absenceId) {
        try {
            List<Admin> admins = adminRepository.findAll();
            if (admins.isEmpty()) return;

            String subject = "Absence supprimée";
            String content = String.format(
                    "Une absence a été supprimée du système.\n" +
                            "ID de l'absence: %d\n" +
                            "Date de suppression: %s",
                    absenceId,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@educoline.com");
            message.setSubject(subject);
            message.setText(content);

            for (Admin admin : admins) {
                message.setTo(admin.getEmail());
                mailSender.send(message);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification aux admins", e);
        }
    }

    private void notifyAdminAndTeacher(Conge conge) {
        try {
            // Notification à l'admin
            String adminEmail = "admin@educoline.com";
            String adminSubject = "Nouvelle demande de congé - " + conge.getEnseignant().getNom();
            String adminContent = String.format(
                    "Détails de la demande :\n\n" +
                            "Enseignant: %s\n" +
                            "Type: %s\n" +
                            "Période: du %s au %s\n" +
                            "Motif: %s\n\n" +
                            "Action requise: Veuillez traiter cette demande dans le système.",
                    conge.getEnseignant().getNom(),
                    conge.getType().toString(),
                    conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    conge.getMotif()
            );
            sendEmail(adminEmail, adminSubject, adminContent);

            // Notification à l'enseignant
            String teacherEmail = conge.getEnseignant().getEmail();
            String teacherSubject = "Votre demande de congé a été enregistrée";
            String teacherContent = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre demande de congé (%s) du %s au %s a bien été enregistrée.\n" +
                            "Statut: En attente de validation\n\n" +
                            "Cordialement,\nL'administration ÉduColine",
                    conge.getEnseignant().getNom(),
                    conge.getType().toString(),
                    conge.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    conge.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            sendEmail(teacherEmail, teacherSubject, teacherContent);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi des notifications pour le congé", e);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@educoline.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à " + to, e);
        }
    }

    // Classes DTO
    public static class CoursRequest {
        private String nom;
        private String description;
        private String niveau;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime heureDebut;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime heureFin;
        private Long classeId;

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
        public Long getClasseId() { return classeId; }
        public void setClasseId(Long classeId) { this.classeId = classeId; }
    }

    public static class NoteRequest {
        private Long etudiantId;
        private Long coursId;
        @Min(value = 0, message = "La note TP ne peut pas être négative")
        @Max(value = 20, message = "La note TP ne peut pas dépasser 20")
        private Double tp;
        @Min(value = 0, message = "La note d'examen ne peut pas être négative")
        @Max(value = 20, message = "La note d'examen ne peut pas dépasser 20")
        private Double exam;
        @Min(value = 0, message = "Le nombre d'absences ne peut pas être négatif")
        private Integer absences;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        public Long getEtudiantId() { return etudiantId; }
        public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
        public Long getCoursId() { return coursId; }
        public void setCoursId(Long coursId) { this.coursId = coursId; }
        public Double getTp() { return tp; }
        public void setTp(Double tp) { this.tp = tp; }
        public Double getExam() { return exam; }
        public void setExam(Double exam) { this.exam = exam; }
        public Integer getAbsences() { return absences; }
        public void setAbsences(Integer absences) { this.absences = absences; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }

    public static class NoteUpdateRequest {
        @Min(value = 0, message = "La note TP ne peut pas être négative")
        @Max(value = 20, message = "La note TP ne peut pas dépasser 20")
        private Double tp;
        @Min(value = 0, message = "La note d'examen ne peut pas être négative")
        @Max(value = 20, message = "La note d'examen ne peut pas dépasser 20")
        private Double exam;
        @Min(value = 0, message = "Le nombre d'absences ne peut pas être négatif")
        private Integer absences;

        public Double getTp() { return tp; }
        public void setTp(Double tp) { this.tp = tp; }
        public Double getExam() { return exam; }
        public void setExam(Double exam) { this.exam = exam; }
        public Integer getAbsences() { return absences; }
        public void setAbsences(Integer absences) { this.absences = absences; }
    }

    public static class CongeRequest {
        private String type;
        private String motif;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDebut;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFin;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMotif() { return motif; }
        public void setMotif(String motif) { this.motif = motif; }
        public LocalDate getDateDebut() { return dateDebut; }
        public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
        public LocalDate getDateFin() { return dateFin; }
        public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    }

    public static class AbsenceRequest {
        private Long etudiantId;
        private Long coursId;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private boolean justifiee;
        private String motif;

        public Long getEtudiantId() { return etudiantId; }
        public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }
        public Long getCoursId() { return coursId; }
        public void setCoursId(Long coursId) { this.coursId = coursId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public boolean isJustifiee() { return justifiee; }
        public void setJustifiee(boolean justifiee) { this.justifiee = justifiee; }
        public String getMotif() { return motif; }
        public void setMotif(String motif) { this.motif = motif; }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralExceptions(Exception ex) {
        logger.error("Erreur non gérée", ex);
        return ResponseEntity.internalServerError()
                .body("Une erreur est survenue: " + ex.getMessage());
    }
}