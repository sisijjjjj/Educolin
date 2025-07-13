package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.AbsenceRepository;
import com.example.educoline.repository.EtudiantRepository;
import com.example.educoline.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final CoursService coursService;
    private final ClasseService classeService;
    private final EnseignantService enseignantService;
    private final NoteRepository noteRepository;
    private final AbsenceRepository absenceRepository;

    @Autowired
    public EtudiantService(EtudiantRepository etudiantRepository,
                           CoursService coursService,
                           ClasseService classeService,
                           EnseignantService enseignantService,
                           NoteRepository noteRepository,
                           AbsenceRepository absenceRepository) {
        this.etudiantRepository = etudiantRepository;
        this.coursService = coursService;
        this.classeService = classeService;
        this.enseignantService = enseignantService;
        this.noteRepository = noteRepository;
        this.absenceRepository = absenceRepository;
    }

    public Optional<Etudiant> getEtudiantById(Long id) {
        return etudiantRepository.findById(id);
    }

    public Etudiant findEtudiant(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
    }

    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    @Transactional
    public Etudiant createEtudiant(Etudiant etudiant) {
        validateEtudiant(etudiant);
        initializeEtudiantCollections(etudiant);
        return etudiantRepository.save(etudiant);
    }

    @Transactional
    public Etudiant updateEtudiant(Long id, Etudiant etudiantDetails) {
        Etudiant etudiant = findEtudiant(id);

        etudiant.setNom(etudiantDetails.getNom());
        etudiant.setPrenom(etudiantDetails.getPrenom());
        etudiant.setEmail(etudiantDetails.getEmail());
        etudiant.setDateNaissance(etudiantDetails.getDateNaissance());
        etudiant.setStatus(etudiantDetails.getStatus());

        if (etudiantDetails.getClasse() != null) {
            Classe classe = classeService.findClasse(etudiantDetails.getClasse().getId());
            etudiant.setClasse(classe);
        }

        if (etudiantDetails.getEnseignant() != null) {
            Enseignant enseignant = enseignantService.findEnseignant(etudiantDetails.getEnseignant().getId());
            etudiant.setEnseignant(enseignant);
        }

        etudiant.setEliminated(etudiantDetails.isEliminated());

        return etudiantRepository.save(etudiant);
    }

    @Transactional
    public void addCoursToEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        if (etudiant.getCours() == null) {
            etudiant.setCours(new HashSet<>());
        }

        etudiant.addCours(cours);
        etudiantRepository.save(etudiant);
    }

    @Transactional
    public void removeCoursFromEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        etudiant.removeCours(cours);
        etudiantRepository.save(etudiant);
    }

    @Transactional
    public void addNoteTP(Long etudiantId, Long coursId, double note) {
        validateNote(note);
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        etudiant.ajouterNoteTP(cours, note);
        etudiantRepository.save(etudiant);
    }

    @Transactional
    public void addNoteExamen(Long etudiantId, Long coursId, double note) {
        validateNote(note);
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        etudiant.ajouterNoteExamen(cours, note);
        etudiantRepository.save(etudiant);
    }

    @Transactional
    public void addAbsenceToEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        etudiant.incrementerAbsence(cours);
        etudiantRepository.save(etudiant);
    }

    @Transactional
    public void removeAbsenceFromEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);

        etudiant.decrementerAbsence(cours);
        etudiantRepository.save(etudiant);
    }

    public List<Etudiant> getEtudiantsByClasse(Long classeId) {
        return etudiantRepository.findByClasseId(classeId);
    }

    public List<Etudiant> getEtudiantsByEnseignant(Long enseignantId) {
        return etudiantRepository.findByEnseignantId(enseignantId);
    }

    public List<Etudiant> getEtudiantsElimines() {
        return etudiantRepository.findByEliminatedTrue();
    }

    public List<Etudiant> getEtudiantsNonElimines() {
        return etudiantRepository.findByEliminatedFalse();
    }

    public Double getMoyenneForCours(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);
        return etudiant.getMoyennePourMatiere(cours);
    }

    public Integer getNombreAbsencesForCours(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);
        return etudiant.getNombreAbsencesPourMatiere(cours);
    }

    public boolean isEliminatedForCours(Long etudiantId, Long coursId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Cours cours = coursService.getCours(coursId);
        return etudiant.estElimineDansMatiere(cours);
    }

    public List<Cours> getAllCoursForEtudiant(Long etudiantId) {
        return new ArrayList<>(findEtudiant(etudiantId).getCours());
    }

    @Transactional
    public Etudiant saveEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public List<Etudiant> getEtudiantsByClasseId(Long id) {
        return etudiantRepository.findByClasseId(id);
    }

    @Transactional
    public Etudiant assignerClasse(Long etudiantId, Long classeId) {
        Etudiant etudiant = findEtudiant(etudiantId);
        Classe classe = classeService.findClasse(classeId);
        etudiant.setClasse(classe);
        return etudiantRepository.save(etudiant);
    }

    @Transactional
    public boolean deleteEtudiant(Long id) {
        Optional<Etudiant> etudiantOpt = etudiantRepository.findById(id);
        if (etudiantOpt.isEmpty()) {
            return false;
        }

        Etudiant etudiant = etudiantOpt.get();

        // 1. Supprimer les relations avec les cours
        etudiant.getCours().clear();

        // 2. Supprimer manuellement les notes (au cas où le CASCADE ne fonctionne pas)
        noteRepository.deleteAllByEtudiant(etudiant);

        // 3. Supprimer manuellement les absences (au cas où le CASCADE ne fonctionne pas)
        absenceRepository.deleteAllByEtudiant(etudiant);

        // 4. Enfin supprimer l'étudiant
        etudiantRepository.delete(etudiant);

        return true;
    }
    public List<Cours> getAllCoursForAllEtudiants() {
        return List.of();
    }

    private void initializeEtudiantCollections(Etudiant etudiant) {
        if (etudiant.getNotes() == null) etudiant.setNotes(new HashSet<>());
        if (etudiant.getNotesTP() == null) etudiant.setNotesTP(new HashMap<>());
        if (etudiant.getNotesExamen() == null) etudiant.setNotesExamen(new HashMap<>());
        if (etudiant.getMoyennes() == null) etudiant.setMoyennes(new HashMap<>());
        if (etudiant.getAbsencesParMatiere() == null) etudiant.setAbsencesParMatiere(new HashMap<>());
        if (etudiant.getEliminationParMatiere() == null) etudiant.setEliminationParMatiere(new HashMap<>());
    }

    private void validateEtudiant(Etudiant etudiant) {
        if (etudiant.getNom() == null || etudiant.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (etudiant.getPrenom() == null || etudiant.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        if (etudiant.getEmail() != null && !etudiant.getEmail().isEmpty()) {
            if (etudiantRepository.existsByEmail(etudiant.getEmail())) {
                throw new IllegalArgumentException("Un étudiant avec cet email existe déjà");
            }
        }
    }

    private void validateNote(double note) {
        if (note < 0 || note > 20) {
            throw new IllegalArgumentException("La note doit être entre 0 et 20");
        }
    }

    public void addCoursToClassStudents(Cours cours) {
    }

    public boolean existsByEmail(String email) {
        return false;
    }

    public boolean etudiantExists(Long etudiantId) {
        return false;
    }
}