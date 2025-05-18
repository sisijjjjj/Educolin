package com.example.educoline.service;

import com.example.educoline.entity.*;
import com.example.educoline.repository.AdminRepository;
import com.example.educoline.repository.EnseignantRepository;
import com.example.educoline.repository.ReunionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnseignantService {

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private AdminRepository adminRepository; // Pour gérer les administrateurs

    @Autowired
    private ReunionRepository reunionRepository; // Injection du repository Reunion

    // --- Logique pour Enseignant ---

    public List<Enseignant> getAllEnseignants() {
        return enseignantRepository.findAll();
    }

    public Optional<Enseignant> getEnseignantById(Long id) {
        return enseignantRepository.findById(id);
    }



    public Enseignant updateEnseignant(Long id, Enseignant updatedEnseignant) {
        return enseignantRepository.findById(id).map(existing -> {
            existing.setName(updatedEnseignant.getName());
            existing.setEmail(updatedEnseignant.getEmail());
            existing.setSubject(updatedEnseignant.getSubject());
            return enseignantRepository.save(existing);
        }).orElse(null);
    }

    public void deleteEnseignant(Long id) {
        enseignantRepository.deleteById(id);
    }

    // --- Logique pour Admin ---

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        return adminRepository.findById(id).map(existing -> {
            existing.setNom(updatedAdmin.getNom());
            existing.setNiveau(updatedAdmin.getNiveau());
            existing.setSection(updatedAdmin.getSection());
            existing.setNombreEtudiants(updatedAdmin.getNombreEtudiants());
            // Ajouter plus de mises à jour si besoin
            return adminRepository.save(existing);
        }).orElse(null);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    // --- Logique liée aux entités associées (exemples simples, à compléter) ---

    public List<CongeRequest> getCongesForEnseignant(Long enseignantId) {
        // TODO: Implémenter la récupération des congés par enseignant
        return List.of();
    }

    public List<Absence> getAbsencesForEnseignant(Long enseignantId) {
        // TODO: Implémenter la récupération des absences par enseignant
        return List.of();
    }

    public Emploi getEmploiForEnseignant(Long enseignantId) {
        // TODO: Implémenter la récupération de l’emploi du temps
        return null;
    }

    public List<Classe> getClassesForEnseignant(Long enseignantId) {
        // TODO: Implémenter la récupération des classes associées
        return List.of();
    }

    // --- Méthode pour ajouter une réunion à un enseignant ---

    public Reunion addReunionForEnseignant(Long enseignantId, Reunion reunion) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        reunion.setEnseignant(enseignant);
        return reunionRepository.save(reunion);
    }

    public Enseignant saveEnseignant(Enseignant enseignant) {
        return enseignant;
    }

    public Enseignant createEnseignant(Enseignant enseignant) {
        enseignant.setId(null);  // Important pour éviter d’envoyer un id existant ou 0
        return enseignantRepository.save(enseignant);
    }


}
