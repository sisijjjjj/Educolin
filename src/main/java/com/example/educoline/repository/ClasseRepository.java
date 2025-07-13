package com.example.educoline.repository;

import com.example.educoline.entity.Classe;
import com.example.educoline.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClasseRepository extends JpaRepository<Classe, Long> {

    // Trouver les classes d'un enseignant (requête optimisée)
    @Query("SELECT DISTINCT c FROM Classe c JOIN c.enseignants e WHERE e.id = :enseignantId")
    List<Classe> findByEnseignantId(@Param("enseignantId") Long enseignantId);

    // Vérification d'existence pour la validation
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT e FROM Etudiant e WHERE e.classe.name = :nameClasse")
    List<Etudiant> findEtudiantsByNameClasse(@Param("nameClasse") String nameClasse);

    // Trouver toutes les classes d'un niveau donné
    List<Classe> findByNiveau(String niveau);

    // Trouver les classes avec une moyenne générale supérieure à une valeur
    @Query("SELECT c FROM Classe c WHERE c.moyenneGenerale > :minMoyenne")
    List<Classe> findByMoyenneGeneraleGreaterThan(@Param("minMoyenne") Double minMoyenne);

    // Nouvelle méthode utile : trouver les classes avec le moins d'absences
    @Query("SELECT c FROM Classe c ORDER BY c.nombreAbsences ASC")
    List<Classe> findClassesWithLeastAbsences();

    // Nouvelle méthode utile : recherche par nom contenant une chaîne
    @Query("SELECT c FROM Classe c WHERE LOWER(c.name) LIKE LOWER(concat('%', :searchTerm, '%'))")
    List<Classe> searchByNameContaining(@Param("searchTerm") String searchTerm);
}