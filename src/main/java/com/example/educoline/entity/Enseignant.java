package com.example.educoline.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "enseignants")
public class Enseignant {

    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "enseignant")
    private List<Etudiant> etudiants;

    @OneToMany(mappedBy = "enseignant")
    private List<Note> notes;

    private String name;
    private String email;
    private String subject;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String niveauScolaire;
    private String diplome;
    private int nbAnneeExperience;
    private int nbClasse;
    private String emploiTemps;

    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL)
    private List<Cours> cours;

    @ManyToMany(mappedBy = "enseignants")
    private List<Classe> classes;

    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL)
    private List<Reunion> reunions;

    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL)
    private List<CongeRequest> demandesConge; // 🆕 ajout de la relation avec CongeRequest

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public List<Etudiant> getEtudiants() { return etudiants; }
    public void setEtudiants(List<Etudiant> etudiants) { this.etudiants = etudiants; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getNiveauScolaire() { return niveauScolaire; }
    public void setNiveauScolaire(String niveauScolaire) { this.niveauScolaire = niveauScolaire; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public int getNbAnneeExperience() { return nbAnneeExperience; }
    public void setNbAnneeExperience(int nbAnneeExperience) { this.nbAnneeExperience = nbAnneeExperience; }

    public int getNbClasse() { return nbClasse; }
    public void setNbClasse(int nbClasse) { this.nbClasse = nbClasse; }

    public String getEmploiTemps() { return emploiTemps; }
    public void setEmploiTemps(String emploiTemps) { this.emploiTemps = emploiTemps; }

    public List<Cours> getCours() { return cours; }
    public void setCours(List<Cours> cours) { this.cours = cours; }

    public List<Classe> getClasses() { return classes; }
    public void setClasses(List<Classe> classes) { this.classes = classes; }

    public List<Reunion> getReunions() { return reunions; }
    public void setReunions(List<Reunion> reunions) { this.reunions = reunions; }

    public List<CongeRequest> getDemandesConge() { return demandesConge; }
    public void setDemandesConge(List<CongeRequest> demandesConge) { this.demandesConge = demandesConge; }
}
