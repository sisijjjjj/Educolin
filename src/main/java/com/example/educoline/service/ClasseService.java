package com.example.educoline.service;

import com.example.educoline.entity.Classe;
import com.example.educoline.entity.Enseignant;
import com.example.educoline.repository.ClasseRepository;
import com.example.educoline.repository.EnseignantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ClasseService {

    private final ClasseRepository classeRepository;
    private final EnseignantRepository enseignantRepository;

    @Autowired
    public ClasseService(ClasseRepository classeRepository,
                         EnseignantRepository enseignantRepository) {
        this.classeRepository = classeRepository;
        this.enseignantRepository = enseignantRepository;
    }

    // Méthodes de base
    public List<Classe> getAllClasses() {
        return classeRepository.findAll();
    }

    public Classe saveClasse(Classe classe) {
        return classeRepository.save(classe);
    }

    public Optional<Classe> getClasseById(Long id) {
        return classeRepository.findById(id);
    }

    public void deleteClasse(Long id) {
        classeRepository.deleteById(id);
    }

    // Méthode simplifiée sans exception personnalisée
    public Classe findClasseOrThrow(Long id) {
        return classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
    }

    // Méthodes de vérification
    public boolean existsByName(String name) {
        return classeRepository.existsByName(name);
    }

    public boolean existsByNameIgnoreCase(String name) {
        return classeRepository.existsByNameIgnoreCase(name);
    }

    // Méthode de création
    public Classe createClasse(Classe classe) {
        if (classe.getName() == null || classe.getName().trim().isEmpty()) {
            throw new RuntimeException("Le nom de la classe est requis");
        }

        // Valeurs par défaut
        classe.setNiveau(classe.getNiveau() != null ? classe.getNiveau().trim() : "Non spécifié");
        classe.setMoyenneGenerale(classe.getMoyenneGenerale() != null ? classe.getMoyenneGenerale() : 0.0);
        classe.setNombreAbsences(classe.getNombreAbsences() != null ? classe.getNombreAbsences() : 0);

        return classeRepository.save(classe);
    }

    // Méthodes pour les enseignants
    public Classe addEnseignantsToClasse(Long classeId, Set<Long> enseignantIds) {
        Classe classe = findClasseOrThrow(classeId);
        Set<Enseignant> enseignants = enseignantRepository.findAllByIdIn(enseignantIds);

        classe.getEnseignants().addAll(enseignants);
        return classeRepository.save(classe);
    }

    // Méthode de mise à jour
    public Classe updateClasse(Long id, Classe classeDetails) {
        Classe classe = findClasseOrThrow(id);

        if (classeDetails.getName() != null) {
            classe.setName(classeDetails.getName().trim());
        }

        if (classeDetails.getNiveau() != null) {
            classe.setNiveau(classeDetails.getNiveau().trim());
        }

        return classeRepository.save(classe);
    }

    // Méthodes de recherche
    public Optional<Classe> findById(Long id) {
        return classeRepository.findById(id);
    }

    public Classe findClasse(Long id) {
        return findClasseOrThrow(id);
    }

    public boolean existeParNom(String nom) {
        return classeRepository.existsByName(nom);
    }
    public Classe save(Classe nouvelleClasse) {
        // Sauvegarde la classe
        Classe savedClasse = classeRepository.save(nouvelleClasse);

        // Met à jour les relations bidirectionnelles
        if (savedClasse.getEnseignants() != null) {
            savedClasse.getEnseignants().forEach(enseignant -> {
                enseignant.getClasses().add(savedClasse);
                enseignantRepository.save(enseignant);
            });
        }

        return savedClasse;
    }


    @Transactional
    public Classe saveWithEnseignants(Classe nouvelleClasse) {
        // 1. Sauvegarde de la classe elle-même
        Classe savedClasse = classeRepository.save(nouvelleClasse);

        // 2. Gestion des relations avec les enseignants
        if (savedClasse.getEnseignants() != null && !savedClasse.getEnseignants().isEmpty()) {
            // Créer une copie pour éviter ConcurrentModificationException
            Set<Enseignant> enseignants = new HashSet<>(savedClasse.getEnseignants());

            for (Enseignant enseignant : enseignants) {
                // 3. Vérifier que l'enseignant existe en base
                Enseignant existingEnseignant = enseignantRepository.findById(enseignant.getId())
                        .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + enseignant.getId()));

                // 4. Mise à jour bidirectionnelle
                existingEnseignant.getClasses().add(savedClasse);
                enseignantRepository.save(existingEnseignant);
            }
        }

        // 5. Recharger la classe pour s'assurer d'avoir les dernières données
        return classeRepository.findById(savedClasse.getId())
                .orElseThrow(() -> new RuntimeException("Erreur lors du rechargement de la classe"));
    }

    public List<Classe> findAllClasses() {
        return List.of();
    }
}