package com.example.educoline.service;


import com.example.educoline.entity.Emploi;
import com.example.educoline.repository.EmploiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmploiService {
    private final EmploiRepository emploiRepository;

    public EmploiService(EmploiRepository emploiRepository) {
        this.emploiRepository = emploiRepository;
    }

    public Emploi getEmploiByEtudiantId(Long etudiantId) {
        // Implémentation dépendante de votre logique métier
        return emploiRepository.findByClasseId(1L) // À adapter selon votre modèle
                .orElseThrow(() -> new RuntimeException("Emploi non trouvé"));
    }

    public Emploi getEmploiByEnseignantId(Long enseignantId) {
        return emploiRepository.findByEnseignantId(enseignantId)
                .orElseThrow(() -> new RuntimeException("Emploi non trouvé"));
    }

    public List<Emploi> getAllEmplois() {
        return List.of();
    }
}
