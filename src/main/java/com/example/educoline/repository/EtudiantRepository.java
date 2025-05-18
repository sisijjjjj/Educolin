package com.example.educoline.repository;

import com.example.educoline.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    // Trouver tous les étudiants d'une classe donnée (classeId)
    List<Etudiant> findByClasseId(Long classeId);

    // Trouver tous les étudiants selon leur status (par ex. "actif", "inactif", etc.)
    List<Etudiant> findByStatus(String status);

}
