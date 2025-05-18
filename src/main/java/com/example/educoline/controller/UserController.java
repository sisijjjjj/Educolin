package com.example.educoline.controller;

import com.example.educoline.entity.User;
import com.example.educoline.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // Méthode pour ajouter un utilisateur
    @PostMapping("/api/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.addUser(user.getUsername(), user.getPassword(), user.getRole());

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}
