package com.example.educoline.entity;

import jakarta.persistence.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom = "";  // Valeur par défaut vide au lieu de null

    @Column(nullable = false)
    private String niveau = "Non spécifié";  // Valeur par défaut

    @Column(nullable = false)
    private String section = "Générale";  // Valeur par défaut

    @Column(nullable = false)
    private int nombreEtudiants = 0;  // Valeur par défaut

    // Coordonnées supplémentaires (peuvent être null)
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;

    // Relations
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "admin_enseignants",
            joinColumns = @JoinColumn(name = "admin_id"),
            inverseJoinColumns = @JoinColumn(name = "enseignant_id")
    )
    private List<Enseignant> enseignants = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "admin_etudiants",
            joinColumns = @JoinColumn(name = "admin_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id")
    )
    private List<Etudiant> etudiants = new ArrayList<>();
    private String nivel;

    // Constructeurs
    public Admin() {
    }

    public Admin(String nom, String niveau, String section) {
        this.nom = nom != null ? nom : "";
        this.niveau = niveau != null ? niveau : "Non spécifié";
        this.section = section != null ? section : "Générale";
    }

    // Getters et Setters avec protection contre null
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom != null ? nom : "";
    }

    public void setNom(String nom) {
        this.nom = nom != null ? nom : "";
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNiveau() {
        String nivel = "";
        return nivel != null ? nivel : "Non spécifié";
    }

    public void setNiveau(String nivel) {
        this.nivel = nivel != null ? nivel : "Non spécifié";
    }

    public String getSection() {
        return section != null ? section : "Générale";
    }

    public void setSection(String section) {
        this.section = section != null ? section : "Générale";
    }

    public int getNombreEtudiants() {
        return nombreEtudiants;
    }

    public void setNombreEtudiants(int nombreEtudiants) {
        this.nombreEtudiants = Math.max(nombreEtudiants, 0); // Évite les valeurs négatives
    }

    public List<Enseignant> getEnseignants() {
        return enseignants;
    }

    public void setEnseignants(List<Enseignant> enseignants) {
        this.enseignants = enseignants != null ? enseignants : new ArrayList<>();
    }

    public List<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(List<Etudiant> etudiants) {
        this.etudiants = etudiants != null ? etudiants : new ArrayList<>();
    }

    // Méthodes utilitaires pour gérer les relations
    public void addEnseignant(Enseignant enseignant) {
        if (enseignant != null && !this.enseignants.contains(enseignant)) {
            this.enseignants.add(enseignant);

        }
    }

    public void removeEnseignant(Enseignant enseignant) {
        if (enseignant != null) {
            this.enseignants.remove(enseignant);

        }
    }

    public void addEtudiant(Etudiant etudiant) throws InterruptedException {
        if (etudiant != null && !this.etudiants.contains(etudiant)) {
            this.etudiants.add(etudiant);
            etudiant.getAdmins().wait();
        }
    }



    // Méthode map simplifiée
    public Optional<ResponseEntity<Object>> map(Object o) {
        return Optional.empty();
    }

    // Méthode toString pour le débogage
    @Override
    public String toString() {
        String nivel = "";
        return "Admin{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", niveau='" + nivel + '\'' +
                ", section='" + section + '\'' +
                '}';
    }

    public String getPassword() {
        return "";
    }

    public Object getRole() {
        return null;
    }

    public void setRole(Object role) {
    }

    public Object getDateNaissance() {
        return null;
    }

    public Object getDiplome() {
        return null;
    }

    public Object getNbAnneeExperience() {
        return null;
    }

    public void getMatieresEnseignees() {
    }
}