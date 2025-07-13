package com.example.educoline.repository;

import com.example.educoline.entity.Conge;
import com.example.educoline.entity.Conge.StatutConge;
import com.example.educoline.entity.Conge.TypeConge;
import com.example.educoline.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    // ========== REQUÊTES DE BASE ==========
    List<Conge> findByEnseignantId(Long enseignantId);

    List<Conge> findByEnseignantIdAndStatut(Long enseignantId, StatutConge statut);

    Optional<Conge> findByIdAndEnseignantId(Long id, Long enseignantId);

    List<Conge> findByType(TypeConge type);

    // Supprimer la méthode statique qui retourne null et garder seulement celle-ci
    List<Conge> findByStatut(StatutConge statut);

    // ========== RECHERCHE PAR PÉRIODE ==========
    @Query("SELECT c FROM Conge c WHERE " +
            "(:enseignantId IS NULL OR c.enseignant.id = :enseignantId) AND " +
            "((c.dateDebut BETWEEN :start AND :end) OR " +
            "(c.dateFin BETWEEN :start AND :end) OR " +
            "(c.dateDebut <= :start AND c.dateFin >= :end)) " +
            "ORDER BY c.dateDebut ASC")
    List<Conge> findInDateRange(
            @Param("enseignantId") Long enseignantId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // ========== VÉRIFICATION CONFLITS ==========
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Conge c WHERE " +
            "c.enseignant.id = :enseignantId AND " +
            "c.statut = com.example.educoline.entity.Conge.StatutConge.APPROUVE AND " +
            "((c.dateDebut BETWEEN :start AND :end) OR " +
            "(c.dateFin BETWEEN :start AND :end) OR " +
            "(c.dateDebut <= :start AND c.dateFin >= :end))")
    boolean hasApprovedCongeConflict(
            @Param("enseignantId") Long enseignantId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    // ========== STATISTIQUES ==========
    @Query("SELECT COUNT(c) FROM Conge c WHERE " +
            "(:enseignantId IS NULL OR c.enseignant.id = :enseignantId) AND " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:year IS NULL OR YEAR(c.dateDebut) = :year)")
    long countByCriteria(
            @Param("enseignantId") Long enseignantId,
            @Param("statut") StatutConge statut,
            @Param("year") Integer year);

    // ========== MÉTHODES DE MISE À JOUR ==========
    @Transactional
    @Modifying
    @Query("UPDATE Conge c SET c.statut = :statut WHERE c.id = :id")
    int updateStatut(@Param("id") Long id, @Param("statut") StatutConge statut);

    @Transactional
    @Modifying
    @Query("DELETE FROM Conge c WHERE c.dateFin < :dateLimite")
    int deleteOldConges(@Param("dateLimite") LocalDate dateLimite);

    // ========== REQUÊTES POUR DASHBOARD ==========
    @Query("SELECT c FROM Conge c WHERE c.statut = 'EN_ATTENTE' ORDER BY c.dateDebut ASC")
    List<Conge> findPendingConges();

    @Query("SELECT c FROM Conge c WHERE c.statut = 'APPROUVE' AND c.dateFin >= CURRENT_DATE ORDER BY c.dateDebut ASC")
    List<Conge> findCurrentApprovedConges();

    List<Conge> findByStatut(com.example.educoline.entity.StatutConge statut);

    boolean existsByEnseignantAndStatutAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(Enseignant enseignant, StatutConge statutConge, LocalDate dateFin, LocalDate dateDebut);
}