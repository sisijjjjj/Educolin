package com.example.educoline.controller;

import com.example.educoline.ResourceNotFoundException;
import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import com.example.educoline.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enseignants")
@CrossOrigin(origins = "http://localhost:4200")
public class EnseignantController {

    private final EnseignantRepository enseignantRepository;
    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final NoteService noteService;
    private final CongeRepository congeRepository;

    @Autowired
    public EnseignantController(EnseignantRepository enseignantRepository,
                                CoursRepository coursRepository,
                                EtudiantRepository etudiantRepository,
                                NoteRepository noteRepository,
                                NoteService noteService,
                                CongeRepository congeRepository) {
        this.enseignantRepository = enseignantRepository;
        this.coursRepository = coursRepository;
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.noteService = noteService;
        this.congeRepository = congeRepository;
    }

    // 1. Gestion des enseignants
    @GetMapping
    public ResponseEntity<List<Enseignant>> getAllEnseignants() {
        return ResponseEntity.ok(enseignantRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Enseignant> createEnseignant(@RequestBody Enseignant enseignant) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enseignantRepository.save(enseignant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enseignant> getEnseignantById(@PathVariable Long id) {
        return enseignantRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enseignant> updateEnseignant(@PathVariable Long id, @RequestBody Enseignant updatedEnseignant) {
        return enseignantRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedEnseignant.getName());
                    existing.setEmail(updatedEnseignant.getEmail());
                    existing.setSubject(updatedEnseignant.getSubject());
                    return ResponseEntity.ok(enseignantRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnseignant(@PathVariable Long id) {
        return enseignantRepository.findById(id)
                .map(enseignant -> {
                    enseignantRepository.delete(enseignant);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. Gestion des cours
    @PostMapping("/{enseignantId}/cours")
    public ResponseEntity<Cours> addCoursToEnseignant(@PathVariable Long enseignantId, @RequestBody Cours cours) {
        return enseignantRepository.findById(enseignantId)
                .map(enseignant -> {
                    cours.setEnseignant(enseignant);
                    return ResponseEntity.status(HttpStatus.CREATED).body(coursRepository.save(cours));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{enseignantId}/cours")
    public ResponseEntity<List<Cours>> getCoursByEnseignant(@PathVariable Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coursRepository.findByEnseignantId(enseignantId));
    }

    @PutMapping("/{enseignantId}/cours/{coursId}")
    public ResponseEntity<Cours> updateCours(@PathVariable Long enseignantId,
                                             @PathVariable Long coursId,
                                             @RequestBody Cours coursUpdated) {
        return coursRepository.findById(coursId)
                .filter(cours -> cours.getEnseignant() != null && cours.getEnseignant().getId().equals(enseignantId))
                .map(cours -> {
                    cours.setNom(coursUpdated.getNom());
                    cours.setDescription(coursUpdated.getDescription());
                    cours.setHeureDebut(coursUpdated.getHeureDebut());
                    cours.setHeureFin(coursUpdated.getHeureFin());
                    return ResponseEntity.ok(coursRepository.save(cours));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{enseignantId}/cours/{coursId}")
    public ResponseEntity<Void> deleteCours(@PathVariable Long enseignantId, @PathVariable Long coursId) {
        return coursRepository.findById(coursId)
                .filter(cours -> cours.getEnseignant() != null && cours.getEnseignant().getId().equals(enseignantId))
                .map(cours -> {
                    coursRepository.delete(cours);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Gestion des notes et absences
    @PutMapping("/{enseignantId}/notes/{etudiantId}/{coursId}")
    public ResponseEntity<Note> updateNotesAndAbsences(@PathVariable Long enseignantId,
                                                       @PathVariable Long etudiantId,
                                                       @PathVariable Long coursId,
                                                       @RequestBody Note noteUpdated) {
        Optional<Etudiant> etudiantOpt = etudiantRepository.findById(etudiantId);
        Optional<Cours> coursOpt = coursRepository.findById(coursId);

        if (etudiantOpt.isEmpty() || coursOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Etudiant etudiant = etudiantOpt.get();
        Cours cours = coursOpt.get();

        if (cours.getEnseignant() == null || !cours.getEnseignant().getId().equals(enseignantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (etudiant.getCours() == null || !etudiant.getCours().contains(cours)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Note> noteOpt = noteRepository.findByEtudiantAndCours(etudiant, cours);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Note note = noteOpt.get();
        note.setTp(noteUpdated.getTp());
        note.setExam(noteUpdated.getExam());
        note.setAbsences(noteUpdated.getAbsences());

        if (note.getAbsences() > 3) {
            note.setExam(0.0);
            note.setRetakeSession(true);
        }

        Note savedNote = noteRepository.save(note);
        return ResponseEntity.ok(savedNote);
    }

    // 4. Détails de l'enseignant
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getEnseignantDetails(@PathVariable Long id) {
        return enseignantRepository.findById(id)
                .map(enseignant -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", enseignant.getId());
                    response.put("name", enseignant.getName());
                    response.put("email", enseignant.getEmail());
                    response.put("subject", enseignant.getSubject());

                    // Cours
                    List<Map<String, Object>> coursData = coursRepository.findByEnseignantId(id).stream()
                            .map(cours -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", cours.getId());
                                map.put("titre", cours.getNom());
                                map.put("horaire", cours.getDescription());
                                return map;
                            })
                            .collect(Collectors.toList());
                    response.put("cours", coursData);

                    // Notes et absences
                    List<Note> notes = noteRepository.findByEnseignantId(id);

                    List<Map<String, Object>> absencesData = notes.stream()
                            .filter(note -> note.getEtudiant() != null && note.getCours() != null)
                            .map(note -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("etudiantId", note.getEtudiant().getId());
                                map.put("coursId", note.getCours().getId());
                                map.put("nombreAbsences", note.getAbsences());
                                return map;
                            })
                            .collect(Collectors.toList());
                    response.put("absences", absencesData);

                    List<Map<String, Object>> notesData = notes.stream()
                            .filter(note -> note.getEtudiant() != null && note.getCours() != null)
                            .map(note -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("etudiantId", note.getEtudiant().getId());
                                map.put("coursId", note.getCours().getId());
                                map.put("tp", note.getTp());
                                map.put("examen", note.getExam());
                                return map;
                            })
                            .collect(Collectors.toList());
                    response.put("notes", notesData);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Gestion des étudiants
    @PostMapping("/{enseignantId}/cours/{coursId}/etudiant")
    public ResponseEntity<Etudiant> addEtudiantToCours(@PathVariable Long enseignantId,
                                                       @PathVariable Long coursId,
                                                       @RequestBody Etudiant etudiant) {
        return coursRepository.findById(coursId)
                .filter(cours -> cours.getEnseignant() != null && cours.getEnseignant().getId().equals(enseignantId))
                .map(cours -> {
                    Etudiant savedEtudiant = etudiantRepository.save(etudiant);

                    // Mise à jour relation cours-étudiant
                    if (cours.getEtudiants() == null) {
                        cours.setEtudiants(new ArrayList<>());
                    }
                    if (!cours.getEtudiants().contains(savedEtudiant)) {
                        cours.getEtudiants().add(savedEtudiant);
                        coursRepository.save(cours);
                    }

                    // Mise à jour relation étudiant-cours
                    if (savedEtudiant.getCours() == null) {
                        savedEtudiant.setCours(new HashSet<>());
                    }
                    if (!savedEtudiant.getCours().contains(cours)) {
                        savedEtudiant.getCours().add(cours);
                        etudiantRepository.save(savedEtudiant);
                    }

                    return ResponseEntity.status(HttpStatus.CREATED).body(savedEtudiant);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. Gestion des congés
    @PostMapping("/{enseignantId}/conges")
    public ResponseEntity<CongeRequest> demanderConge(@PathVariable Long enseignantId,
                                                      @RequestBody CongeRequest congeRequest) {
        return enseignantRepository.findById(enseignantId)
                .map(enseignant -> {
                    CongeRequest conge = new CongeRequest();
                    conge.setType(congeRequest.getType());
                    conge.setMotif(congeRequest.getMotif());
                    conge.setDateDebut(congeRequest.getDateDebut());
                    conge.setDateFin(congeRequest.getDateFin());
                    conge.setEnseignant(enseignant);
                    conge.setStatut("EN_ATTENTE");

                    CongeRequest savedConge = congeRepository.save(conge);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedConge);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{enseignantId}/conges")
    public ResponseEntity<List<CongeRequest>> getCongesByEnseignant(@PathVariable Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(congeRepository.findByEnseignantId(enseignantId));
    }

    @GetMapping("/conges/statut/{statut}")
    public ResponseEntity<List<CongeRequest>> getCongesByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(congeRepository.findByStatut(statut));
    }
}