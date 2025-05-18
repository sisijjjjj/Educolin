package com.example.educoline.repository;

import com.example.educoline.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {
    // Pas besoin de redéfinir findById ici
}
