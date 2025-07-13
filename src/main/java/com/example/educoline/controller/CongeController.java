package com.example.educoline.controller;

import com.example.educoline.entity.*;
import com.example.educoline.repository.CongeRepository;
import com.example.educoline.repository.EnseignantRepository;
import com.example.educoline.service.CongeService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin(origins = "ttp://localhost:58155/")
@RequestMapping("/api/conges")
public class CongeController {

    private final CongeRepository congeRepository;
    private final EnseignantRepository enseignantRepository;
    private final CongeService congeService;

    public CongeController(CongeRepository congeRepository,
                           EnseignantRepository enseignantRepository,
                           CongeService congeService) {
        this.congeRepository = congeRepository;
        this.enseignantRepository = enseignantRepository;
        this.congeService = congeService;
    }

    @PostMapping("/{congeId}/approve")
    @Transactional
    public ResponseEntity<Conge> approuverConge(@PathVariable Long congeId) {
        Conge conge = congeService.approveConge(congeId);
        return ResponseEntity.ok(conge);
    }

    @PostMapping("/{congeId}/reject")
    @Transactional
    public ResponseEntity<Conge> rejeterConge(
            @PathVariable Long congeId,
            @RequestBody(required = false) String motifRejet) {

        Conge conge = congeService.rejectConge(congeId, motifRejet);
        return ResponseEntity.ok(conge);
    }

    @PostMapping("/{congeId}/cancel")
    @Transactional
    public ResponseEntity<Conge> annulerConge(
            @PathVariable Long congeId,
            @RequestBody String motifAnnulation) {

        congeService.annulerConge(congeId, motifAnnulation);
        return ResponseEntity.ok().build();
    }
}