package com.example.educoline.repository;

import com.example.educoline.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoursRepository extends JpaRepository<Cours, Long> {

    // Retourne la liste des cours d'un enseignant donné
    List<Cours> findByEnseignantId(Long enseignantId);

    // Retourne la liste des cours d'une classe donnée (corrigé le type de retour en List<Cours>)
    List<Cours> findByClasseId(Long classeId);

    // Retourne la liste des cours d'un étudiant donné
    List<Cours> findByEtudiants_Id(Long etudiantId);
}
