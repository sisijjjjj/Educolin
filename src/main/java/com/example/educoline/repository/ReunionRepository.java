package com.example.educoline.repository;

import com.example.educoline.entity.Enseignant;
import com.example.educoline.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    List<Reunion> findByEnseignantId(Long enseignantId);

    List<Reunion> findByEnseignant(Enseignant enseignant);

    void deleteByEnseignant(Enseignant enseignant);
}
