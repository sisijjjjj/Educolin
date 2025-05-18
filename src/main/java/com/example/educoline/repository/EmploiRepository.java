package com.example.educoline.repository;


import com.example.educoline.entity.Emploi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmploiRepository extends JpaRepository<Emploi, Long> {
    Optional<Emploi> findByClasseId(Long classeId);
    Optional<Emploi> findByEnseignantId(Long enseignantId);
}