package com.example.educoline.repository;

import com.example.educoline.entity.Cours;
import com.example.educoline.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CoursRepository extends JpaRepository<Cours, Long> {

    // 1. Trouver les cours par enseignant (version optimisée)
    List<Cours> findByEnseignantId(Long enseignantId);

    // 2. Trouver les cours par étudiant (version alternative plus lisible)
    @Query("SELECT c FROM Cours c JOIN c.etudiants e WHERE e.id = :etudiantId")
    List<Cours> findByEtudiantId(@Param("etudiantId") Long etudiantId);
    @Modifying
    @Query(value = "DELETE FROM etudiant_cours WHERE cours_id = :coursId", nativeQuery = true)
    void deleteEtudiantCoursRelations(@Param("coursId") Long coursId);
    // 3. Trouver les cours disponibles (sans enseignant assigné)
    List<Cours> findByEnseignantIsNull();

    // 4. Trouver les cours par nom (recherche insensible à la casse)
    List<Cours> findByNomContainingIgnoreCase(String nom);

    // 5. Trouver les cours par niveau
    List<Cours> findByNiveau(String niveau);

    // 6. Version alternative pour trouver les cours par étudiant (plus simple)
    List<Cours> findByEtudiants_Id(Long etudiantId);

    // 7. Vérifier si un cours existe pour un enseignant spécifique
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Cours c WHERE c.id = :coursId AND c.enseignant.id = :enseignantId")
    boolean existsByIdAndEnseignantId(@Param("coursId") Long coursId,
                                      @Param("enseignantId") Long enseignantId);

    List<Cours> findByEnseignant(Enseignant enseignant);

    void deleteByEnseignant(Enseignant enseignant);

    List<Cours> findByEtudiantsId(Long etudiantId);
}