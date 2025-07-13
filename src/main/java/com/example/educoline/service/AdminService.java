package com.example.educoline.service;

import com.example.educoline.entity.Admin;
import com.example.educoline.entity.Etudiant;
import com.example.educoline.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public Admin getAdminById(Long id) {
        Optional<Admin> optionalAdmin = adminRepository.findById(id);
        return optionalAdmin.orElse(null);
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        Optional<Admin> optionalAdmin = adminRepository.findById(id);
        if (optionalAdmin.isPresent()) {
            Admin existingAdmin = optionalAdmin.get();
            existingAdmin.setNom(updatedAdmin.getNom());
            existingAdmin.setPrenom(updatedAdmin.getPrenom());
            existingAdmin.setEmail(updatedAdmin.getEmail());
            existingAdmin.setTelephone(updatedAdmin.getTelephone());
            existingAdmin.setAdresse(updatedAdmin.getAdresse());
            // Ajoute d'autres champs si nécessaire
            return adminRepository.save(existingAdmin);
        }
        return null;
    }


    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}
