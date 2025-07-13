package com.example.educoline.repository;

import com.example.educoline.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username); // Add this method

    User findByEmail(String email);

    boolean existsByEmail(String email);
}