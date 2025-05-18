package com.example.educoline.repository;

import com.example.educoline.entity.CongeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CongeRepository extends JpaRepository<CongeRequest, Long> {
    List<CongeRequest> findByEnseignantId(Long enseignantId);
    List<CongeRequest> findByStatut(String statut);
}