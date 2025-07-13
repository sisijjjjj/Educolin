package com.example.educoline.service;

import com.example.educoline.entity.Enseignant;
import com.example.educoline.entity.Reunion;
import com.example.educoline.repository.EnseignantRepository;
import com.example.educoline.repository.ReunionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final EnseignantRepository enseignantRepository;

    @Autowired
    public ReunionService(ReunionRepository reunionRepository,
                          EnseignantRepository enseignantRepository) {
        this.reunionRepository = reunionRepository;
        this.enseignantRepository = enseignantRepository;
    }

    /**
     * Ajoute une réunion pour un enseignant
     */
    public Reunion addReunionForEnseignant(Long enseignantId, Reunion reunion) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        reunion.setEnseignant(enseignant);
        return reunionRepository.save(reunion);
    }

    /**
     * Sauvegarde une réunion (méthode générique)
     */
    public Reunion save(Reunion reunion) {
        return reunionRepository.save(reunion);
    }

    /**
     * Récupère les réunions d'un enseignant
     */
    public List<Reunion> findByEnseignantId(Long enseignantId) {
        return reunionRepository.findByEnseignantId(enseignantId);
    }

    /**
     * Récupère toutes les réunions
     */
    public List<Reunion> getAllReunions() {
        return reunionRepository.findAll();
    }

    /**
     * Récupère une réunion par son ID
     */
    public Optional<Reunion> getReunionById(Long id) {
        return reunionRepository.findById(id);
    }

    /**
     * Met à jour une réunion
     */
    public Reunion updateReunion(Long id, Reunion reunionDetails) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        reunion.setSujet(reunionDetails.getSujet());
        reunion.setDateHeure(reunionDetails.getDateHeure());
        reunion.setLieu(reunionDetails.getLieu());
        reunion.setEmail(reunionDetails.getEmail());

        return reunionRepository.save(reunion);
    }

    /**
     * Supprime une réunion
     */
    public void deleteReunion(Long id) {
        reunionRepository.deleteById(id);
    }
}