package com.example.educoline.service;

import com.example.educoline.entity.Absence;
import com.example.educoline.repository.AbsenceRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AbsenceService {
    private final AbsenceRepository absenceRepository;

    public AbsenceService(AbsenceRepository absenceRepository) {
        this.absenceRepository = absenceRepository;
    }

    public List<Absence> getAbsencesByEtudiantId(Long etudiantId) {
        return absenceRepository.findByEtudiantId(etudiantId);
    }

    public List<Absence> getAbsencesByEnseignantId(Long enseignantId) {
        return absenceRepository.findByEnseignantId(enseignantId);
    }
}