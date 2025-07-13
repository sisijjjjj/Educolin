package com.example.educoline.service;

import com.example.educoline.entity.User;
import com.example.educoline.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public User createUser(String username, String password, String role, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Note: Dans une application réelle, le mot de passe devrait être hashé
        user.setRole(role);
        user.setEmail(email);

        return userRepository.save(user);
    }

    public User authenticate(String username, String password, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        if (!user.getRole().equals(role)) {
            throw new RuntimeException("Rôle incorrect");
        }

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    public User authenticateByEmail(String email, String password, String role) {
        return null;
    }
}