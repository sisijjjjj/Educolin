package com.example.educoline.controller;

import com.example.educoline.entity.User;
import com.example.educoline.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200/")
public class UserController {

    private final UserService userService;

    // Cache temporaire pour simuler le dashboard admin (à remplacer par une vraie base de données)
    private static final Map<Object, User> adminDashboardCache = new HashMap<Object, User>();

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Validation simple
        if (user.getUsername() == null || user.getPassword() == null ||
                user.getEmail() == null || user.getRole() == null) {
            return ResponseEntity.badRequest().body("Tous les champs sont requis");
        }

        // Validate role
        String normalizedRole = user.getRole().toLowerCase().trim();
        if (!Arrays.asList("admin", "etudiant", "enseignant").contains(normalizedRole)) {
            return ResponseEntity.badRequest().body(
                    "Rôle invalide. Les rôles autorisés sont: admin, etudiant, enseignant"
            );
        }

        // Vérification disponibilité
        if (!userService.isUsernameAvailable(user.getUsername())) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur déjà pris");
        }
        if (!userService.isEmailAvailable(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email déjà utilisé");
        }

        // Création utilisateur avec le rôle normalisé
        try {
            User createdUser = userService.createUser(
                    user.getUsername(),
                    user.getPassword(),
                    normalizedRole,
                    user.getEmail()
            );

            // Ajouter l'utilisateur au dashboard admin
            addUserToAdminDashboard(createdUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la création de l'utilisateur");
        }
    }

    // Nouvelle méthode pour ajouter un utilisateur au dashboard admin
    private void addUserToAdminDashboard(User user) {
        // Ici on utilise un cache en mémoire, mais dans une vraie application,
        // vous devriez utiliser une table dédiée dans votre base de données
        adminDashboardCache.put(user.getId(), user);

        // Vous pouvez aussi logger cette action
        System.out.println("Utilisateur ajouté au dashboard admin: " + user.getUsername());
    }

    // Nouvelle méthode pour récupérer les utilisateurs du dashboard admin
    @GetMapping("/admin-dashboard")
    public ResponseEntity<List<User>> getAdminDashboardUsers() {
        return ResponseEntity.ok(List.copyOf(adminDashboardCache.values()));
    }

    // Nouvelle méthode pour mettre à jour le statut d'un utilisateur
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        if (!adminDashboardCache.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        User user = adminDashboardCache.get(id);
        user.setStatus(status); // Assurez-vous que votre entité User a un champ status

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User credentials) {
        try {
            User user = userService.authenticateByEmail(
                    credentials.getEmail(),
                    credentials.getPassword(),
                    credentials.getRole()
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        return ResponseEntity.ok(userService.isUsernameAvailable(username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
}