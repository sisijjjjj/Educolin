package com.example.educoline.repository;

import com.example.educoline.entity.Cours;
import com.example.educoline.entity.Note;
import com.example.educoline.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByEtudiant(Etudiant etudiant);
    List<Note> findByEnseignantId(Long enseignantId);

    List<Note> findByEtudiantId(Long etudiantId);

    Optional<Note> findByEnseignantIdAndCoursIdAndEtudiantId(Long enseignantId, Long coursId, Long etudiantId);

    Optional<Note> findByEtudiantAndCours(Etudiant etudiant, Cours cours);
}