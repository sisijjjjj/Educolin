package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EnseignantService {

    private final EnseignantRepository enseignantRepository;
    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final CongeRepository congeRepository;
    private final AbsenceRepository absenceRepository;
    private final ClasseRepository classeRepository;

    @Autowired
    public EnseignantService(EnseignantRepository enseignantRepository,
                             CoursRepository coursRepository,
                             EtudiantRepository etudiantRepository,
                             NoteRepository noteRepository,
                             CongeRepository congeRepository,
                             AbsenceRepository absenceRepository,
                             ClasseRepository classeRepository) {
        this.enseignantRepository = enseignantRepository;
        this.coursRepository = coursRepository;
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.congeRepository = congeRepository;
        this.absenceRepository = absenceRepository;
        this.classeRepository = classeRepository;
    }

    // 1. Gestion des enseignants
    public List<Enseignant> getAllEnseignants() {
        return enseignantRepository.findAll();
    }

    public Optional<Enseignant> getEnseignantById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant ne peut pas être null");
        }
        return enseignantRepository.findById(id);
    }

    public Enseignant saveEnseignant(Enseignant enseignant) {
        if (enseignant == null) {
            throw new IllegalArgumentException("L'objet enseignant ne peut pas être null");
        }
        return enseignantRepository.save(enseignant);
    }

    public boolean enseignantExists(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant ne peut pas être null");
        }
        return enseignantRepository.existsById(id);
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }
        return enseignantRepository.existsByEmail(email);
    }

    public void deleteEnseignant(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant ne peut pas être null");
        }
        if (!enseignantRepository.existsById(id)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + id);
        }
        enseignantRepository.deleteById(id);
    }
    @Transactional
    // 2. Gestion des cours
    public List<Cours> getCoursByEnseignant(Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }
        return coursRepository.findByEnseignantId(enseignantId);
    }


    public Cours createCourse(Long enseignantId, Cours cours, Long classeId) {
        if (cours == null) {
            throw new IllegalArgumentException("L'objet cours ne peut pas être null");
        }
        if (cours.getNom() == null || cours.getNom().isEmpty()) {
            throw new IllegalArgumentException("Le nom du cours est obligatoire");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId));

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new EntityNotFoundException("Classe non trouvée avec l'ID: " + classeId));

        cours.setEnseignant(enseignant);
        cours.setClasse(classe);

        return coursRepository.save(cours);
    }

    // 3. Gestion des étudiants
    public List<Etudiant> getEtudiantsByEnseignant(Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }
        return etudiantRepository.findByClasseEnseignantsId(enseignantId);
    }

    // 4. Gestion des notes
    public List<Note> getNotesByEnseignant(Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }
        return noteRepository.findByEnseignantId(enseignantId);
    }

    @Transactional
    public Note addNote(Long enseignantId, Note note, Long etudiantId, Long coursId) {
        if (note == null) {
            throw new IllegalArgumentException("L'objet note ne peut pas être null");
        }
        if (note.getTp() == null || note.getExam() == null) {
            throw new IllegalArgumentException("Les notes TP et Examen sont obligatoires");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

        if (!cours.getEnseignant().getId().equals(enseignantId)) {
            throw new IllegalStateException("Cet enseignant ne donne pas ce cours");
        }

        note.setEnseignant(enseignant);
        note.setEtudiant(etudiant);
        note.setCours(cours);
        note.setMoyenne(calculateMoyenne(note.getTp(), note.getExam()));

        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long noteId, Note noteDetails) {
        if (noteDetails == null) {
            throw new IllegalArgumentException("Les détails de la note ne peuvent pas être null");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note non trouvée"));

        if (noteDetails.getTp() == null || noteDetails.getExam() == null) {
            throw new IllegalArgumentException("Les notes TP et Examen sont obligatoires");
        }

        note.setTp(noteDetails.getTp());
        note.setExam(noteDetails.getExam());
        note.setAbsences(noteDetails.getAbsences());
        note.setMoyenne(calculateMoyenne(noteDetails.getTp(), noteDetails.getExam()));

        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            throw new EntityNotFoundException("Note non trouvée avec l'ID: " + noteId);
        }
        noteRepository.deleteById(noteId);
    }

    // 5. Gestion des congés
    public List<Conge> getCongesByEnseignant(Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }
        return congeRepository.findByEnseignantId(enseignantId);
    }

    @Transactional
    public Conge demanderConge(Long enseignantId, Conge conge) {
        if (conge == null) {
            throw new IllegalArgumentException("L'objet congé ne peut pas être null");
        }
        if (conge.getDateDebut() == null || conge.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et fin sont obligatoires");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

        conge.setEnseignant(enseignant);
        conge.setStatut(Conge.StatutConge.EN_ATTENTE);

        return congeRepository.save(conge);
    }

    @Transactional
    public void annulerConge(Long congeId) {
        if (!congeRepository.existsById(congeId)) {
            throw new EntityNotFoundException("Congé non trouvé avec l'ID: " + congeId);
        }
        congeRepository.deleteById(congeId);
    }

    // 6. Gestion des absences
    public List<Absence> getAbsencesByEnseignant(Long enseignantId) {
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }
        return absenceRepository.findByEnseignantId(enseignantId);
    }

    @Transactional
    public Absence createAbsence(Long enseignantId, Absence absence, Long etudiantId, Long coursId) {
        if (absence == null) {
            throw new IllegalArgumentException("L'objet absence ne peut pas être null");
        }
        if (absence.getDate() == null) {
            throw new IllegalArgumentException("La date de l'absence est obligatoire");
        }

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

        if (!cours.getEnseignant().getId().equals(enseignantId)) {
            throw new IllegalStateException("Cet enseignant ne donne pas ce cours");
        }

        absence.setEtudiant(etudiant);
        absence.setCours(cours);

        Absence savedAbsence = absenceRepository.save(absence);

        checkAndMarkEliminated(etudiantId, coursId);

        return savedAbsence;
    }

    @Transactional
    public Absence updateAbsence(Long absenceId, Absence absenceDetails) {
        if (absenceDetails == null) {
            throw new IllegalArgumentException("Les détails de l'absence ne peuvent pas être null");
        }

        Absence absence = absenceRepository.findById(absenceId)
                .orElseThrow(() -> new EntityNotFoundException("Absence non trouvée"));

        absence.setDate(absenceDetails.getDate());
        absence.setJustifiee(absenceDetails.isJustifiee());
        absence.setMotif(absenceDetails.getMotif());

        Absence updatedAbsence = absenceRepository.save(absence);

        if (updatedAbsence.getEtudiant() != null && updatedAbsence.getCours() != null) {
            checkAndMarkEliminated(updatedAbsence.getEtudiant().getId(), updatedAbsence.getCours().getId());
        }

        return updatedAbsence;
    }

    @Transactional
    public void deleteAbsence(Long absenceId) {
        if (!absenceRepository.existsById(absenceId)) {
            throw new EntityNotFoundException("Absence non trouvée avec l'ID: " + absenceId);
        }
        absenceRepository.deleteById(absenceId);
    }

    // Méthodes utilitaires
    private double calculateMoyenne(Double tp, Double exam) {
        if (tp == null || exam == null) {
            throw new IllegalArgumentException("Les notes TP et Examen sont obligatoires");
        }
        return (tp * 0.3) + (exam * 0.7);
    }

    @Transactional
    public void checkAndMarkEliminated(Long etudiantId, Long coursId) {
        int unjustifiedAbsences = absenceRepository.countByEtudiantIdAndCoursIdAndJustifieeFalse(etudiantId, coursId);

        if (unjustifiedAbsences >= 3) {
            Etudiant etudiant = etudiantRepository.findById(etudiantId)
                    .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

            etudiant.setEliminated(true);
            etudiantRepository.save(etudiant);
        }
    }

    @Transactional
    public int addCoursToAllEtudiants(Long enseignantId, Long coursId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé"));

        if (!cours.getEnseignant().getId().equals(enseignantId)) {
            throw new IllegalStateException("Ce cours n'appartient pas à cet enseignant");
        }

        List<Etudiant> etudiants = etudiantRepository.findByClasseEnseignantsId(enseignantId);

        etudiants.forEach(etudiant -> {
            if (!etudiant.getCours().contains(cours)) {
                etudiant.addCours(cours);
                etudiantRepository.save(etudiant);
            }
        });

        return etudiants.size();
    }

    @Transactional
    public Enseignant save(Enseignant enseignant) {
        if (enseignant == null) {
            throw new IllegalArgumentException("L'objet enseignant ne peut pas être null");
        }

        // Vérification des champs obligatoires
        if (enseignant.getNom() == null || enseignant.getNom().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'enseignant est obligatoire");
        }
        if (enseignant.getPrenom() == null || enseignant.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Le prénom de l'enseignant est obligatoire");
        }
        if (enseignant.getEmail() == null || enseignant.getEmail().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'enseignant est obligatoire");
        }

        // Vérification de l'unicité de l'email
        if (enseignant.getId() == null) { // Nouvel enseignant
            if (enseignantRepository.existsByEmail(enseignant.getEmail())) {
                throw new IllegalStateException("Un enseignant avec cet email existe déjà");
            }
        } else { // Mise à jour d'un enseignant existant
            Optional<Enseignant> existingEnseignant = enseignantRepository.findByEmail(enseignant.getEmail());
            if (existingEnseignant.isPresent() && !existingEnseignant.get().getId().equals(enseignant.getId())) {
                throw new IllegalStateException("Un autre enseignant avec cet email existe déjà");
            }
        }

        return enseignantRepository.save(enseignant);
    }

    public Enseignant findEnseignant(Long id) {
        return enseignantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enseignant non trouvé avec l'ID: " + id));
    }

    @Transactional
    public void deleteEtudiant(Long etudiantId) {
        etudiantRepository.deleteEtudiantWithRelations(etudiantId);
    }

    public List<Enseignant> getEnseignantsByClasseId(Long id) {
        return List.of();
    }

    public Optional<Enseignant> findById(Long id) {
        return enseignantRepository.findById(id);
    }
    public Optional<Enseignant> findCompleteEnseignantById(Long id) {
        return enseignantRepository.findById(id)
                .map(enseignant -> {
                    // Force le chargement des propriétés nécessaires
                    Hibernate.initialize(enseignant.getNom());
                    Hibernate.initialize(enseignant.getPrenom());
                    Hibernate.initialize(enseignant.getEmail());
                    return enseignant;
                });
    }
}