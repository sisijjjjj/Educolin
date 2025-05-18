package com.example.educoline.repository;

import com.example.educoline.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClasseRepository extends JpaRepository<Classe, Long> {

    // Requête automatique pour trouver les classes d'un enseignant donné
    List<Classe> findByEnseignants_Id(Long enseignantId);
}
