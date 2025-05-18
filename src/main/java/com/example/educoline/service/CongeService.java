package com.example.educoline.service;

import com.example.educoline.entity.CongeRequest;
import com.example.educoline.repository.CongeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class CongeService {
    @Autowired
    private CongeRepository congeRepository;

    public List<CongeRequest> getAllConges() {
        return congeRepository.findAll();
    }

    public List<CongeRequest> getCongesByStatut(String statut) {
        return congeRepository.findByStatut(statut);
    }

    public CongeRequest getCongeById(Long id) {
        return congeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));
    }

    public CongeRequest saveConge(CongeRequest conge) {
        return congeRepository.save(conge);
    }

    public List<CongeRequest> getCongesByEnseignantId(Long id) {
        return List.of();
    }
}