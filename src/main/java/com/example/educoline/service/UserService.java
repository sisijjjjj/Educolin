package com.example.educoline.service;

import com.example.educoline.entity.User;
import com.example.educoline.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Méthode pour ajouter un utilisateur
    public User addUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        // Sauvegarder l'utilisateur dans la base de données
        return userRepository.save(user);
    }
}
