package com.example.educoline.repository;

import com.example.educoline.entity.Absence;
import com.example.educoline.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    // Méthodes de recherche
    List<Absence> findByEnseignant_Id(Long enseignantId);
    List<Absence> findByEtudiant_Id(Long etudiantId);
    List<Absence> findByCours_Id(Long coursId);
    List<Absence> findByDate(LocalDate date);
    List<Absence> findByJustifiee(boolean justifiee);

    // Méthodes combinées
    List<Absence> findByEtudiant_IdAndDate(Long etudiantId, LocalDate date);
    List<Absence> findByEnseignant_IdAndCours_Id(Long enseignantId, Long coursId);
    List<Absence> findByEtudiant_IdAndJustifieeFalse(Long etudiantId);
    List<Absence> findByEtudiant_IdAndJustifieeTrue(Long etudiantId);
    List<Absence> findByEtudiant_IdAndCours_Id(Long etudiantId, Long coursId);

    // Méthodes de comptage
    int countByEtudiant_IdAndCours_Id(Long etudiantId, Long coursId);
    int countByEtudiant_IdAndCours_IdAndJustifieeFalse(Long etudiantId, Long coursId);

    // Méthodes de suppression
    void deleteByCours_Id(Long coursId);
    void deleteAllByEtudiant(Etudiant etudiant);

    // Version alternative avec @Query (au cas où)
    @Query("SELECT a FROM Absence a WHERE a.etudiant.id = :etudiantId AND a.cours.id = :coursId")
    List<Absence> findAbsencesByEtudiantAndCours(@Param("etudiantId") Long etudiantId,
                                                 @Param("coursId") Long coursId);

    List<Absence> findByEnseignantId(Long enseignantId);

    int countByEtudiantIdAndCoursIdAndJustifieeFalse(Long etudiantId, Long coursId);

    List<Absence> findByEtudiantId(Long etudiantId);

    List<Absence> findByCoursId(Long coursId);

    List<Absence> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    List<Absence> findByEtudiantIdAndJustifieeTrue(Long etudiantId);

    List<Absence> findByEtudiantIdAndJustifieeFalse(Long etudiantId);

    List<Absence> findByEtudiantIdAndDate(Long etudiantId, LocalDate date);

    List<Absence> findByEnseignantIdAndCoursId(Long enseignantId, Long coursId);

    int countByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
}