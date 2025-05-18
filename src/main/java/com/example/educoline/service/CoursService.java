package com.example.educoline.service;

import com.example.educoline.entity.Cours;
import com.example.educoline.repository.CoursRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CoursService {

    private final CoursRepository coursRepository;

    public CoursService(CoursRepository coursRepository) {
        this.coursRepository = coursRepository;
    }

    // Récupérer tous les cours
    public List<Cours> getAllCours() {
        return coursRepository.findAll();
    }

    // Récupérer un cours par son id
    public Optional<Cours> getCoursById(Long id) {
        return coursRepository.findById(id);
    }

    // Ajouter ou modifier un cours
    public Cours saveCours(Cours cours) {
        return coursRepository.save(cours);
    }

    // Supprimer un cours par son id
    public void deleteCours(Long id) {
        coursRepository.deleteById(id);
    }

    // Récupérer tous les cours d’un enseignant donné
    public List<Cours> getCoursByEnseignantId(Long enseignantId) {
        return coursRepository.findByEnseignantId(enseignantId);
    }

    // Récupérer tous les cours d’un étudiant donné
    public List<Cours> getCoursByEtudiantId(Long etudiantId) {
        return coursRepository.findByEtudiants_Id(etudiantId);
    }
}
