package com.example.educoline.repository;

import com.example.educoline.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Vous pouvez ajouter des méthodes personnalisées ici si nécessaire
}
