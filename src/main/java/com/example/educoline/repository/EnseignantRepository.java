package com.example.educoline.repository;

import com.example.educoline.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {
    // Méthode pour vérifier l'existence d'un enseignant par email
    boolean existsByEmail(String email);

    // Méthode pour trouver un enseignant par son email
    Optional<Enseignant> findByEmail(String email);

    Set<Enseignant> findAllByIdIn(Set<Long> enseignantIds);

    // Vous pouvez ajouter d'autres méthodes de requête personnalisées si nécessaire
}