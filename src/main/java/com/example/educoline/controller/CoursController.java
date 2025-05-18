package com.example.educoline.controller;

import com.example.educoline.entity.Cours;
import com.example.educoline.service.CoursService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cours")
public class CoursController {

    private final CoursService coursService;

    public CoursController(CoursService coursService) {
        this.coursService = coursService;
    }

    // Récupérer tous les cours
    @GetMapping
    public List<Cours> getAllCours() {
        return coursService.getAllCours();
    }

    // Récupérer un cours par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Cours> getCoursById(@PathVariable Long id) {
        Optional<Cours> cours = coursService.getCoursById(id);
        return cours.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Ajouter un nouveau cours
    @PostMapping
    public ResponseEntity<Cours> createCours(@RequestBody Cours cours) {
        Cours savedCours = coursService.saveCours(cours);
        return new ResponseEntity<>(savedCours, HttpStatus.CREATED);
    }

    // Modifier un cours existant
    @PutMapping("/{id}")
    public ResponseEntity<Cours> updateCours(@PathVariable Long id, @RequestBody Cours coursDetails) {
        Optional<Cours> coursOptional = coursService.getCoursById(id);
        if (!coursOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Cours coursToUpdate = coursOptional.get();
        coursToUpdate.setNom(coursDetails.getNom());
        coursToUpdate.setDescription(coursDetails.getDescription());
        coursToUpdate.setHeureDebut(coursDetails.getHeureDebut());
        coursToUpdate.setHeureFin(coursDetails.getHeureFin());
        coursToUpdate.setEnseignant(coursDetails.getEnseignant());
        coursToUpdate.setEmploi(coursDetails.getEmploi());

        Cours updatedCours = coursService.saveCours(coursToUpdate);
        return ResponseEntity.ok(updatedCours);
    }

    // Supprimer un cours par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCours(@PathVariable Long id) {
        Optional<Cours> coursOptional = coursService.getCoursById(id);
        if (!coursOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        coursService.deleteCours(id);
        return ResponseEntity.noContent().build();
    }

    // Récupérer les cours par enseignant
    @GetMapping("/enseignant/{enseignantId}")
    public List<Cours> getCoursByEnseignant(@PathVariable Long enseignantId) {
        return coursService.getCoursByEnseignantId(enseignantId);
    }

    // Récupérer les cours par étudiant
    @GetMapping("/etudiant/{etudiantId}")
    public List<Cours> getCoursByEtudiant(@PathVariable Long etudiantId) {
        return coursService.getCoursByEtudiantId(etudiantId);
    }
}
