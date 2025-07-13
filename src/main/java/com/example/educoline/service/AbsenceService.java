package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AbsenceService {

    private static final int MAX_UNJUSTIFIED_ABSENCES = 3;

    private final AbsenceRepository absenceRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;

    public AbsenceService(AbsenceRepository absenceRepository,
                          EtudiantRepository etudiantRepository,
                          CoursRepository coursRepository,
                          EnseignantRepository enseignantRepository) {
        this.absenceRepository = absenceRepository;
        this.etudiantRepository = etudiantRepository;
        this.coursRepository = coursRepository;
        this.enseignantRepository = enseignantRepository;
    }

    @Transactional
    public Absence createAbsence(Long etudiantId, Long coursId, Absence absence) {
        Objects.requireNonNull(etudiantId, "L'ID étudiant ne peut pas être null");
        Objects.requireNonNull(coursId, "L'ID cours ne peut pas être null");
        Objects.requireNonNull(absence, "L'objet Absence ne peut pas être null");

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé avec ID: " + etudiantId));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé avec ID: " + coursId));

        absence.setEtudiant(etudiant);
        absence.setCours(cours);

        if (cours.getEnseignant() != null) {
            absence.setEnseignant(cours.getEnseignant());
        } else {
            throw new IllegalStateException("Le cours doit avoir un enseignant assigné");
        }

        return absenceRepository.save(absence);
    }
    @Transactional
    public List<Absence> getAllAbsences() {
        return absenceRepository.findAll();
    }
    @Transactional
    public Absence getAbsenceById(Long id) {
        return absenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Absence non trouvée avec ID: " + id));
    }
    @Transactional
    public Absence updateAbsence(Long id, Absence absenceDetails) {
        Absence absence = getAbsenceById(id);

        if (absenceDetails.getDate() != null) {
            absence.setDate(absenceDetails.getDate());
        }

        absence.setJustifiee(absenceDetails.isJustifiee());

        if (absenceDetails.getMotif() != null) {
            absence.setMotif(absenceDetails.getMotif());
        }

        return absenceRepository.save(absence);
    }

    public void deleteAbsence(Long id) {
        if (!absenceRepository.existsById(id)) {
            throw new EntityNotFoundException("Absence non trouvée avec ID: " + id);
        }
        absenceRepository.deleteById(id);
    }

    public List<Absence> getAbsencesByEtudiant(Long etudiantId) {
        return absenceRepository.findByEtudiantId(etudiantId);
    }

    public List<Absence> getAbsencesByCours(Long coursId) {
        return absenceRepository.findByCoursId(coursId);
    }

    public List<Absence> getAbsencesByEtudiantAndCours(Long etudiantId, Long coursId) {
        return absenceRepository.findByEtudiantIdAndCoursId(etudiantId, coursId);
    }

    public Absence justifyAbsence(Long absenceId, String motif) {
        Absence absence = getAbsenceById(absenceId);
        absence.justifier(motif);
        return absenceRepository.save(absence);
    }

    public boolean hasTooManyUnjustifiedAbsences(Long etudiantId, Long coursId) {
        int count = absenceRepository.countByEtudiantIdAndCoursIdAndJustifieeFalse(etudiantId, coursId);
        return count >= MAX_UNJUSTIFIED_ABSENCES;
    }

    public List<Absence> getJustifiedAbsencesByEtudiantId(Long etudiantId) {
        return absenceRepository.findByEtudiantIdAndJustifieeTrue(etudiantId);
    }

    public List<Absence> getUnjustifiedAbsencesByEtudiantId(Long etudiantId) {
        return absenceRepository.findByEtudiantIdAndJustifieeFalse(etudiantId);
    }

    public void deleteEtudiantAbsence(Long etudiantId, Long absenceId) {
        Absence absence = getAbsenceById(absenceId);
        if (!absence.getEtudiant().getId().equals(etudiantId)) {
            throw new IllegalArgumentException("L'absence n'appartient pas à l'étudiant spécifié");
        }
        absenceRepository.delete(absence);
    }

    public List<Absence> getAbsencesByEnseignantId(Long enseignantId) {
        return absenceRepository.findByEnseignantId(enseignantId);
    }

    public Absence addAbsenceToEtudiant(Long etudiantId, Long coursId, LocalDate date, boolean justifiee, String motif) {
        Absence absence = new Absence();
        absence.setDate(date);
        absence.setJustifiee(justifiee);
        absence.setMotif(motif);
        return createAbsence(etudiantId, coursId, absence);
    }

    public Absence justifyAbsence(Long etudiantId, Long absenceId, String motif) {
        Absence absence = getAbsenceById(absenceId);
        if (!absence.getEtudiant().getId().equals(etudiantId)) {
            throw new IllegalArgumentException("L'absence n'appartient pas à l'étudiant spécifié");
        }
        absence.justifier(motif);
        return absenceRepository.save(absence);
    }

    public List<Absence> getAbsencesByDate(LocalDate date) {
        return absenceRepository.findByDate(date);
    }

    public List<Absence> getAbsencesByEtudiantAndDate(Long etudiantId, LocalDate date) {
        return absenceRepository.findByEtudiantIdAndDate(etudiantId, date);
    }

    public List<Absence> getAbsencesByEnseignantAndCours(Long enseignantId, Long coursId) {
        return absenceRepository.findByEnseignantIdAndCoursId(enseignantId, coursId);
    }

    public int countUnjustifiedAbsencesForEtudiantInCours(Long etudiantId, Long coursId) {
        return absenceRepository.countByEtudiantIdAndCoursIdAndJustifieeFalse(etudiantId, coursId);
    }

    public void checkAndHandleElimination(Long etudiantId, Long coursId) {
        if (hasTooManyUnjustifiedAbsences(etudiantId, coursId)) {
            Etudiant etudiant = etudiantRepository.findById(etudiantId)
                    .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));
            etudiant.setEliminated(true);
            etudiantRepository.save(etudiant);
        }
    }

    public Absence addAbsenceToEtudiant(Long etudiantId, Long coursId, Absence absence) {
        return absence;
    }

    public Absence justifyAbsence(Long etudiantId, Long absenceId) {
        return null;
    }

    public int getNombreAbsencesByEtudiantId(Long id) {
        return 0;
    }

    public List<Absence> getAbsencesByEtudiantId(Long etudiantId) {
        return List.of();
    }
}