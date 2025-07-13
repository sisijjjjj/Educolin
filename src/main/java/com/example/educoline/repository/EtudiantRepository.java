package com.example.educoline.repository;

import com.example.educoline.entity.Etudiant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    // ========== Méthodes de base avec chargement optimisé ==========
    @EntityGraph(attributePaths = {"classe", "enseignant"})
    Optional<Etudiant> findByEmail(String email);

    @EntityGraph(attributePaths = {"classe"})
    List<Etudiant> findByNom(String nom);

    @EntityGraph(attributePaths = {"classe"})
    List<Etudiant> findByPrenom(String prenom);

    @EntityGraph(attributePaths = {"classe"})
    List<Etudiant> findByNomAndPrenom(String nom, String prenom);

    boolean existsByEmail(String email);

    // ========== Méthodes pour les classes avec pagination ==========
    @EntityGraph(attributePaths = {"notes", "absences"})
    List<Etudiant> findByClasseId(Long classeId);

    @EntityGraph(attributePaths = {"notes", "absences"})
    Page<Etudiant> findByClasseId(Long classeId, Pageable pageable);

    @EntityGraph(attributePaths = {"classe"})
    @Query("SELECT e FROM Etudiant e WHERE e.classe.name = :nomClasse")
    List<Etudiant> findByNomClasse(@Param("nomClasse") String nameClasse);

    // ========== Méthodes pour les cours/enseignants optimisées ==========
    @EntityGraph(attributePaths = {"cours", "notes"})
    @Query("SELECT DISTINCT e FROM Etudiant e JOIN e.cours c WHERE c.enseignant.id = :enseignantId")
    List<Etudiant> findByEnseignantId(@Param("enseignantId") Long enseignantId);

    @EntityGraph(attributePaths = {"cours"})
    @Query("SELECT DISTINCT e FROM Etudiant e JOIN e.cours c WHERE c.enseignant.id = :enseignantId")
    List<Etudiant> findByCoursEnseignantId(@Param("enseignantId") Long enseignantId);

    @EntityGraph(attributePaths = {"notes", "absences"})
    @Query("SELECT e FROM Etudiant e JOIN e.cours c WHERE c.id = :coursId")
    List<Etudiant> findByCoursId(@Param("coursId") Long coursId);

    @EntityGraph(attributePaths = {"notes.cours", "notes.enseignant"})
    @Query("SELECT e FROM Etudiant e JOIN FETCH e.notes n WHERE n.cours.id = :coursId AND n.deleted = false")
    List<Etudiant> findByCoursIdWithNotes(@Param("coursId") Long coursId);

    @EntityGraph(attributePaths = {"classe.enseignants"})
    List<Etudiant> findByClasseEnseignantsId(Long enseignantId);

    // ========== Recherche avancée avec indexation ==========
    @EntityGraph(attributePaths = {"classe"})
    @Query("SELECT e FROM Etudiant e WHERE " +
            "LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Etudiant> search(@Param("keyword") String keyword);

    // ========== Gestion des absences optimisée ==========
    @EntityGraph(attributePaths = {"classe"})
    @Query("SELECT e FROM Etudiant e WHERE e.classe.id = :classeId AND e NOT IN " +
            "(SELECT a.etudiant FROM Absence a WHERE a.cours.id = :coursId AND a.date = CURRENT_DATE)")
    List<Etudiant> findAbsentsForToday(@Param("classeId") Long classeId,
                                       @Param("coursId") Long coursId);

    // ========== Gestion du statut avec filtres ==========
    @EntityGraph(attributePaths = {"classe"})
    List<Etudiant> findByEliminatedFalse();

    @EntityGraph(attributePaths = {"classe"})
    List<Etudiant> findByEliminatedTrue();

    @EntityGraph(attributePaths = {"cours"})
    List<Etudiant> findByCours_Enseignant_Id(Long enseignantId);

    // ========== Méthodes pour la suppression sécurisée optimisées ==========
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_cours WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteCoursLinks(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.deleted = true WHERE n.etudiant.id = :etudiantId")
    void softDeleteNotes(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Absence a WHERE a.etudiant.id = :etudiantId")
    void deleteAbsences(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_notes_tp WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteNotesTP(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_notes_examen WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteNotesExamen(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_moyennes WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteMoyennes(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_absences WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteAbsencesParMatiere(@Param("etudiantId") Long etudiantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM etudiant_elimination WHERE etudiant_id = :etudiantId", nativeQuery = true)
    void deleteEliminationParMatiere(@Param("etudiantId") Long etudiantId);

    @Transactional
    default void deleteEtudiantWithRelations(Long etudiantId) {
        // 1. Soft delete des notes (marquage comme deleted)
        softDeleteNotes(etudiantId);

        // 2. Supprimer les collections élémentaires
        deleteNotesTP(etudiantId);
        deleteNotesExamen(etudiantId);
        deleteMoyennes(etudiantId);
        deleteAbsencesParMatiere(etudiantId);
        deleteEliminationParMatiere(etudiantId);

        // 3. Supprimer les absences
        deleteAbsences(etudiantId);

        // 4. Supprimer les liens avec les cours
        deleteCoursLinks(etudiantId);

        // 5. Enfin supprimer l'étudiant
        deleteById(etudiantId);
    }

    // ========== Méthodes statistiques optimisées ==========
    @Query("SELECT COUNT(e) FROM Etudiant e WHERE e.classe.id = :classeId")
    Long countByClasseId(@Param("classeId") Long classeId);

    @Query("SELECT COUNT(e) FROM Etudiant e WHERE e.eliminated = true AND e.deleted = false")
    Long countEliminated();

    @Query("SELECT COUNT(e) FROM Etudiant e WHERE e.eliminated = false AND e.deleted = false")
    Long countActive();

    // ========== Nouvelles méthodes pour Angular ==========
    @EntityGraph(attributePaths = {"classe", "notes", "absences", "cours"})
    @Query("SELECT e FROM Etudiant e WHERE e.id = :id AND e.deleted = false")
    Optional<Etudiant> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"notes.cours", "notes.enseignant"})
    @Query("SELECT e FROM Etudiant e JOIN FETCH e.notes n WHERE e.id = :id AND n.deleted = false")
    Optional<Etudiant> findByIdWithNotes(@Param("id") Long id);

}