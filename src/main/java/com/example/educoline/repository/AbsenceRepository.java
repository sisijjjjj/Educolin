package com.example.educoline.repository;

import com.example.educoline.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    List<Absence> findByEtudiantId(Long etudiantId);
    List<Absence> findByEnseignantId(Long enseignantId);

}
