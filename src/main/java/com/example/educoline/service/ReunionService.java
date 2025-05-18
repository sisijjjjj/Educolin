package com.example.educoline.service;

import com.example.educoline.entity.Enseignant;
import com.example.educoline.entity.Reunion;
import com.example.educoline.repository.EnseignantRepository;
import com.example.educoline.repository.ReunionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReunionService {

    @Autowired
    private ReunionRepository reunionRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    public Reunion addReunionForEnseignant(Long enseignantId, Reunion reunion) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        reunion.setEnseignant(enseignant);
        return reunionRepository.save(reunion);
    }

    // Nouvelle méthode
    public Reunion saveReunion(Reunion reunion) {
        return reunionRepository.save(reunion);
    }

    public List<Reunion> getReunionsByEnseignantId(Long enseignantId) {
        return reunionRepository.findByEnseignantId(enseignantId);
    }

    public List<Reunion> getAllReunions() {
        return List.of();
    }
    public Optional<Reunion> getReunionById(Long id) {
        return reunionRepository.findById(id);
    }
}
