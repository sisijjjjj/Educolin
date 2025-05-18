package com.example.educoline.repository;

import com.example.educoline.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Méthode pour récupérer un utilisateur par son nom d'utilisateur
    User findByUsername(String username);
}
