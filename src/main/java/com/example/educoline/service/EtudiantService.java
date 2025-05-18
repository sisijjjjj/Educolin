package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final CoursRepository coursRepository;
    private final ClasseRepository classeRepository;

    public EtudiantService(EtudiantRepository etudiantRepository,
                           NoteRepository noteRepository,
                           CoursRepository coursRepository,
                           ClasseRepository classeRepository) {
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.coursRepository = coursRepository;
        this.classeRepository = classeRepository;
    }

    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Optional<Etudiant> getEtudiantById(Long id) {
        return etudiantRepository.findById(id);
    }

    public Etudiant saveEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public Etudiant updateEtudiant(Long id, Etudiant etudiantDetails) {
        return etudiantRepository.findById(id)
                .map(etudiant -> {
                    etudiant.setNom(etudiantDetails.getNom());
                    etudiant.setPrenom(etudiantDetails.getPrenom());
                    etudiant.setEmail(etudiantDetails.getEmail());
                    etudiant.setStatus(etudiantDetails.getStatus());

                    if (etudiantDetails.getClasse() != null) {
                        Classe classe = classeRepository.findById(etudiantDetails.getClasse().getId())
                                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
                        etudiant.setClasse(classe);
                    }

                    return etudiantRepository.save(etudiant);
                })
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'id: " + id));
    }

    public boolean deleteEtudiant(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        // Supprimer les relations ManyToMany
        for (Cours cours : etudiant.getCours()) {
            cours.getEtudiants().remove(etudiant);
        }

        etudiantRepository.delete(etudiant);
        return false;
    }

    public Note addNoteToEtudiant(Long etudiantId, Note note) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        note.setEtudiant(etudiant);
        return noteRepository.save(note);
    }

    public List<Note> getNotesByEtudiant(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId);
    }

    public Etudiant addCoursToEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        etudiant.addCours(cours);
        return etudiantRepository.save(etudiant);
    }

    public Etudiant removeCoursFromEtudiant(Long etudiantId, Long coursId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        etudiant.removeCours(cours);
        return etudiantRepository.save(etudiant);
    }

    public Set<Cours> getCoursByEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        return (Set<Cours>) etudiant.getCours();
    }

    public List<Etudiant> getEtudiantsByClasse(Long classeId) {
        return etudiantRepository.findByClasseId(classeId);
    }

    public Etudiant addEtudiant(Etudiant etudiant) {
        return etudiant;
    }
}