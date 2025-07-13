package com.example.educoline.repository;

import com.example.educoline.entity.Etudiant;
import com.example.educoline.entity.Note;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Récupération basique des notes
    List<Note> findByEnseignantId(Long enseignantId);
    List<Note> findByEtudiantId(Long etudiantId);
    List<Note> findByCoursId(Long coursId);

    // Version avec chargement eager des relations
    @EntityGraph(attributePaths = {"etudiant", "cours", "enseignant"})
    List<Note> findByEtudiantIdAndDeletedFalse(Long etudiantId);

    @EntityGraph(attributePaths = {"etudiant", "cours", "enseignant"})
    List<Note> findByCoursIdAndDeletedFalse(Long coursId);

    // Recherche spécifique avec jointures
    @Query("SELECT n FROM Note n JOIN FETCH n.etudiant JOIN FETCH n.cours WHERE n.etudiant.id = :etudiantId AND n.cours.id = :coursId")
    Optional<Note> findByEtudiantAndCours(@Param("etudiantId") Long etudiantId,
                                          @Param("coursId") Long coursId);

    @EntityGraph(attributePaths = {"etudiant", "cours"})
    @Query("SELECT n FROM Note n WHERE n.enseignant.id = :enseignantId AND n.cours.id = :coursId AND n.etudiant.id = :etudiantId")
    Optional<Note> findByEnseignantAndCoursAndEtudiant(
            @Param("enseignantId") Long enseignantId,
            @Param("coursId") Long coursId,
            @Param("etudiantId") Long etudiantId);

    // Vérification d'existence
    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
    boolean existsByEtudiantIdAndCoursIdAndDeletedFalse(Long etudiantId, Long coursId);

    // Suppressions (logique et physique)
    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.deleted = true WHERE n.cours.id = :coursId")
    void softDeleteByCoursId(@Param("coursId") Long coursId);

    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.deleted = true WHERE n.etudiant.id = :etudiantId")
    void softDeleteByEtudiantId(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Note n WHERE n.cours.id = :coursId AND n.deleted = true")
    void hardDeleteByCoursId(@Param("coursId") Long coursId);

    // Méthodes dérivées supplémentaires
    @EntityGraph(attributePaths = {"cours"})
    Optional<Note> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    // Comptage avec filtre deleted
    long countByCoursId(Long coursId);
    long countByCoursIdAndDeletedFalse(Long coursId);

    // Nouvelle méthode pour récupérer toutes les notes d'un étudiant avec pagination
    @EntityGraph(attributePaths = {"cours", "enseignant"})
    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId AND n.deleted = false")
    List<Note> findAllActiveNotesByEtudiant(@Param("etudiantId") Long etudiantId);

    // Méthode pour trouver les notes par plusieurs étudiants (utile pour les classes)
    @EntityGraph(attributePaths = {"etudiant", "cours"})
    List<Note> findByEtudiantIdInAndDeletedFalse(Set<Long> etudiantIds);

    void deleteAllByEtudiant(Etudiant etudiant);

    void deleteByCoursId(Long coursId);

    void deleteByCours_Id(Long coursId);
}